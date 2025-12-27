-- --------------------------------------------------------
-- 月次勤怠集計ビューの作成
-- 日次テーブル(work_month_table)を集計し、残業・休日等の計算を行う
-- --------------------------------------------------------
DROP VIEW IF EXISTS view_work_month_summary;
CREATE VIEW view_work_month_summary AS
SELECT wm.STAFF_ID,
    st.NAME AS STAFF_NAME,
    st.CLASS_ID,
    DATE_FORMAT(wm.WORK_DATE, '%Y-%m') AS WORK_MONTH,
    -- 1. 日数集計
    COUNT(wm.WORK_DATE) AS TOTAL_RECORD_DAYS,
    -- 実働日数 (マスタのIS_WORKフラグが1の日)
    SUM(
        CASE
            WHEN am.IS_WORK = 1 THEN 1
            ELSE 0
        END
    ) AS WORK_DAYS,
    -- 有給取得日数 (IS_PAIDフラグが1の日)
    SUM(
        CASE
            WHEN am.IS_PAID = 1 THEN 1
            ELSE 0
        END
    ) AS PAID_LEAVE_DAYS,
    -- 欠勤日数 (便宜上、その他(ID=9)などをカウント。運用に合わせて調整可)
    SUM(
        CASE
            WHEN am.ID = '9' THEN 1
            ELSE 0
        END
    ) AS ABSENCE_DAYS,
    -- 2. 時間集計（分単位）
    -- 総労働時間
    IFNULL(SUM(wm.TOTAL_WORK_TIME), 0) AS TOTAL_WORK_MINUTES,
    -- 3. 残業時間（法定外残業）
    -- 条件：平日(休日フラグなし) かつ 労働日 かつ 8時間(480分)超
    SUM(
        CASE
            WHEN am.IS_LEGAL_HOLIDAY = 0
            AND am.IS_PRESCRIBED_HOLIDAY = 0
            AND am.IS_WORK = 1
            AND wm.TOTAL_WORK_TIME > 480 THEN wm.TOTAL_WORK_TIME - 480
            ELSE 0
        END
    ) AS OVERTIME_MINUTES,
    -- 4. 休日労働時間（所定休日）
    -- 条件：所定休日フラグあり かつ 労働日
    -- ※ここでは全時間を「休日労働」として計上
    SUM(
        CASE
            WHEN am.IS_PRESCRIBED_HOLIDAY = 1
            AND am.IS_WORK = 1 THEN wm.TOTAL_WORK_TIME
            ELSE 0
        END
    ) AS PRESCRIBED_HOLIDAY_WORK_MINUTES,
    -- 5. 法定休日労働時間
    -- 条件：法定休日フラグあり かつ 労働日
    SUM(
        CASE
            WHEN am.IS_LEGAL_HOLIDAY = 1
            AND am.IS_WORK = 1 THEN wm.TOTAL_WORK_TIME
            ELSE 0
        END
    ) AS LEGAL_HOLIDAY_WORK_MINUTES,
    -- 6. 深夜時間
    IFNULL(SUM(wm.CORRECTION_MID_TIME), 0) AS NIGHT_WORK_MINUTES
FROM work_month_table wm
    INNER JOIN staff_table st ON wm.STAFF_ID = st.ID
    LEFT JOIN abstract_master am ON wm.ABSTRACT_ID = am.ID
GROUP BY wm.STAFF_ID,
    st.NAME,
    st.CLASS_ID,
    DATE_FORMAT(wm.WORK_DATE, '%Y-%m');