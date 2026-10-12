-- SES勤怠管理システム 新規テーブル定義
-- MySQL / PostgreSQL 互換

-- 1. 案件契約テーブル
CREATE TABLE IF NOT EXISTS project_contracts (
    id VARCHAR(64) PRIMARY KEY,
    client_name VARCHAR(128) NOT NULL,
    billing_lower_limit_hours DECIMAL(5, 2) NOT NULL,
    billing_upper_limit_hours DECIMAL(5, 2) NOT NULL,
    default_start_time TIME NOT NULL DEFAULT '09:00:00',
    default_end_time TIME NOT NULL DEFAULT '18:00:00',
    default_break_minutes INT NOT NULL DEFAULT 60,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 案件アサインテーブル
CREATE TABLE IF NOT EXISTS assignments (
    id VARCHAR(64) PRIMARY KEY,
    employee_id VARCHAR(64) NOT NULL,
    project_contract_id VARCHAR(64) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    allocation_ratio DECIMAL(3, 2) NOT NULL DEFAULT 1.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_contract_id) REFERENCES project_contracts(id)
);

-- 3. 日次勤怠テーブル
CREATE TABLE IF NOT EXISTS daily_attendances (
    id VARCHAR(64) PRIMARY KEY,
    employee_id VARCHAR(64) NOT NULL,
    work_date DATE NOT NULL,
    start_time TIME DEFAULT '09:00:00',
    end_time TIME DEFAULT '18:00:00',
    break_minutes INT NOT NULL DEFAULT 60,
    abstract_id INT DEFAULT 1,
    leave_type VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    remarks TEXT,
    is_filled BOOLEAN NOT NULL DEFAULT FALSE,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (employee_id, work_date)
);

-- 4. 作業明細セグメントテーブル
CREATE TABLE IF NOT EXISTS work_time_segments (
    id VARCHAR(64) PRIMARY KEY,
    daily_attendance_id VARCHAR(64) NOT NULL,
    category VARCHAR(16) NOT NULL, -- 'DIRECT' or 'INDIRECT'
    duration_minutes INT NOT NULL,
    task_description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (daily_attendance_id) REFERENCES daily_attendances(id) ON DELETE CASCADE
);

-- 5. 週報テーブル
CREATE TABLE IF NOT EXISTS weekly_reports (
    id VARCHAR(64) PRIMARY KEY,
    employee_id VARCHAR(64) NOT NULL,
    year_week VARCHAR(16) NOT NULL, -- 例: 2026-W41
    task_title TEXT, -- 作業件名、工程名・作業名
    task_progress TEXT, -- 作業進捗、作業内容と状況
    personal_reflection TEXT, -- 本人所感
    progress_summary TEXT,
    next_week_plan TEXT,
    workload_score INT NOT NULL DEFAULT 3,
    health_score INT NOT NULL DEFAULT 3,
    escalation_needed BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    submitted_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (employee_id, year_week)
);

-- 6. 週報レビューコメント履歴
CREATE TABLE IF NOT EXISTS weekly_report_comments (
    id VARCHAR(64) PRIMARY KEY,
    weekly_report_id VARCHAR(64) NOT NULL,
    author_id VARCHAR(64) NOT NULL,
    author_role VARCHAR(32) NOT NULL,
    comment TEXT NOT NULL,
    is_private_to_mgmt BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (weekly_report_id) REFERENCES weekly_reports(id) ON DELETE CASCADE
);

-- 7. 月次サマリテーブル
CREATE TABLE IF NOT EXISTS monthly_summaries (
    id VARCHAR(64) PRIMARY KEY,
    employee_id VARCHAR(64) NOT NULL,
    `year_month` VARCHAR(16) NOT NULL, -- 例: 2026-10
    assignment_id VARCHAR(64) NOT NULL,
    total_work_minutes INT NOT NULL DEFAULT 0,
    direct_minutes INT NOT NULL DEFAULT 0,
    indirect_minutes INT NOT NULL DEFAULT 0,
    legal_overtime_minutes INT NOT NULL DEFAULT 0,
    settlement_status VARCHAR(32) NOT NULL DEFAULT 'WITHIN_RANGE',
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (employee_id, `year_month`)
);
