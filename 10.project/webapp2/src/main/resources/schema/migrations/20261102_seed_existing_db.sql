-- ============================================================================
-- 既存の PostgreSQL DB（旧スキーマから作ったもの）に、サンプルの社員とログインを入れる
--
-- ・何度実行しても安全（登録済みの行は飛ばす）
-- ・ON CONFLICT / DO ブロックを使わないので、古い PostgreSQL や SQL クライアントでも流せる
-- ・ログインパスワードは全員 "password"（PBKDF2 ハッシュ）
-- ・フラグは '1' / '0' で書いているので、列が boolean でも integer でも入る
-- ============================================================================

-- 1. password 列をハッシュが入る長さに広げる（varchar(50) -> varchar(255)。データは消えない）
ALTER TABLE login_table ALTER COLUMN password TYPE varchar(255);

-- 2. 適用マスタ
INSERT INTO abstract_master (id, name, is_work, is_paid, is_legal_holiday, is_prescribed_holiday) SELECT 1, '出勤', '1', '0', '0', '0' WHERE NOT EXISTS (SELECT 1 FROM abstract_master WHERE id = 1);
INSERT INTO abstract_master (id, name, is_work, is_paid, is_legal_holiday, is_prescribed_holiday) SELECT 10, '休日', '0', '0', '1', '0' WHERE NOT EXISTS (SELECT 1 FROM abstract_master WHERE id = 10);
INSERT INTO abstract_master (id, name, is_work, is_paid, is_legal_holiday, is_prescribed_holiday) SELECT 2, '休出', '1', '0', '0', '1' WHERE NOT EXISTS (SELECT 1 FROM abstract_master WHERE id = 2);
INSERT INTO abstract_master (id, name, is_work, is_paid, is_legal_holiday, is_prescribed_holiday) SELECT 3, '有給', '0', '1', '0', '0' WHERE NOT EXISTS (SELECT 1 FROM abstract_master WHERE id = 3);
INSERT INTO abstract_master (id, name, is_work, is_paid, is_legal_holiday, is_prescribed_holiday) SELECT 4, '代休', '0', '0', '0', '0' WHERE NOT EXISTS (SELECT 1 FROM abstract_master WHERE id = 4);
INSERT INTO abstract_master (id, name, is_work, is_paid, is_legal_holiday, is_prescribed_holiday) SELECT 5, '半休', '1', '0', '0', '0' WHERE NOT EXISTS (SELECT 1 FROM abstract_master WHERE id = 5);
INSERT INTO abstract_master (id, name, is_work, is_paid, is_legal_holiday, is_prescribed_holiday) SELECT 6, '特休', '0', '0', '0', '0' WHERE NOT EXISTS (SELECT 1 FROM abstract_master WHERE id = 6);
INSERT INTO abstract_master (id, name, is_work, is_paid, is_legal_holiday, is_prescribed_holiday) SELECT 7, '振出', '1', '0', '0', '0' WHERE NOT EXISTS (SELECT 1 FROM abstract_master WHERE id = 7);
INSERT INTO abstract_master (id, name, is_work, is_paid, is_legal_holiday, is_prescribed_holiday) SELECT 8, '振休', '0', '0', '0', '0' WHERE NOT EXISTS (SELECT 1 FROM abstract_master WHERE id = 8);
INSERT INTO abstract_master (id, name, is_work, is_paid, is_legal_holiday, is_prescribed_holiday) SELECT 9, 'その他', '0', '0', '0', '0' WHERE NOT EXISTS (SELECT 1 FROM abstract_master WHERE id = 9);

-- 6. ログインアカウント（パスワード: password）
INSERT INTO login_table (username, password, enabled) SELECT '1', 'pbkdf2_sha256$210000$Z9CKxfJWARZDbu6a7pVomg==$lLVMH2UtGYyEm4n9FfJGPf1TS8rDHgYBf7jq6UhTfZw=', 1 WHERE NOT EXISTS (SELECT 1 FROM login_table WHERE username = '126');


-- 確認: 0 件ならハッシュ化されていないユーザーは残っていない
SELECT username FROM login_table WHERE password NOT LIKE 'pbkdf2_sha256$%' OR password IS NULL;
