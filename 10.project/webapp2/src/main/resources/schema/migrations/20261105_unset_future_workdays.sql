-- ============================================================================
-- 未到来の平日に自動で入っていた勤務区分「出勤」を、未設定（NULL）に戻す（1回だけ実行）
--
-- これまでは月の勤怠表を開くと、その月の全日を自動作成し、平日には「出勤」を入れていた。
-- 今後は未到来の平日は未設定で作るので、すでに作られている未到来の日も同じ状態にそろえる。
-- 対象は、今日より後の日付で、勤務区分が「出勤」、出勤・退勤・備考・補正がどれも入っていない、
-- 一時保存（承認状態0）のままの行だけ。本人が入力した日は変えない。
-- ============================================================================

UPDATE work_month_table
SET abstract_id = NULL
WHERE work_date > CURRENT_DATE
  AND abstract_id = (SELECT id FROM abstract_master WHERE name = '出勤')
  AND job_from_time IS NULL
  AND job_to_time IS NULL
  AND (remarks IS NULL OR remarks = '')
  AND correction_id IS NULL
  AND approval_status = 0;
