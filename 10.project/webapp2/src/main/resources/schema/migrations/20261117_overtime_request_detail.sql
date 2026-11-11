-- ============================================================================
-- 残業事前申請に PJ名と摘要（①自責〜⑤その他）を持たせる
--   個人別時間外勤務申請書（Excel）の「PJ名」「摘要」欄に出す。申請画面で入力する。
-- 何度実行しても安全。DO ブロックは使わない。
-- ============================================================================

ALTER TABLE overtime_request ADD COLUMN IF NOT EXISTS project_name varchar(100);
ALTER TABLE overtime_request ADD COLUMN IF NOT EXISTS category varchar(10);
COMMENT ON COLUMN overtime_request.project_name IS 'PJ名（時間外申請書の PJ名欄）';
COMMENT ON COLUMN overtime_request.category IS '摘要（①自責 / ②他責 / ③緊急 / ④客先都合 / ⑤その他）';
