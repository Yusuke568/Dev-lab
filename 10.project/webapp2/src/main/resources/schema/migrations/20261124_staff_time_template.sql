-- ============================================================================
-- 社員ごとの「自分の時刻テンプレート」（出勤・退勤の組を1つ）
--   勤怠画面の日ごとの入力ポップアップで本人が登録する。「9:00〜18:00」の隣にボタンとして出し、
--   押すと出勤・退勤にこの時刻を入れる。退勤が出勤以前なら翌日の退勤とみなす（表の入力と同じ）。
--   両方そろっているときだけ使う（片方だけの状態は作らない）。
-- 何度実行しても安全。DO ブロックは使わない。war を入れ替える前に流すこと。
-- ============================================================================

ALTER TABLE staff_table ADD COLUMN IF NOT EXISTS template_start_time time;
ALTER TABLE staff_table ADD COLUMN IF NOT EXISTS template_end_time time;
COMMENT ON COLUMN staff_table.template_start_time IS '自分の時刻テンプレートの出勤（勤怠画面で本人が登録）';
COMMENT ON COLUMN staff_table.template_end_time IS '自分の時刻テンプレートの退勤（出勤以前なら翌日）';
