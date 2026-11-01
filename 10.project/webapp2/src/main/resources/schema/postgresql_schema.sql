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
    paid_leave_days  integer DEFAULT 0,
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
    is_prescribed_holiday   boolean DEFAULT false
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
    enabled   integer DEFAULT 1
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
    created_at        timestamp DEFAULT CURRENT_TIMESTAMP,
    updated_at        timestamp DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_staff_week UNIQUE (staff_id, week_end_date)
);

COMMENT ON COLUMN weekly_work_report.week_end_date IS '当該週の水曜日（木～翌水の週区切り）';
COMMENT ON COLUMN weekly_work_report.progress_status IS '作業進捗（オンスケ/〇日遅れ等）';
COMMENT ON COLUMN weekly_work_report.progress_detail IS '作業進捗、作業内容と状況';
COMMENT ON COLUMN weekly_work_report.personal_note IS '本人所感';
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
