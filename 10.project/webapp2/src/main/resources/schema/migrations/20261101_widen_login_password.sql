-- ============================================================================
-- login_table.password をハッシュ保存用に広げる（varchar(50) -> varchar(255)）
--
-- 注意: このSQLは列を広げるだけで、既存の平文パスワードはハッシュに変換しない。
-- アプリのデプロイと同時に、全ユーザーのパスワードを PasswordHasher で作った
-- ハッシュに UPDATE すること（手順: 10.project/webapp2/docs/password-hashing.md）。
-- ============================================================================

ALTER TABLE login_table ALTER COLUMN password TYPE varchar(255);
