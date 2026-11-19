-- 勤怠の承認の運用開始日（マスタ管理の承認設定で変える）
--   この日より前の勤怠は、有給や時間外があっても承認待ちにしない（作業報告書を取り込み直しても承認済みのまま）
-- 何度実行しても安全
CREATE TABLE IF NOT EXISTS app_setting (
    setting_key    varchar(50) PRIMARY KEY,
    setting_value  varchar(255)
);
COMMENT ON TABLE app_setting IS 'アプリ全体の設定（キーと値）';

-- 初期値：2026年11月から承認の運用を始めた（10月以前は導入時に SQL でまとめて承認済み）
INSERT INTO app_setting (setting_key, setting_value)
VALUES ('ATTENDANCE_APPROVAL_START_DATE', '2026-11-01')
ON CONFLICT (setting_key) DO NOTHING;
