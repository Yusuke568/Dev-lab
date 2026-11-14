-- ============================================================================
-- 平日の残業事前申請の「申請の時間」を、休憩を除いた数え方で計算し直す（一度だけ実行する）
--
--   これまで平日の申請は、休憩（18:00〜18:30・22:00〜22:30・8:30〜9:00 など）を引かずに数えていたため、
--   予定どおりに働いても勤怠の実績より多くなり、勤怠に「⚠ 少ない」が出て、残業の実績確認が始まらなかった。
--   アプリは休憩を引く数え方に直したので、保存済みの申請もそろえる。
--   （予定出勤が18:00より遅い申請を 18:00 から数えていた分も、あわせて直る。fix_late_start_overtime_requests.sql は不要）
--
--   申請の時間 ＝ 早出（予定出勤〜9:00、休憩を除く）＋ 残業（18:00 か遅い予定出勤〜予定退勤、休憩を除く）＋ 補正
--              その日の勤怠の時間外のほうが多ければ、その時間（申請したときと同じ決まり）
--   対象：申請中・承認済みの、土日以外の申請（祝日の申請は休日の数え方なので、手順1で見つけたら対象から外す）
--   承認の状態はそのまま。
--
-- 使い方：手順1で確かめ → 手順2を実行（件数がおかしければ ROLLBACK）→ 手順1をもう一度実行して0件ならよい
--         直したあと、実績が申請と合うようになった日は、その日の勤怠を保存し直すと残業の実績確認が始まる
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 手順1：直す申請と、直したあとの時間を確かめる（読むだけ）
-- ---------------------------------------------------------------------------
WITH breaks(bs, be) AS (            -- 休憩時間帯（分）。翌日の分（+1440）も入れる
    SELECT s + d, e + d FROM (VALUES (300, 330), (510, 540), (720, 780), (1080, 1110), (1320, 1350), (120, 180)) b(s, e),
                             (VALUES (0), (1440)) o(d)
),
req AS (
    SELECT r.id, r.staff_id, r.target_date, r.planned_start_time, r.planned_end_time, r.status,
           r.planned_overtime_minutes AS old_minutes,
           COALESCE(r.correction_minutes, 0) + COALESCE(r.correction_mid_minutes, 0) AS corr,
           CASE WHEN COALESCE(r.planned_start_time, '') = '' THEN 540
                ELSE split_part(r.planned_start_time, ':', 1)::int * 60 + split_part(r.planned_start_time, ':', 2)::int END AS s,
           CASE WHEN COALESCE(r.planned_end_time, '') = '' THEN 1080
                ELSE split_part(r.planned_end_time, ':', 1)::int * 60 + split_part(r.planned_end_time, ':', 2)::int END AS e0
    FROM overtime_request r
    WHERE r.status IN ('PENDING', 'APPROVED')
      AND EXTRACT(ISODOW FROM r.target_date) < 6
),
span AS (
    SELECT q.*, CASE WHEN q.e0 < q.s THEN q.e0 + 1440 ELSE q.e0 END AS e, GREATEST(q.s, 1080) AS late_from FROM req q
),
calc AS (
    SELECT p.*,
           CASE WHEN p.s < 540 THEN (540 - p.s)
                - (SELECT COALESCE(SUM(GREATEST(0, LEAST(540, b.be) - GREATEST(p.s, b.bs))), 0) FROM breaks b) ELSE 0 END AS early,
           CASE WHEN p.e > p.late_from THEN (p.e - p.late_from)
                - (SELECT COALESCE(SUM(GREATEST(0, LEAST(p.e, b.be) - GREATEST(p.late_from, b.bs))), 0) FROM breaks b) ELSE 0 END AS late
    FROM span p
),
result AS (
    SELECT c.id, c.staff_id, c.target_date, c.planned_start_time, c.planned_end_time, c.status, c.old_minutes,
           GREATEST(GREATEST(0, c.early) + GREATEST(0, c.late) + c.corr, COALESCE(w.overtime, 0)) AS new_minutes
    FROM calc c
    LEFT JOIN work_month_table w ON w.staff_id = c.staff_id AND w.work_date = c.target_date
)
SELECT * FROM result WHERE new_minutes <> old_minutes ORDER BY staff_id, target_date;

-- ---------------------------------------------------------------------------
-- 手順2：直す（手順1と同じ計算）
-- ---------------------------------------------------------------------------
BEGIN;

WITH breaks(bs, be) AS (
    SELECT s + d, e + d FROM (VALUES (300, 330), (510, 540), (720, 780), (1080, 1110), (1320, 1350), (120, 180)) b(s, e),
                             (VALUES (0), (1440)) o(d)
),
req AS (
    SELECT r.id, r.staff_id, r.target_date, r.planned_overtime_minutes AS old_minutes,
           COALESCE(r.correction_minutes, 0) + COALESCE(r.correction_mid_minutes, 0) AS corr,
           CASE WHEN COALESCE(r.planned_start_time, '') = '' THEN 540
                ELSE split_part(r.planned_start_time, ':', 1)::int * 60 + split_part(r.planned_start_time, ':', 2)::int END AS s,
           CASE WHEN COALESCE(r.planned_end_time, '') = '' THEN 1080
                ELSE split_part(r.planned_end_time, ':', 1)::int * 60 + split_part(r.planned_end_time, ':', 2)::int END AS e0
    FROM overtime_request r
    WHERE r.status IN ('PENDING', 'APPROVED')
      AND EXTRACT(ISODOW FROM r.target_date) < 6
),
span AS (
    SELECT q.*, CASE WHEN q.e0 < q.s THEN q.e0 + 1440 ELSE q.e0 END AS e, GREATEST(q.s, 1080) AS late_from FROM req q
),
calc AS (
    SELECT p.*,
           CASE WHEN p.s < 540 THEN (540 - p.s)
                - (SELECT COALESCE(SUM(GREATEST(0, LEAST(540, b.be) - GREATEST(p.s, b.bs))), 0) FROM breaks b) ELSE 0 END AS early,
           CASE WHEN p.e > p.late_from THEN (p.e - p.late_from)
                - (SELECT COALESCE(SUM(GREATEST(0, LEAST(p.e, b.be) - GREATEST(p.late_from, b.bs))), 0) FROM breaks b) ELSE 0 END AS late
    FROM span p
),
result AS (
    SELECT c.id, c.old_minutes,
           GREATEST(GREATEST(0, c.early) + GREATEST(0, c.late) + c.corr, COALESCE(w.overtime, 0)) AS new_minutes
    FROM calc c
    LEFT JOIN work_month_table w ON w.staff_id = c.staff_id AND w.work_date = c.target_date
)
UPDATE overtime_request r
SET planned_overtime_minutes = x.new_minutes
FROM result x
WHERE r.id = x.id AND x.new_minutes <> x.old_minutes;
-- 「UPDATE n」の n が手順1の件数と同じなら COMMIT、違えば ROLLBACK

COMMIT;
-- ROLLBACK;
