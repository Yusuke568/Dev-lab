-- ============================================================================
-- 導入時の作業：承認待ちの勤怠（有給・事前申請なしの時間外）をまとめて承認する
--
--   アプリで承認したときと同じように、
--     ・勤怠（work_month_table）の approval_status を 1（承認待ち）→ 2（承認済み）にする
--     ・承認履歴（kintai_approval_history）に「APPROVED」を残す
--   残業事前申請（overtime_request）・休暇申請は変えない。
--
-- 使い方（psql / pgAdmin など）
--   1. 下の「★」の3か所（対象の期間・承認者の社員ID）を書き換える
--   2. 手順1の SELECT で、承認する勤怠を確かめる
--   3. 手順2を実行する（BEGIN〜COMMIT。件数がおかしければ COMMIT の代わりに ROLLBACK）
--   4. 手順3で、承認待ちが残っていないか確かめる
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 手順1：承認する勤怠を確かめる（読むだけ）
-- ---------------------------------------------------------------------------
SELECT w.staff_id,
       s.name,
       w.work_date,
       w.job_from_time,
       w.job_to_time,
       w.overtime,
       a.name AS work_type
FROM work_month_table w
JOIN staff_table s ON s.id = w.staff_id
LEFT JOIN abstract_master a ON a.id = w.abstract_id
WHERE w.approval_status = 1
  AND w.work_date BETWEEN DATE '2025-12-01' AND DATE '2026-10-31'   -- ★対象の期間
ORDER BY w.staff_id, w.work_date;

-- 社員ごとの件数
SELECT w.staff_id, s.name, COUNT(*) AS days
FROM work_month_table w
JOIN staff_table s ON s.id = w.staff_id
WHERE w.approval_status = 1
  AND w.work_date BETWEEN DATE '2025-12-01' AND DATE '2026-10-31'   -- ★対象の期間（上と同じにする）
GROUP BY w.staff_id, s.name
ORDER BY w.staff_id;

-- ---------------------------------------------------------------------------
-- 手順2：まとめて承認する（承認と履歴を1つの文でまとめて行う）
-- ---------------------------------------------------------------------------
BEGIN;

WITH approved AS (
    UPDATE work_month_table
    SET approval_status = 2
    WHERE approval_status = 1
      AND work_date BETWEEN DATE '2025-12-01' AND DATE '2026-10-31'  -- ★対象の期間（手順1と同じにする）
    RETURNING staff_id, work_date
)
INSERT INTO kintai_approval_history (staff_id, work_date, decision, decided_by, decided_at)
SELECT staff_id, work_date, 'APPROVED',
       1,                                                            -- ★承認者として残す社員ID（管理者など）
       NOW()
FROM approved;
-- 「INSERT 0 n」の n が承認した件数。手順1の件数と同じなら COMMIT、違えば ROLLBACK

COMMIT;
-- ROLLBACK;

-- ---------------------------------------------------------------------------
-- 手順3：承認待ちが残っていないか確かめる（読むだけ）
-- ---------------------------------------------------------------------------
SELECT COUNT(*) AS still_pending
FROM work_month_table
WHERE approval_status = 1
  AND work_date BETWEEN DATE '2025-12-01' AND DATE '2026-10-31';    -- ★対象の期間（手順1と同じにする）
