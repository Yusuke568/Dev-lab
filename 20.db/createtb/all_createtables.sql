-- --------------------------------------------------------
-- テーブル削除
-- --------------------------------------------------------
DROP TABLE IF EXISTS abstract_master;
DROP TABLE IF EXISTS login_table;
DROP TABLE IF EXISTS work_month_table;
DROP TABLE IF EXISTS staff_table;
DROP TABLE IF EXISTS class_master;
DROP TABLE IF EXISTS leave_grant_rules;
-- --------------------------------------------------------
-- 適用マスタ (abstract_master) 作成
-- --------------------------------------------------------
CREATE TABLE abstract_master (
  ID int NOT NULL COMMENT '適用区分',
  NAME varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '適用名称',
  -- 集計用フラグカラム（新規追加）
  IS_WORK tinyint(1) DEFAULT 0 COMMENT '労働日フラグ(1：労働, 0：休み)',
  IS_PAID tinyint(1) DEFAULT 0 COMMENT '有給対象フラグ(1：有給, 0：無給)',
  IS_LEGAL_HOLIDAY tinyint(1) DEFAULT 0 COMMENT '法定休日フラグ(1：法定休日)',
  IS_PRESCRIBED_HOLIDAY tinyint(1) DEFAULT 0 COMMENT '所定休日フラグ(1：所定休日)',
  PRIMARY KEY (ID)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '適用マスタ';
-- --------------------------------------------------------
-- 役職マスタ (class_master) 作成
-- --------------------------------------------------------
CREATE TABLE `class_master` (
  `ID` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '役職区分',
  `NAME` varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '役職名称',
  PRIMARY KEY (`ID`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '役職マスタ';
-- --------------------------------------------------------
-- 社員テーブル (staff_table) 作成
-- --------------------------------------------------------
CREATE TABLE `staff_table` (
  `ID` int NOT NULL COMMENT '社員ID',
  `NAME` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '社員名',
  `NAMEKANA` varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '社員名カナ',
  `ENTRY_YEAR` int NOT NULL COMMENT '入社年',
  `GENDER` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '性別',
  `CLASS_ID` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '役職区分',
  `PAID_LEAVE_DAYS` int DEFAULT 20 COMMENT '有給休暇残日数',
  PRIMARY KEY (`ID`),
  KEY `CLASS_ID` (`CLASS_ID`),
  CONSTRAINT `staff_table_ibfk_1` FOREIGN KEY (`CLASS_ID`) REFERENCES `class_master` (`ID`) ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '社員テーブル';
-- --------------------------------------------------------
-- 社員ログインテーブル (login_table) 作成
-- --------------------------------------------------------
CREATE TABLE `login_table` (
  `USERNAME` int NOT NULL COMMENT '社員ID',
  `PASSWORD` varchar(500) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'パスワード',
  `ENABLED` tinyint(1) NOT NULL COMMENT '有効フラグ',
  `EXPIRATION_DATE` date DEFAULT NULL COMMENT '有効期限',
  PRIMARY KEY (`USERNAME`),
  CONSTRAINT `login_table_ibfk_1` FOREIGN KEY (`USERNAME`) REFERENCES `staff_table` (`ID`) ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '社員ログインテーブル';
-- -- --------------------------------------------------------
-- -- 勤怠月テーブル (work_month_table) 作成
-- -- --------------------------------------------------------
-- CREATE TABLE `work_month_table` (
--   `STAFF_ID` int NOT NULL COMMENT '社員ID',
--   `WORK_DATE` date NOT NULL COMMENT '日付',
--   `WORK_WEEK` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '曜日',
--   `CORRECTION_ID` int DEFAULT NULL COMMENT '稼働補正テンプレートCD',
--   `JOB_FROM_TIME` datetime DEFAULT NULL COMMENT '勤怠FROM時間',
--   `JOB_TO_TIME` datetime DEFAULT NULL COMMENT '勤怠TO時間',
--   `CORRECTION_US_TIME` int DEFAULT NULL COMMENT '稼働時間補正（通常）',
--   `CORRECTION_MID_TIME` int DEFAULT NULL COMMENT '稼働時間補正（深夜）',
--   `INDIRECT_TIME` int DEFAULT NULL COMMENT '間接時間',
--   `TOTAL_WORK_TIME` int DEFAULT NULL COMMENT '総稼働時間',
--   `TOTAL_DIRECT_WORK_TIME` int DEFAULT NULL COMMENT '総稼働時間（直接）',
--   `OVERTIME` int DEFAULT NULL COMMENT '残業時間',
--   `ABSTRACT_ID` int DEFAULT NULL COMMENT '摘要区分',
--   `REMARKS` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '備考',
--   PRIMARY KEY (`STAFF_ID`, `WORK_DATE`),
--   KEY `idx_work_date` (`WORK_DATE`),
--   KEY `idx_abstract_id` (`ABSTRACT_ID`),
--   CONSTRAINT `work_month_table_ibfk_1` FOREIGN KEY (`STAFF_ID`) REFERENCES `staff_table` (`ID`) ON UPDATE CASCADE,
--   CONSTRAINT `work_month_table_ibfk_2` FOREIGN KEY (`ABSTRACT_ID`) REFERENCES `abstract_master` (`ID`) ON UPDATE CASCADE
-- ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '勤怠月テーブル';

-- --------------------------------------------------------
-- 適用マスタ (ktmt_abstract) 作成
-- --------------------------------------------------------
CREATE TABLE ktmt_abstract (
  ID int NOT NULL COMMENT '適用区分',
  NAME varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '適用名称',
  -- 集計用フラグカラム（新規追加）
  IS_WORK tinyint(1) DEFAULT 0 COMMENT '労働日フラグ(1：労働, 0：休み)',
  IS_PAID tinyint(1) DEFAULT 0 COMMENT '有給対象フラグ(1：有給, 0：無給)',
  IS_LEGAL_HOLIDAY tinyint(1) DEFAULT 0 COMMENT '法定休日フラグ(1：法定休日)',
  IS_PRESCRIBED_HOLIDAY tinyint(1) DEFAULT 0 COMMENT '所定休日フラグ(1：所定休日)',
  PRIMARY KEY (ID)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '適用マスタ';
-- --------------------------------------------------------
-- 役職マスタ (aatm_jobclass) 作成
-- --------------------------------------------------------
DROP TABLE IF EXISTS aatm_jobclass;
CREATE TABLE `aatm_jobclass` (
  `ID` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '役職区分',
  `NAME` varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '役職名称',
  `CREATESTAFF` varchar(256) COMMENT '作成者',
  `CREATEDAY` timestamp COMMENT '作成日',
  PRIMARY KEY (`ID`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '役職マスタ';

-- --------------------------------------------------------
-- 稼働補正テンプレートマスタ (kttm_adjustment) 作成
-- --------------------------------------------------------
DROP TABLE IF EXISTS kttm_adjustment;
CREATE TABLE `kttm_adjustment` (
  `ADJUSTMENT_ID` int NOT NULL COMMENT '稼働補正ID',
  `ADJUSTMENT_NAME` verchar(256) NOT NULL COMMENT '稼働補正名前',
  `CORRECTION_US_TIME` int DEFAULT NULL COMMENT '稼働時間補正（通常）',
  `CORRECTION_MID_TIME` int DEFAULT NULL COMMENT '稼働時間補正（深夜）',
  `CREATESTAFF` varchar(256) COMMENT '作成者',
  `CREATEDAY` timestamp COMMENT '作成日',
  PRIMARY KEY (`ADJUSTMENT_ID`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '稼働補正テンプレートマスタ';

-- --------------------------------------------------------
-- 社員テーブル (aatb_staff) 作成
-- --------------------------------------------------------
CREATE TABLE `aatb_staff` (
  `ID` int NOT NULL COMMENT '社員ID',
  `NAME` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '社員名',
  `NAMEKANA` varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '社員名カナ',
  `ENTRY_YEAR` int NOT NULL COMMENT '入社年',
  `GENDER` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '性別',
  `CLASS_ID` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '役職区分',
  `PAID_LEAVE_DAYS` int DEFAULT 20 COMMENT '有給休暇残日数',
  PRIMARY KEY (`ID`),
  KEY `CLASS_ID` (`CLASS_ID`),
  CONSTRAINT `staff_table_ibfk_1` FOREIGN KEY (`CLASS_ID`) REFERENCES `class_master` (`ID`) ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '社員テーブル';

-- --------------------------------------------------------
-- 勤怠月テーブル (kttb_work_month) 作成
-- --------------------------------------------------------
CREATE TABLE `kttb_work_month` (
  `STAFF_ID` int NOT NULL COMMENT '社員ID',
  `WORK_DATE` date NOT NULL COMMENT '日付',
  `WORK_WEEK` varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '曜日',
  `CORRECTION_ID` int DEFAULT NULL COMMENT '稼働補正テンプレートCD',
  `JOB_FROM_TIME` datetime DEFAULT NULL COMMENT '勤怠FROM時間',
  `JOB_TO_TIME` datetime DEFAULT NULL COMMENT '勤怠TO時間',
  `CORRECTION_US_TIME` int DEFAULT NULL COMMENT '稼働時間補正（通常）',
  `CORRECTION_MID_TIME` int DEFAULT NULL COMMENT '稼働時間補正（深夜）',
  `INDIRECT_TIME` int DEFAULT NULL COMMENT '間接時間',
  `TOTAL_WORK_TIME` int DEFAULT NULL COMMENT '総稼働時間',
  `TOTAL_DIRECT_WORK_TIME` int DEFAULT NULL COMMENT '総稼働時間（直接）',
  `OVERTIME` int DEFAULT NULL COMMENT '残業時間',
  `ABSTRACT_ID` int DEFAULT NULL COMMENT '摘要区分',
  `REMARKS` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '備考',
  PRIMARY KEY (`STAFF_ID`, `WORK_DATE`),
  KEY `idx_work_date` (`WORK_DATE`),
  KEY `idx_abstract_id` (`ABSTRACT_ID`),
  KEY `idx_correction_id` (`CORRECTION_ID`),
  CONSTRAINT `work_month_table_ibfk_1` FOREIGN KEY (`STAFF_ID`) REFERENCES `aatb_staff` (`ID`) ON UPDATE CASCADE,
  CONSTRAINT `work_month_table_ibfk_2` FOREIGN KEY (`ABSTRACT_ID`) REFERENCES `ktmt_abstract` (`ID`) ON UPDATE CASCADE
  CONSTRAINT `work_month_table_ibfk_3` FOREIGN KEY (`CORRECTION_ID`) REFERENCES `kttm_adjustment` (`ADJUSTMENT_ID`) ON UPDATE CASCADE

) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '勤怠月テーブル';

-- --------------------------------------------------------
-- 時間外申請テーブル (kttb_overtime_request) 作成
-- --------------------------------------------------------
DROP TABLE IF EXISTS kttb_overtime_request;
CREATE TABLE `kttb_overtime_request` (
  `ID` int NOT NULL AUTO_INCREMENT COMMENT '社員ID',
  `OUTTIME_ID` int NOT NULL COMMENT '時間外申請ID',
  `KADOUHOSEI_CD` int NOT NULL COMMENT '稼働補正CD',
  `APPROVE_ID` int NOT NULL COMMENT '承認ルートID',
  `DAY` timestamp NOT NULL COMMENT '年月日',
  `PROJECT_CD` int NOT NULL COMMENT 'プロジェクトコード',
  `OUTTIME_FROM` int NOT NULL COMMENT '時間外開始',
  `OUTTIME_TO` timestamp NOT NULL COMMENT '時間外終了',
  `CORRECTION_US_TIME` int COMMENT '稼働補正（通常）',
  `CORRECTION_MID_TIME` int COMMENT '稼働補正（時間外）',
  `REMARKS` varchar(256) COMMENT '備考',
  `CREATESTAFF` varchar(256) COMMENT '作成者',
  `CREATEDAY` timestamp COMMENT '作成日',
  PRIMARY KEY (`ID`,`OUTTIME_ID`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '時間外申請テーブル';

-- --------------------------------------------------------
-- 承認ルートマスタ (ktmt_approve_root) 作成
-- --------------------------------------------------------
DROP TABLE IF EXISTS ktmt_approve_root;
CREATE TABLE `ktmt_approve_root`(
  `APPROVE_ID` int NOT NULL COMMENT '承認ルートID',
  `NO` int COMMENT '項番',
  `STAFF_ID` int COMMENT '社員ID',
  `APPROVE_STATE` varchar(32) COMMENT '承認状態',
  `NEXT_NO` int COMMENT '次回項番',
  `REMARKS` varchar(256) COMMENT '備考',
  `CREATESTAFF` varchar(256) NOT NULL COMMENT '作成者',
  `CREATEDAY` timestamp NOT NULL COMMENT '作成日',
  PRIMARY KEY (`APPROVE_ID`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '承認ルートマスタ';

-- --------------------------------------------------------
-- プロジェクトコードマスタ (ktmt_projectcd) 作成
-- --------------------------------------------------------
DROP TABLE IF EXISTS ktmt_projectcd;
CREATE TABLE `ktmt_projectcd`(
  `PROJECT_CD` int NOT NULL COMMENT 'プロジェクトコード',
  `PROJECT_NAME` varchar(256) NOT NULL COMMENT 'プロジェクト名称',
  `CREATESTAFF` varchar(256) NOT NULL COMMENT '作成者',
  `CREATEDAY` timestamp NOT NULL COMMENT '作成日',
  PRIMARY KEY (`PROJECT_CD`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'プロジェクトコードマスタ';

-- --------------------------------------------------------
-- 有給付与ルールテーブル (leave_grant_rules) 作成 /*プロシージぁでしたほうがいいかも？*/
-- --------------------------------------------------------
CREATE TABLE `leave_grant_rules` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ルールID',
  `years_of_service` int NOT NULL COMMENT '勤続年数',
  `grant_days` int NOT NULL COMMENT '付与日数',
  PRIMARY KEY (`id`),
  UNIQUE KEY `years_of_service` (`years_of_service`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '有給付与ルールテーブル';