-- ============================================================================
-- 日曜（法定休日）の出勤を時間外にしないように直したときの、保存済みの勤怠の直し方
--
--   アプリを直したあとに保存した日曜は、時間外が 0 で保存される。
--   それより前に保存した日曜は時間外が入ったままなので、このSQLで直す。
--     ・日曜の勤怠の時間外（overtime）を 0 にする
--     ・時間外だけが理由で承認待ち（approval_status = 1）になっていた日曜を承認済み（2）にする
--       （有給・半休の日曜は、承認待ちのまま残す）
--   日曜の勤務が40時間を埋めていた週は、同じ週の土曜・祝日の時間外が増えることがある。
--   手順4の一覧の社員・週は、勤怠画面でその週を開いて保存し直す（週の時間外が計算し直される）。
--
-- 使い方（psql / pgAdmin など）
--   1. 手順1で、直す日曜を確かめる
--   2. 手順2を実行する（BEGIN〜COMMIT。件数がおかしければ COMMIT の代わりに ROLLBACK）
--   3. 手順3・4で、残っているものを確かめる
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 手順1：時間外が入っている日曜（読むだけ）
-- ---------------------------------------------------------------------------
SELECT w.staff_id, s.name, w.work_date, w.job_from_time, w.job_to_time, w.overtime, w.approval_status,
       a.name AS work_type
FROM work_month_table w
JOIN staff_table s ON s.id = w.staff_id
LEFT JOIN abstract_master a ON a.id = w.abstract_id
WHERE EXTRACT(DOW FROM w.work_date) = 0
  AND w.overtime > 0
ORDER BY w.staff_id, w.work_date;

-- ---------------------------------------------------------------------------
-- 手順2：日曜の時間外を 0 にし、時間外だけで承認待ちになっていた日曜を承認済みにする
-- ---------------------------------------------------------------------------
BEGIN;

UPDATE work_month_table w
SET approval_status = 2
WHERE EXTRACT(DOW FROM w.work_date) = 0
  AND w.overtime > 0
  AND w.approval_status = 1
  AND NOT EXISTS (SELECT 1 FROM abstract_master a WHERE a.id = w.abstract_id AND a.paid_leave_days > 0);
-- 「UPDATE n」：承認済みにした日曜の数

UPDATE work_month_table
SET overtime = 0
WHERE EXTRACT(DOW FROM work_date) = 0
  AND overtime > 0;
-- 「UPDATE n」：手順1の件数と同じなら COMMIT、違えば ROLLBACK

COMMIT;
-- ROLLBACK;

-- ---------------------------------------------------------------------------
-- 手順3：日曜の残業事前申請（読むだけ）。日曜は申請が要らないので、不要なら本人に取り下げてもらう
-- ---------------------------------------------------------------------------
SELECT r.id, r.staff_id, s.name, r.target_date, r.status, r.planned_overtime_minutes
FROM overtime_request r
JOIN staff_table s ON s.id = r.staff_id
WHERE EXTRACT(DOW FROM r.target_date) = 0
  AND r.status IN ('PENDING', 'APPROVED')
ORDER BY r.staff_id, r.target_date;

-- ---------------------------------------------------------------------------
-- 手順4：日曜に勤務があった週の、土曜・祝日など休日出勤の日（読むだけ）
--   この週は日曜の勤務が40時間を埋めていたため、土曜などの時間外が少なく保存されていることがある。
--   勤怠画面でその週を開き、保存し直すと計算し直される。
-- ---------------------------------------------------------------------------
SELECT h.staff_id, s.name, sun.work_date AS sunday, h.work_date, h.overtime
FROM work_month_table sun
JOIN work_month_table h ON h.staff_id = sun.staff_id
                       AND h.work_date BETWEEN sun.work_date + 1 AND sun.work_date + 6
                       AND h.job_from_time IS NOT NULL
                       AND EXTRACT(DOW FROM h.work_date) = 6
JOIN staff_table s ON s.id = h.staff_id
WHERE EXTRACT(DOW FROM sun.work_date) = 0
  AND sun.job_from_time IS NOT NULL
ORDER BY h.staff_id, h.work_date;
