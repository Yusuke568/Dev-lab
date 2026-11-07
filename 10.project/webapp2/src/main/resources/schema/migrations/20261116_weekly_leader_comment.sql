-- ============================================================================
-- 週次作業報告にリーダー所見を持たせる
--   作業報告書（Excel）の取り込みで AU列のリーダー所見を入れる。勤怠画面では表示だけで編集はできない。
-- 何度実行しても安全。DO ブロックは使わない。
-- ============================================================================

ALTER TABLE weekly_work_report ADD COLUMN IF NOT EXISTS leader_comment text;
COMMENT ON COLUMN weekly_work_report.leader_comment IS 'リーダー所見（作業報告書の AU列から取り込む）';
