-- ============================================================================
-- まだ来ていない日の勤務区分「出勤」を未設定に戻す
--   以前は月の勤怠を開いたときに、平日をすべて「出勤」で作っていた。
--   いまは未到来の平日は未設定（NULL）で作るので、それより前に作られた分を揃える。
--   時刻・補正・間接・備考のどれかが入っている日は、本人が入力したものとして変えない。
-- 何度実行しても安全（実行した日より後の日だけが対象）。DO ブロックは使わない。
-- ============================================================================

UPDATE work_month_table
SET abstract_id = NULL
WHERE work_date > CURRENT_DATE
  AND abstract_id = 1
  AND job_from_time IS NULL
  AND job_to_time IS NULL
  AND correction_id IS NULL
  AND COALESCE(correction_us_time, 0) = 0
  AND COALESCE(correction_mid_time, 0) = 0
  AND COALESCE(indirect_time, 0) = 0
  AND COALESCE(remarks, '') = '';
