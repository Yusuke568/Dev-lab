CREATE TABLE `abstract_master` (
  `ID` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '適用区分',
  `NAME` varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '適用名称',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='適用マスタ'

CREATE TABLE `class_master` (
  `ID` varchar(40) COLLATE utf8mb4_general_ci NOT NULL COMMENT '役職区分',
  `NAME` varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '役職名称',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='役職マスタ'

CREATE TABLE `login_table` (
  `USERNAME` int NOT NULL COMMENT '社員ID',
  `PASSWORD` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'パスワード',
  `ENABLED` tinyint(1) DEFAULT NULL COMMENT '有効フラグ',
  `EXPIRATION_DATE` date DEFAULT NULL COMMENT '有効期限',
  PRIMARY KEY (`USERNAME`),
  CONSTRAINT `login_table_ibfk_1` FOREIGN KEY (`USERNAME`) REFERENCES `staff_table` (`ID`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='社員ログインテーブル'

CREATE TABLE `staff_table` (
  `ID` int NOT NULL COMMENT '社員ID',
  `NAME` varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '社員名',
  `NAMEKANA` varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '社員名カナ',
  `ENTRY_YEAR` int DEFAULT NULL COMMENT '入社年',
  `GENDER` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '性別',
  `CLASS_ID` varchar(40) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '役職区分',
  PRIMARY KEY (`ID`),
  KEY `CLASS_ID` (`CLASS_ID`),
  CONSTRAINT `staff_table_ibfk_1` FOREIGN KEY (`CLASS_ID`) REFERENCES `class_master` (`ID`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='社員テーブル'

CREATE TABLE `work_month_table` (
  `STAFF_ID` int NOT NULL COMMENT '社員ID',
  `WORK_DATE` date NOT NULL COMMENT '日付',
  `WORK_WEEK` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '曜日',
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
  PRIMARY KEY (`STAFF_ID`,`WORK_DATE`),
  CONSTRAINT `work_month_table_ibfk_1` FOREIGN KEY (`STAFF_ID`) REFERENCES `staff_table` (`ID`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='勤怠月テーブル'