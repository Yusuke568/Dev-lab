-- ============================================================================
-- 勤怠管理システム（webapp2）PostgreSQL スキーマ
-- Supabase（または任意のPostgreSQL）に対してそのまま実行してください。
-- MySQL版からのデータ移行は行わず、スキーマのみを新規作成する前提。
-- ============================================================================

-- updated_at を自動更新するための共通トリガー関数（MySQLの ON UPDATE CURRENT_TIMESTAMP 相当）
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ----------------------------------------------------------------------------
-- 社員マスタ
-- ----------------------------------------------------------------------------
CREATE TABLE staff_table (
    id               integer PRIMARY KEY,
    name             varchar(100),
    namekana         varchar(100),
    entry_year       integer,
    gender           varchar(10),
    paid_leave_days  numeric(5,1) DEFAULT 0,  -- 有給残日数（半休を 0.5 日として数える）
    class_id         integer
);

-- ----------------------------------------------------------------------------
-- 職種マスタ
-- ----------------------------------------------------------------------------
CREATE TABLE class_master (
    id    integer PRIMARY KEY,
    name  varchar(50)
);

-- ----------------------------------------------------------------------------
-- 勤務区分マスタ（出勤・有給・休日等）
-- ----------------------------------------------------------------------------
CREATE TABLE abstract_master (
    id                      integer PRIMARY KEY,
    name                    varchar(50),
    is_work                 boolean DEFAULT false,
    is_paid                 boolean DEFAULT false,
    is_legal_holiday        boolean DEFAULT false,
    is_prescribed_holiday   boolean DEFAULT false,
    paid_leave_days         numeric(2,1) NOT NULL DEFAULT 0  -- 有給消費日数（有給 1、半休 0.5、それ以外 0）
);

-- ----------------------------------------------------------------------------
-- 勤怠実績（日次）
-- ----------------------------------------------------------------------------
CREATE TABLE work_month_table (
    staff_id                 integer NOT NULL,
    work_date                date NOT NULL,
    work_week                varchar(10),
    correction_id            integer,
    job_from_time             timestamp,
    job_to_time               timestamp,
    correction_us_time        integer,
    correction_mid_time       integer,
    indirect_time             integer,
    total_work_time           integer,
    total_direct_work_time    integer,
    overtime                  integer DEFAULT 0,
    abstract_id               integer,
    remarks                   varchar(255),
    approval_status           integer DEFAULT 0,
    PRIMARY KEY (staff_id, work_date)
);

-- ----------------------------------------------------------------------------
-- 有給付与ルール
-- ----------------------------------------------------------------------------
CREATE TABLE leave_grant_rules (
    id                integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    years_of_service  integer,
    grant_days        integer
);

-- ----------------------------------------------------------------------------
-- ログイン情報
-- ----------------------------------------------------------------------------
CREATE TABLE login_table (
    username  varchar(50) PRIMARY KEY,
    password  varchar(255),  -- PBKDF2 ハッシュ（pbkdf2_sha256$反復回数$ソルト$ハッシュ）。平文は保存しない
    enabled   integer DEFAULT 1,
    must_change_password boolean NOT NULL DEFAULT false  -- true なら次回ログイン時にパスワード変更を強制する
);

-- ----------------------------------------------------------------------------
-- 勤怠承認履歴
-- ----------------------------------------------------------------------------
CREATE TABLE kintai_approval_history (
    id          integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    staff_id    integer NOT NULL,
    work_date   date NOT NULL,
    decision    varchar(10) NOT NULL,
    decided_by  integer NOT NULL,
    decided_at  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 補正CDマスタ（事前登録マスタ）
-- ----------------------------------------------------------------------------
CREATE TABLE correction_master (
    id                   integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name                 varchar(100) NOT NULL,
    correction_us_time   integer NOT NULL DEFAULT 0,
    correction_mid_time  integer NOT NULL DEFAULT 0
);

-- ----------------------------------------------------------------------------
-- 残業事前申請
-- ----------------------------------------------------------------------------
CREATE TABLE overtime_request (
    id                        integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    staff_id                  integer NOT NULL,
    target_date               date NOT NULL,
    planned_start_time        varchar(5),
    planned_overtime_minutes  integer NOT NULL DEFAULT 0,
    planned_end_time          varchar(5),
    reason                    varchar(255),
    status                    varchar(10) NOT NULL DEFAULT 'PENDING',
    decided_by                integer,
    decided_at                timestamp,
    created_at                timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_staff_date UNIQUE (staff_id, target_date)
);

-- ----------------------------------------------------------------------------
-- 週次作業報告（木～翌水の週単位、作業報告書Excel出力用）
-- ----------------------------------------------------------------------------
CREATE TABLE weekly_work_report (
    id                integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    staff_id          integer NOT NULL,
    week_end_date     date NOT NULL,
    client_name       varchar(100),
    project_name      varchar(100),
    process_name      varchar(200),
    progress_status   varchar(100),
    progress_detail   text,
    personal_note     text,
    problem_countermeasure text,  -- 問題点・対処（作業報告書の「問題点・対処：」の下の2行）
    created_at        timestamp DEFAULT CURRENT_TIMESTAMP,
    updated_at        timestamp DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_staff_week UNIQUE (staff_id, week_end_date)
);

COMMENT ON COLUMN weekly_work_report.week_end_date IS '当該週の水曜日（木～翌水の週区切り）';
COMMENT ON COLUMN weekly_work_report.progress_status IS '作業進捗（オンスケ/〇日遅れ等）';
COMMENT ON COLUMN weekly_work_report.progress_detail IS '作業進捗、作業内容と状況';
COMMENT ON COLUMN weekly_work_report.personal_note IS '本人所感';
-- リーダー所見（作業報告書の取り込みでだけ入る。画面では編集しない。migrations/20261116_weekly_leader_comment.sql と同じ）
ALTER TABLE weekly_work_report ADD COLUMN IF NOT EXISTS leader_comment text;
COMMENT ON COLUMN weekly_work_report.leader_comment IS 'リーダー所見（作業報告書の AU列から取り込む）';
COMMENT ON TABLE weekly_work_report IS '週次作業報告（木～翌水の週単位、作業報告書Excel出力用）';

CREATE TRIGGER trg_weekly_work_report_updated_at
    BEFORE UPDATE ON weekly_work_report
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

-- ============================================================================
-- 初期データ（マスタ類の最低限のシード。実データは別途投入してください）
-- ============================================================================
INSERT INTO abstract_master (id, name, is_work, is_paid, is_legal_holiday, is_prescribed_holiday) VALUES
    (1, '出勤', true, false, false, false),
    (2, '有給', false, true, false, false),
    (10, '休日', false, false, false, true);

INSERT INTO class_master (id, name) VALUES
    (1, 'エンジニア'),
    (2, 'リーダー'),
    (3, 'マネージャー');

-- ============================================================================
-- 権限グループ・組織図・承認ルート・休暇申請（migrations/20261103_org_permission_approval.sql と同じ）
-- ============================================================================
-- ----------------------------------------------------------------------------
-- 権限グループと、グループごとに使える機能
-- feature_code は Java の com.example.permission.domain.model.Feature の名前
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS permission_group (
    id    integer GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name  varchar(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS permission_group_feature (
    group_id      integer NOT NULL REFERENCES permission_group (id) ON DELETE CASCADE,
    feature_code  varchar(30) NOT NULL,
    PRIMARY KEY (group_id, feature_code)
);

-- 役職をどの権限グループに置くか
ALTER TABLE class_master ADD COLUMN IF NOT EXISTS permission_group_id integer REFERENCES permission_group (id);

-- ----------------------------------------------------------------------------
-- 組織図（部署ツリー）。approver_staff_id がその部署の承認者
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS department (
    id                 integer GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name               varchar(100) NOT NULL,
    parent_id          integer REFERENCES department (id),
    approver_staff_id  integer,
    sort_order         integer NOT NULL DEFAULT 0
);

ALTER TABLE staff_table ADD COLUMN IF NOT EXISTS department_id integer REFERENCES department (id);

-- ----------------------------------------------------------------------------
-- 承認ルート設定（申請種別ごとに、組織図を何段上までたどるか）
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS approval_route_setting (
    request_type  varchar(20) PRIMARY KEY,   -- OVERTIME / LEAVE
    max_levels    integer NOT NULL DEFAULT 1 CHECK (max_levels >= 1)
);

-- ----------------------------------------------------------------------------
-- 休暇申請（有給・代休など。承認されても勤怠には自動反映しない）
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS leave_request (
    id             integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    staff_id       integer NOT NULL,
    target_date    date NOT NULL,
    abstract_id    integer NOT NULL,
    reason         varchar(255),
    status         varchar(10) NOT NULL DEFAULT 'PENDING',   -- PENDING / APPROVED / REJECTED
    current_level  integer NOT NULL DEFAULT 1,
    created_at     timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_leave_staff_date UNIQUE (staff_id, target_date)
);

-- 残業事前申請にも「いま何段目の承認待ちか」を持たせる
ALTER TABLE overtime_request ADD COLUMN IF NOT EXISTS current_level integer NOT NULL DEFAULT 1;

-- ----------------------------------------------------------------------------
-- 承認ステップ（申請ごとの承認ルート。1段ごとに1行）
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS approval_step (
    id                 integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_type       varchar(20) NOT NULL,   -- OVERTIME / LEAVE
    request_id         integer NOT NULL,
    level              integer NOT NULL,
    approver_staff_id  integer NOT NULL,
    decision           varchar(10),            -- NULL=未処理 / APPROVED / REJECTED
    comment            varchar(255),
    decided_at         timestamp,
    -- 同じ段に候補者（主承認者・副承認者など）を複数行で持てる。誰か1人の判定でその段は終わり
    CONSTRAINT uq_approval_step_candidate UNIQUE (request_type, request_id, level, approver_staff_id)
);

CREATE INDEX IF NOT EXISTS idx_approval_step_approver ON approval_step (approver_staff_id, decision);

-- ============================================================================
-- 初期データ
-- ============================================================================
INSERT INTO permission_group (name) SELECT '一般' WHERE NOT EXISTS (SELECT 1 FROM permission_group WHERE name = '一般');
INSERT INTO permission_group (name) SELECT '承認者' WHERE NOT EXISTS (SELECT 1 FROM permission_group WHERE name = '承認者');
INSERT INTO permission_group (name) SELECT '管理者' WHERE NOT EXISTS (SELECT 1 FROM permission_group WHERE name = '管理者');

-- 一般: 自分の勤怠・残業申請・休暇申請
INSERT INTO permission_group_feature (group_id, feature_code)
SELECT g.id, f.code FROM permission_group g, (VALUES ('ATTENDANCE'), ('OVERTIME_REQUEST'), ('LEAVE_REQUEST')) AS f(code)
WHERE g.name = '一般'
  AND NOT EXISTS (SELECT 1 FROM permission_group_feature x WHERE x.group_id = g.id AND x.feature_code = f.code);

-- 承認者: 一般 + 承認
INSERT INTO permission_group_feature (group_id, feature_code)
SELECT g.id, f.code FROM permission_group g, (VALUES ('ATTENDANCE'), ('OVERTIME_REQUEST'), ('LEAVE_REQUEST'), ('APPROVAL')) AS f(code)
WHERE g.name = '承認者'
  AND NOT EXISTS (SELECT 1 FROM permission_group_feature x WHERE x.group_id = g.id AND x.feature_code = f.code);

-- 管理者: すべて
INSERT INTO permission_group_feature (group_id, feature_code)
SELECT g.id, f.code FROM permission_group g,
    (VALUES ('ATTENDANCE'), ('OVERTIME_REQUEST'), ('LEAVE_REQUEST'), ('APPROVAL'),
            ('SHAIN_ADMIN'), ('PAID_LEAVE_ADMIN'),
            ('MASTER_PERMISSION_GROUP'), ('MASTER_JOB_CLASS'), ('MASTER_DEPARTMENT'), ('MASTER_APPROVAL_ROUTE'),
            ('MASTER_ABSTRACT'), ('MASTER_LEAVE_RULE'), ('MASTER_CORRECTION'), ('MASTER_COMPANY_HOLIDAY')) AS f(code)
WHERE g.name = '管理者'
  AND NOT EXISTS (SELECT 1 FROM permission_group_feature x WHERE x.group_id = g.id AND x.feature_code = f.code);

-- 権限グループ未設定の役職は「一般」にする
UPDATE class_master SET permission_group_id = (SELECT id FROM permission_group WHERE name = '一般')
WHERE permission_group_id IS NULL;

-- 承認ルートは残業・休暇とも 2 段（所属部署の承認者 → 上位部署の承認者）
INSERT INTO approval_route_setting (request_type, max_levels) SELECT 'OVERTIME', 2
WHERE NOT EXISTS (SELECT 1 FROM approval_route_setting WHERE request_type = 'OVERTIME');
INSERT INTO approval_route_setting (request_type, max_levels) SELECT 'LEAVE', 2
WHERE NOT EXISTS (SELECT 1 FROM approval_route_setting WHERE request_type = 'LEAVE');

-- 初期の役職はすべて「一般」。自分の役職を管理者にする手順は migrations/20261103_org_permission_approval.sql の末尾を参照

-- ----------------------------------------------------------------------------
-- 会社休日（年末年始・創立記念日など、国民の祝日以外の会社の休日）
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS company_holiday (
    holiday_date  date PRIMARY KEY,
    name          varchar(100) NOT NULL,
    created_at    timestamp DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 上長からのお知らせ（department_id の部署とその下の部署の社員に表示）
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notice (
    id               integer GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    author_staff_id  integer NOT NULL,
    department_id    integer NOT NULL REFERENCES department (id),
    body             text NOT NULL,
    expires_on       date,
    created_at       timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_notice_department ON notice (department_id);
CREATE INDEX IF NOT EXISTS idx_notice_author ON notice (author_staff_id);

-- ============================================================================
-- 承認ルートを柔軟にする（migrations/20261112_flexible_approval_route.sql と同じ）
-- ============================================================================
-- 兼任部署（主所属は今までどおり staff_table.department_id）
CREATE TABLE IF NOT EXISTS staff_department (
    staff_id       integer NOT NULL,
    department_id  integer NOT NULL REFERENCES department (id) ON DELETE CASCADE,
    PRIMARY KEY (staff_id, department_id)
);

-- 部署の副承認者（主承認者は今までどおり department.approver_staff_id）。どちらか1人が承認すればよい
CREATE TABLE IF NOT EXISTS department_approver (
    department_id  integer NOT NULL REFERENCES department (id) ON DELETE CASCADE,
    staff_id       integer NOT NULL,
    PRIMARY KEY (department_id, staff_id)
);

-- 承認ルートマスタ。request_type が NULL なら残業・休暇の両方に使う
CREATE TABLE IF NOT EXISTS approval_route (
    id            integer GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name          varchar(100) NOT NULL,
    request_type  varchar(20),
    note          varchar(255)
);

-- ルートの段ごとの承認者。同じ段に複数いれば、誰か1人の承認で次の段へ進む
CREATE TABLE IF NOT EXISTS approval_route_step (
    route_id  integer NOT NULL REFERENCES approval_route (id) ON DELETE CASCADE,
    level     integer NOT NULL CHECK (level >= 1),
    staff_id  integer NOT NULL,
    sort_order integer NOT NULL DEFAULT 0,  -- 段の中の並び。先頭がその段の主承認者
    PRIMARY KEY (route_id, level, staff_id)
);

-- ルートの条件つきで足す段（残業事前申請だけ。migrations/20261114_route_extra_levels.sql と同じ）
--   MONTHLY_OVERTIME … 当月の時間外（実績＋今回の申請）が threshold_minutes 以上 / NIGHT … 予定が22:00〜翌5:00にかかる
CREATE TABLE IF NOT EXISTS approval_route_extra_step (
    route_id          integer NOT NULL REFERENCES approval_route (id) ON DELETE CASCADE,
    seq               integer NOT NULL CHECK (seq >= 1),
    condition_type    varchar(20) NOT NULL,
    threshold_minutes integer,
    staff_id          integer NOT NULL,
    sort_order        integer NOT NULL DEFAULT 0,
    PRIMARY KEY (route_id, seq, staff_id)
);

-- ルートの割り当て先（社員または部署）。社員への割り当てが部署より優先。部署の割り当ては配下の部署にも効く
CREATE TABLE IF NOT EXISTS approval_route_assignment (
    id           integer GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    route_id     integer NOT NULL REFERENCES approval_route (id) ON DELETE CASCADE,
    target_type  varchar(20) NOT NULL CHECK (target_type IN ('STAFF', 'DEPARTMENT')),
    target_id    integer NOT NULL,
    CONSTRAINT uq_approval_route_assignment UNIQUE (route_id, target_type, target_id)
);
CREATE INDEX IF NOT EXISTS idx_approval_route_assignment_target ON approval_route_assignment (target_type, target_id);

-- 申請がどの部署の業務か（兼任している人が選ぶ。NULL なら主所属）
ALTER TABLE overtime_request ADD COLUMN IF NOT EXISTS department_id integer;
ALTER TABLE leave_request ADD COLUMN IF NOT EXISTS department_id integer;
