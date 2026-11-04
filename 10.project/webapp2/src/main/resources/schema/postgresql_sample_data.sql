-- ============================================================================
-- 勤怠管理システム（webapp2）PostgreSQL サンプルデータ
-- postgresql_schema.sql の実行後に実行してください。
-- 20.db/sampleinsert の MySQL 版を PostgreSQL 用に書き換えたもの（マスタ・社員・ログインのみ）。
-- ログインパスワードは全員 "password"（PBKDF2 ハッシュで登録。1件ずつソルトが違う）。
-- ============================================================================

-- 適用マスタ（ID, 名称, 労働FLG, 有給FLG, 法定休FLG, 所定休FLG）
INSERT INTO abstract_master (id, name, is_work, is_paid, is_legal_holiday, is_prescribed_holiday) VALUES
    (1, '出勤', true, false, false, false),    -- 通常出勤
    (10, '休日', false, false, true, false),   -- 法定休日（日曜など）と仮定
    (2, '休出', true, false, false, true),    -- 所定休日労働（土曜出勤など）と仮定
    (3, '有給', false, true, false, false),    -- 有給休暇（労働ではないが給与あり）
    (4, '代休', false, false, false, false),    -- 休み
    (5, '半休', true, false, false, false),    -- 労働日として扱い、時間は実働で計算
    (6, '特休', false, false, false, false),    -- 特別休暇
    (7, '振出', true, false, false, false),    -- 振替出勤（平日扱い）
    (8, '振休', false, false, false, false),    -- 振替休日
    (9, 'その他', false, false, false, false);

-- 有給消費日数（勤怠を保存すると、この日数だけ有給残日数が減る）
UPDATE abstract_master SET paid_leave_days = 1 WHERE id = 3;    -- 有給
UPDATE abstract_master SET paid_leave_days = 0.5 WHERE id = 5;  -- 半休

-- 役職マスタ
INSERT INTO class_master (id, name) VALUES
    ('0', '一般'),
    ('1', 'TEST'),
    ('2', '部長'),
    ('3', '班長'),
    ('4', '管理長'),
    ('5', '課長'),
    ('6', '次長'),
    ('7', '会計'),
    ('8', '所長'),
    ('9', 'リーダー');

-- 有給付与ルール
INSERT INTO leave_grant_rules (years_of_service, grant_days) VALUES
    (0, 10),
    (1, 11),
    (2, 12),
    (3, 14),
    (4, 16),
    (5, 18),
    (6, 20);

-- 社員
INSERT INTO staff_table (id, name, namekana, entry_year, gender, class_id) VALUES
    (1, '中島　祐介', 'ナカシマユウスケ', 2018, '男', '1'),
    (7, '中村　晃', 'ナカムラ　アキラ', 2007, '男', '0'),
    (47, '大関　友久', 'オオゼキ　トモヒサ', 2019, '男', '0'),
    (125, '櫻井　翔', 'サクライ　ショウ', 1982, '男', '0'),
    (505, '野原　しんのすけ', 'ノハラ　シンノスケ', 1985, '男', '0'),
    (617, '二宮　和也', 'ニノミヤ　カズナリ', 1983, '男', '0'),
    (668, '11111', 'ファイル', 2025, '男', '2'),
    (711, '坂口　健太郎', 'サカグチ　ケンタロウ', 1991, '男', '0'),
    (830, '松本　潤', 'マツモト　ジュン', 1983, '男', '0'),
    (1023, '渡辺　直美', 'ワタナベ　ナオミ', 1987, '女', '0'),
    (1126, '大野　智', 'オオノ　サトシ', 1980, '男', '0'),
    (1224, '相葉　雅紀', 'アイバ　マサキ', 1982, '女', '0');

-- ログインアカウント（パスワード: password）
INSERT INTO login_table (username, password, enabled) VALUES
    ('1', 'pbkdf2_sha256$210000$Z9CKxfJWARZDbu6a7pVomg==$lLVMH2UtGYyEm4n9FfJGPf1TS8rDHgYBf7jq6UhTfZw=', 1),
    ('7', 'pbkdf2_sha256$210000$0MWBYVEHkb/0Rduz8t+rRA==$L2RGQT0BG9WzHpXEUyzKvwcOVZ7Fmw9fT5P5lrCxKc0=', 1),
    ('47', 'pbkdf2_sha256$210000$ZuXeOiFFUeBa7sq6/kzJfg==$tOCLVfXl6bsEf8a7XM2oZ6IlqQrqgxdNxeKiBX8OYDg=', 1),
    ('125', 'pbkdf2_sha256$210000$fVYZJ1XBTP37gyiAktaRMg==$WmZL4/HqIE3WKnP3OLIW+xkF1A4jM479tyyq4tEuHIc=', 1),
    ('505', 'pbkdf2_sha256$210000$U/3yTD8xATl8/mENVmC1Gw==$HVQQZdnJz2I6kjZPLOVg1l0NbXTx2c9Y2iC6twu2Rb4=', 1),
    ('617', 'pbkdf2_sha256$210000$ZX7o1/bXjerKr3qc9QNLQw==$g+n/NLfuCQyXiGL8EyvovfRFjol/OJ9nKNu5P+E3ge0=', 1),
    ('668', 'pbkdf2_sha256$210000$LVvHqIHYlOFzBluwZv7PRw==$P888WH6/k6npBRxap9LS9M1Sdg1GZhMHMl+qD4exXkc=', 1),
    ('711', 'pbkdf2_sha256$210000$I9Fov7RF7/QcdxT3PnOS+w==$hasSUmI4GDnwhR9slfz+4Psi3wuCOEtnCFFWhYumJyA=', 1),
    ('830', 'pbkdf2_sha256$210000$ybFsfcpszBWcpqKww5G1xA==$I6AKdhh9mpmaW947BvCDC17Wgz4b3N3XG9wbBCOZSqA=', 1),
    ('1023', 'pbkdf2_sha256$210000$uK+5IUeLwd2z5penjuP/Tg==$+PDyMptR+Mvy3OMiwVmSpboOe4Td7o74tC+qvPyPyIs=', 1),
    ('1126', 'pbkdf2_sha256$210000$kwIXiZVhNzSTh1F6S+tgZA==$IWthR5CY2LjUEjsQYz7S2MWLLYZbis+ZQ674OtDWjF8=', 1),
    ('1224', 'pbkdf2_sha256$210000$xnAiGfOmTyOkuuyyjy9Kmg==$Dj/V1hGN8zhYtA7SeVr5nIEN3ihyCmkmUugZEGRHQfw=', 1);
