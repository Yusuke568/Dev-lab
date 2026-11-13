-- ============================================================================
-- 予定出勤が 18:00 より遅い残業事前申請の「申請の時間」を計算し直す（一度だけ実行する）
--
--   修正前（コミット edb18f0 より前）は、予定出勤が 18:00 より遅くても 18:00 から数えていたため、
--   例：20:00〜22:00 の申請が 4:00 で保存されている（正しくは 2:00）。
--   申請履歴の「時間外内訳」は正しく出るが、勤怠のカレンダー・一覧の「予定」、承認画面のグラフ、
--   部下の勤怠表のサマリー、時間外申請書は保存した時間を使うので、ずれて見える。
--
--   直す時間 ＝ 予定出勤〜予定退勤（予定退勤が予定出勤より前なら翌日）＋ 補正（通常）＋ 補正（深夜）
--   承認の状態（承認済み・申請中）はそのまま。平日の申請だけ（土日の申請は休日の計算なので対象外）。
--
-- 使い方：手順1で確かめ → 手順2を実行（件数がおかしければ ROLLBACK）→ 手順3で確かめる
-- ============================================================================

-- ---------------------------------------------------------------------------
-- 手順1：直す申請と、直したあとの時間を確かめる（読むだけ）
-- ---------------------------------------------------------------------------
WITH calc AS (
    SELECT r.id, r.staff_id, r.target_date, r.planned_start_time, r.planned_end_time, r.status,
           r.planned_overtime_minutes AS old_minutes,
           (split_part(r.planned_start_time, ':', 1)::int * 60 + split_part(r.planned_start_time, ':', 2)::int) AS s,
           (split_part(r.planned_end_time, ':', 1)::int * 60 + split_part(r.planned_end_time, ':', 2)::int) AS e,
           COALESCE(r.correction_minutes, 0) + COALESCE(r.correction_mid_minutes, 0) AS corr
    FROM overtime_request r
    WHERE r.planned_start_time > '18:00'
      AND COALESCE(r.planned_end_time, '') <> ''
      AND r.status IN ('PENDING', 'APPROVED')
      AND EXTRACT(ISODOW FROM r.target_date) < 6
)
SELECT id, staff_id, target_date, planned_start_time, planned_end_time, status,
       old_minutes,
       (CASE WHEN e < s THEN e + 1440 ELSE e END) - s + corr AS new_minutes
FROM calc
WHERE (CASE WHEN e < s THEN e + 1440 ELSE e END) - s + corr <> old_minutes
ORDER BY staff_id, target_date;

-- ---------------------------------------------------------------------------
-- 手順2：直す
-- ---------------------------------------------------------------------------
BEGIN;

UPDATE overtime_request r
SET planned_overtime_minutes = c.new_minutes
FROM (
    SELECT id,
           (CASE WHEN e < s THEN e + 1440 ELSE e END) - s + corr AS new_minutes,
           planned_overtime_minutes AS old_minutes
    FROM (
        SELECT id, planned_overtime_minutes,
               (split_part(planned_start_time, ':', 1)::int * 60 + split_part(planned_start_time, ':', 2)::int) AS s,
               (split_part(planned_end_time, ':', 1)::int * 60 + split_part(planned_end_time, ':', 2)::int) AS e,
               COALESCE(correction_minutes, 0) + COALESCE(correction_mid_minutes, 0) AS corr
        FROM overtime_request
        WHERE planned_start_time > '18:00'
          AND COALESCE(planned_end_time, '') <> ''
          AND status IN ('PENDING', 'APPROVED')
          AND EXTRACT(ISODOW FROM target_date) < 6
    ) x
) c
WHERE r.id = c.id
  AND c.new_minutes <> c.old_minutes;
-- 「UPDATE n」の n が手順1の件数と同じなら COMMIT、違えば ROLLBACK

COMMIT;
-- ROLLBACK;

-- ---------------------------------------------------------------------------
-- 手順3：残っていないか確かめる（手順1をもう一度実行して 0件ならよい）
-- ---------------------------------------------------------------------------
