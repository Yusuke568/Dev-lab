-- 作業報告書のファイル名の形（マスタ管理の「作業報告書の設定」で変える）
--   {年} {月} {氏名} {社員ID} を差し込める。拡張子 .xlsm は自動で付く
--   app_setting は 20261125_attendance_approval_start.sql で作る（先に流す）
-- 何度実行しても安全（すでに決めてあれば変えない）
INSERT INTO app_setting (setting_key, setting_value)
VALUES ('WORK_REPORT_FILE_NAME', '作業報告書{年}（{氏名}）_41')
ON CONFLICT (setting_key) DO NOTHING;
