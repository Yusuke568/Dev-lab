-- ============================================================================
-- 残業事前申請に「実績の確認日」と「ふだんの承認の段数」を持たせる
--   時間外申請書（Excel）の実績報告の承認欄に使う。
--     actual_confirmed_on … 勤怠を保存したとき、時間外の実績が申請の時間と合った（または時間外がなかった）日。
--                           合わなければ空にし、申請し直して承認された日（段ごと）を使う
--     base_levels         … 承認ルートのうち、条件つきで足した段（当月○時間以上・深夜）を除いた段数。
--                           それより後の段は条件つきの段で、承認されるまで実績の承認欄に日付を入れない
-- 何度実行しても安全。DO ブロックは使わない。
-- ============================================================================

ALTER TABLE overtime_request ADD COLUMN IF NOT EXISTS actual_confirmed_on date;
ALTER TABLE overtime_request ADD COLUMN IF NOT EXISTS base_levels integer;
COMMENT ON COLUMN overtime_request.actual_confirmed_on IS '実績の確認日（勤怠の時間外が申請と合った・時間外がなかった日）';
COMMENT ON COLUMN overtime_request.base_levels IS '承認ルートのうち条件つきの段を除いた段数';
