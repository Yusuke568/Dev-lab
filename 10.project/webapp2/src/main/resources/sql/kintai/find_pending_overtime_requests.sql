-- 指定した承認者の、いまの段で処理待ちになっている残業事前申請
SELECT r.id, r.staff_id, s.NAME AS staff_name, r.target_date, r.planned_overtime_minutes,
       r.planned_start_time, r.planned_end_time, r.reason, r.status, r.decided_by, r.decided_at, r.created_at,
       r.current_level
FROM overtime_request r
JOIN STAFF_TABLE s ON r.staff_id = s.id
JOIN approval_step a ON a.request_type = 'OVERTIME' AND a.request_id = r.id AND a.level = r.current_level
WHERE r.status = 'PENDING'
  AND a.decision IS NULL
  AND a.approver_staff_id = ?
ORDER BY r.target_date ASC;