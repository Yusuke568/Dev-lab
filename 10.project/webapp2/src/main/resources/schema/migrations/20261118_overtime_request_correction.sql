-- ============================================================================
-- 残業事前申請に補正（通常・深夜）を持たせる
--   申請の時間外 ＝ 予定の出退勤から出した時間 ＋ 補正（通常）＋ 補正（深夜）。
--   事前の申請は申請画面で入力し、勤怠の実績から申請するときは勤怠の補正を使う。
-- 何度実行しても安全。DO ブロックは使わない。
-- ============================================================================

ALTER TABLE overtime_request ADD COLUMN IF NOT EXISTS correction_minutes integer;
ALTER TABLE overtime_request ADD COLUMN IF NOT EXISTS correction_mid_minutes integer;
COMMENT ON COLUMN overtime_request.correction_minutes IS '補正（通常）の分。申請の時間外に含める';
COMMENT ON COLUMN overtime_request.correction_mid_minutes IS '補正（深夜）の分。申請の時間外に含める';
