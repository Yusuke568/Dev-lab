-- ============================================================================
-- 半休を有給 0.5 日として数える（何度実行しても安全）
--
-- 1. 社員の有給残日数を 0.5 日単位で持てるよう、整数から小数（小数1桁）にする
-- 2. 勤務区分（適用マスタ）ごとに「有給消費日数」を持たせる
--      有給（有給扱いの区分）= 1 日、半休 = 0.5 日、それ以外 = 0 日
--    勤怠を保存すると、勤務区分の有給消費日数の分だけ残日数が増減する。
--    適用マスタ画面で区分ごとに変えられる（午前休・午後休などを足すときも同じ）。
-- ============================================================================

ALTER TABLE staff_table ALTER COLUMN paid_leave_days TYPE numeric(5,1);

ALTER TABLE abstract_master ADD COLUMN IF NOT EXISTS paid_leave_days numeric(2,1) NOT NULL DEFAULT 0;

-- 既存の区分に初期値を入れる（すでに設定してあれば変えない）。is_paid は boolean でも整数でも動くよう文字列で比べる
UPDATE abstract_master SET paid_leave_days = 1
WHERE paid_leave_days = 0 AND (name = '有給' OR CAST(is_paid AS text) IN ('true', '1'));

UPDATE abstract_master SET paid_leave_days = 0.5
WHERE paid_leave_days = 0 AND name = '半休';
