-- 指定した承認者が自分の段で承認したが、まだ申請中（後の段の承認待ち）の残業事前申請
SELECT r.id, r.staff_id, s.NAME AS staff_name, r.target_date, r.planned_overtime_minutes,
       r.planned_start_time, r.planned_end_time, r.reason, r.status, r.decided_by, r.decided_at, r.created_at,
       r.current_level, r.project_name, r.category, r.correction_minutes, r.correction_mid_minutes, r.actual_confirmed_on, r.base_levels
FROM overtime_request r
JOIN STAFF_TABLE s ON r.staff_id = s.id
WHERE r.status = 'PENDING'
  AND r.staff_id <> ?
  AND EXISTS (SELECT 1 FROM approval_step a
              WHERE a.request_type = 'OVERTIME' AND a.request_id = r.id
                AND a.approver_staff_id = ? AND a.decision = 'APPROVED')
ORDER BY r.staff_id, r.target_date ASC;
