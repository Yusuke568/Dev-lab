-- ============================================================================
-- 承認・却下・確認のときの理由（任意）
--   残業事前申請 … approval_step.comment（列はもうある）
--   残業の実績確認 … overtime_actual_step.comment
--   勤怠（有給・事前申請のない時間外）… kintai_approval_history.comment
-- 何度実行しても安全。DO ブロックは使わない。
-- ============================================================================

ALTER TABLE approval_step ADD COLUMN IF NOT EXISTS comment varchar(255);
ALTER TABLE overtime_actual_step ADD COLUMN IF NOT EXISTS comment varchar(255);
ALTER TABLE kintai_approval_history ADD COLUMN IF NOT EXISTS comment varchar(255);
COMMENT ON COLUMN overtime_actual_step.comment IS '実績を確認したときのコメント（任意）';
COMMENT ON COLUMN kintai_approval_history.comment IS '承認・却下の理由（任意）';
