-- ============================================================================
-- 勤怠管理システム（webapp2）PostgreSQL スキーマ
-- Supabase（または任意のPostgreSQL）に対してそのまま実行してください。
-- 20.db/createtb の MySQL 版（all_createtables.sql / ses_tables.sql / all_createviews.sql）を
-- PostgreSQL 用に書き換えたもの。サンプルデータは postgresql_sample_data.sql。
-- ============================================================================

DROP VIEW IF EXISTS view_work_month_summary;
DROP TABLE IF EXISTS monthly_summaries;
DROP TABLE IF EXISTS weekly_report_comments;
DROP TABLE IF EXISTS weekly_reports;
DROP TABLE IF EXISTS work_time_segments;
DROP TABLE IF EXISTS daily_attendances;
DROP TABLE IF EXISTS assignments;
DROP TABLE IF EXISTS project_contracts;
DROP TABLE IF EXISTS work_month_table;
DROP TABLE IF EXISTS login_table;
DROP TABLE IF EXISTS staff_table;
DROP TABLE IF EXISTS class_master;
DROP TABLE IF EXISTS abstract_master;
DROP TABLE IF EXISTS leave_grant_rules;

-- ----------------------------------------------------------------------------
-- 適用マスタ
-- フラグは MySQL 版の tinyint(1) に合わせて 0/1 の integer（Java 側は getBoolean で読む）
-- ----------------------------------------------------------------------------
CREATE TABLE abstract_master (
    id                     integer PRIMARY KEY,               -- 適用区分
    name                   varchar(40),                       -- 適用名称
    is_work                integer DEFAULT 0,                 -- 労働日フラグ(1：労働, 0：休み)
    is_paid                integer DEFAULT 0,                 -- 有給対象フラグ(1：有給, 0：無給)
    is_legal_holiday       integer DEFAULT 0,                 -- 法定休日フラグ(1：法定休日)
    is_prescribed_holiday  integer DEFAULT 0                  -- 所定休日フラグ(1：所定休日)
);

-- ----------------------------------------------------------------------------
-- 役職マスタ
-- ----------------------------------------------------------------------------
CREATE TABLE class_master (
    id    varchar(40) PRIMARY KEY,                            -- 役職区分
    name  varchar(40)                                         -- 役職名称
);

-- ----------------------------------------------------------------------------
-- 社員テーブル
-- ----------------------------------------------------------------------------
CREATE TABLE staff_table (
    id               integer PRIMARY KEY,                     -- 社員ID
    name             varchar(40) NOT NULL,                    -- 社員名
    namekana         varchar(40),                             -- 社員名カナ
    entry_year       integer NOT NULL,                        -- 入社年
    gender           varchar(10),                             -- 性別
    class_id         varchar(40) NOT NULL REFERENCES class_master (id) ON UPDATE CASCADE, -- 役職区分
    paid_leave_days  integer DEFAULT 20                       -- 有給休暇残日数
);
CREATE INDEX idx_staff_table_class_id ON staff_table (class_id);

-- ----------------------------------------------------------------------------
-- 社員ログインテーブル
-- username は社員ID（staff_table.id）を文字列にした値。
-- 型が違うため外部キーは張らない（照合SQLでは CAST(staff_table.id AS varchar) で結合する）
-- ----------------------------------------------------------------------------
CREATE TABLE login_table (
    username         varchar(50) PRIMARY KEY,                 -- 社員ID
    password         varchar(255) NOT NULL,                   -- PBKDF2 ハッシュ（pbkdf2_sha256$反復回数$ソルト$ハッシュ）。平文は保存しない
    enabled          integer NOT NULL DEFAULT 1,              -- 有効フラグ
    expiration_date  date                                     -- 有効期限
);

-- ----------------------------------------------------------------------------
-- 勤怠月テーブル（旧勤怠画面用。現在のアプリからは参照していない）
-- ----------------------------------------------------------------------------
CREATE TABLE work_month_table (
    staff_id                integer NOT NULL REFERENCES staff_table (id) ON UPDATE CASCADE, -- 社員ID
    work_date               date NOT NULL,                    -- 日付
    work_week               varchar(100),                     -- 曜日
    correction_id           integer,                          -- 稼働補正テンプレートCD
    job_from_time           timestamp,                        -- 勤怠FROM時間
    job_to_time             timestamp,                        -- 勤怠TO時間
    correction_us_time      integer,                          -- 稼働時間補正（通常）
    correction_mid_time     integer,                          -- 稼働時間補正（深夜）
    indirect_time           integer,                          -- 間接時間
    total_work_time         integer,                          -- 総稼働時間
    total_direct_work_time  integer,                          -- 総稼働時間（直接）
    overtime                integer,                          -- 残業時間
    abstract_id             integer REFERENCES abstract_master (id) ON UPDATE CASCADE, -- 摘要区分
    remarks                 varchar(200),                     -- 備考
    PRIMARY KEY (staff_id, work_date)
);
CREATE INDEX idx_work_month_table_work_date ON work_month_table (work_date);
CREATE INDEX idx_work_month_table_abstract_id ON work_month_table (abstract_id);

-- ----------------------------------------------------------------------------
-- 有給付与ルールテーブル
-- ----------------------------------------------------------------------------
CREATE TABLE leave_grant_rules (
    id                integer GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, -- ルールID
    years_of_service  integer NOT NULL UNIQUE,                -- 勤続年数
    grant_days        integer NOT NULL                        -- 付与日数
);

-- ----------------------------------------------------------------------------
-- 案件契約テーブル
-- ----------------------------------------------------------------------------
CREATE TABLE project_contracts (
    id                         varchar(64) PRIMARY KEY,
    client_name                varchar(128) NOT NULL,
    billing_lower_limit_hours  numeric(5, 2) NOT NULL,
    billing_upper_limit_hours  numeric(5, 2) NOT NULL,
    default_start_time         time NOT NULL DEFAULT '09:00:00',
    default_end_time           time NOT NULL DEFAULT '18:00:00',
    default_break_minutes      integer NOT NULL DEFAULT 60,
    created_at                 timestamp DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 案件アサインテーブル
-- ----------------------------------------------------------------------------
CREATE TABLE assignments (
    id                   varchar(64) PRIMARY KEY,
    employee_id          varchar(64) NOT NULL,
    project_contract_id  varchar(64) NOT NULL REFERENCES project_contracts (id),
    start_date           date NOT NULL,
    end_date             date,
    allocation_ratio     numeric(3, 2) NOT NULL DEFAULT 1.00,
    created_at           timestamp DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 日次勤怠テーブル
-- ----------------------------------------------------------------------------
CREATE TABLE daily_attendances (
    id             varchar(64) PRIMARY KEY,                   -- 社員ID_日付
    employee_id    varchar(64) NOT NULL,
    work_date      date NOT NULL,
    start_time     time DEFAULT '09:00:00',
    end_time       time DEFAULT '18:00:00',
    break_minutes  integer NOT NULL DEFAULT 60,
    abstract_id    integer DEFAULT 1,
    leave_type     varchar(32) NOT NULL DEFAULT 'NORMAL',
    remarks        text,
    is_filled      boolean NOT NULL DEFAULT FALSE,
    is_locked      boolean NOT NULL DEFAULT FALSE,
    created_at     timestamp DEFAULT CURRENT_TIMESTAMP,
    updated_at     timestamp DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (employee_id, work_date)
);

-- ----------------------------------------------------------------------------
-- 作業明細セグメントテーブル
-- ----------------------------------------------------------------------------
CREATE TABLE work_time_segments (
    id                   varchar(64) PRIMARY KEY,
    daily_attendance_id  varchar(64) NOT NULL REFERENCES daily_attendances (id) ON DELETE CASCADE,
    category             varchar(16) NOT NULL,                -- 'DIRECT' or 'INDIRECT'
    duration_minutes     integer NOT NULL,
    task_description     text,
    created_at           timestamp DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 週報テーブル
-- ----------------------------------------------------------------------------
CREATE TABLE weekly_reports (
    id                   varchar(64) PRIMARY KEY,
    employee_id          varchar(64) NOT NULL,
    year_week            varchar(16) NOT NULL,                -- 例: 2026-W41
    task_title           text,                                -- 作業件名、工程名・作業名
    task_progress        text,                                -- 作業進捗、作業内容と状況
    personal_reflection  text,                                -- 本人所感
    progress_summary     text,
    next_week_plan       text,
    workload_score       integer NOT NULL DEFAULT 3,
    health_score         integer NOT NULL DEFAULT 3,
    escalation_needed    boolean NOT NULL DEFAULT FALSE,
    status               varchar(32) NOT NULL DEFAULT 'DRAFT',
    submitted_at         timestamp,
    created_at           timestamp DEFAULT CURRENT_TIMESTAMP,
    updated_at           timestamp DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (employee_id, year_week)
);

-- ----------------------------------------------------------------------------
-- 週報レビューコメント履歴
-- ----------------------------------------------------------------------------
CREATE TABLE weekly_report_comments (
    id                  varchar(64) PRIMARY KEY,
    weekly_report_id    varchar(64) NOT NULL REFERENCES weekly_reports (id) ON DELETE CASCADE,
    author_id           varchar(64) NOT NULL,
    author_role         varchar(32) NOT NULL,
    comment             text NOT NULL,
    is_private_to_mgmt  boolean NOT NULL DEFAULT FALSE,
    created_at          timestamp DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 月次サマリテーブル
-- ----------------------------------------------------------------------------
CREATE TABLE monthly_summaries (
    id                      varchar(64) PRIMARY KEY,          -- 社員ID_年月
    employee_id             varchar(64) NOT NULL,
    year_month              varchar(16) NOT NULL,             -- 例: 2026-10
    assignment_id           varchar(64) NOT NULL,
    total_work_minutes      integer NOT NULL DEFAULT 0,
    direct_minutes          integer NOT NULL DEFAULT 0,
    indirect_minutes        integer NOT NULL DEFAULT 0,
    legal_overtime_minutes  integer NOT NULL DEFAULT 0,
    settlement_status       varchar(32) NOT NULL DEFAULT 'WITHIN_RANGE',
    status                  varchar(32) NOT NULL DEFAULT 'DRAFT',
    created_at              timestamp DEFAULT CURRENT_TIMESTAMP,
    updated_at              timestamp DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (employee_id, year_month)
);

-- ----------------------------------------------------------------------------
-- 月次勤怠集計ビュー（work_month_table を集計。現在のアプリからは参照していない）
-- ----------------------------------------------------------------------------
CREATE VIEW view_work_month_summary AS
SELECT wm.staff_id,
    st.name AS staff_name,
    st.class_id,
    to_char(wm.work_date, 'YYYY-MM') AS work_month,
    -- 1. 日数集計
    COUNT(wm.work_date) AS total_record_days,
    SUM(CASE WHEN am.is_work = 1 THEN 1 ELSE 0 END) AS work_days,
    SUM(CASE WHEN am.is_paid = 1 THEN 1 ELSE 0 END) AS paid_leave_days,
    SUM(CASE WHEN am.id = 9 THEN 1 ELSE 0 END) AS absence_days,
    -- 2. 時間集計（分単位）
    COALESCE(SUM(wm.total_work_time), 0) AS total_work_minutes,
    -- 3. 残業時間（法定外残業）: 平日 かつ 労働日 かつ 8時間(480分)超
    SUM(
        CASE
            WHEN am.is_legal_holiday = 0
            AND am.is_prescribed_holiday = 0
            AND am.is_work = 1
            AND wm.total_work_time > 480 THEN wm.total_work_time - 480
            ELSE 0
        END
    ) AS overtime_minutes,
    -- 4. 休日労働時間（所定休日）
    SUM(CASE WHEN am.is_prescribed_holiday = 1 AND am.is_work = 1 THEN wm.total_work_time ELSE 0 END) AS prescribed_holiday_work_minutes,
    -- 5. 法定休日労働時間
    SUM(CASE WHEN am.is_legal_holiday = 1 AND am.is_work = 1 THEN wm.total_work_time ELSE 0 END) AS legal_holiday_work_minutes,
    -- 6. 深夜時間
    COALESCE(SUM(wm.correction_mid_time), 0) AS night_work_minutes
FROM work_month_table wm
    INNER JOIN staff_table st ON wm.staff_id = st.id
    LEFT JOIN abstract_master am ON wm.abstract_id = am.id
GROUP BY wm.staff_id,
    st.name,
    st.class_id,
    to_char(wm.work_date, 'YYYY-MM');
