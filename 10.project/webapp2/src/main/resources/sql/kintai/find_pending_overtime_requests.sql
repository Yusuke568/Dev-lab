SELECT r.id, r.staff_id, s.NAME AS staff_name, r.target_date, r.planned_overtime_minutes,
       r.planned_start_time, r.planned_end_time, r.reason, r.status, r.decided_by, r.decided_at, r.created_at
FROM overtime_request r
JOIN STAFF_TABLE s ON r.staff_id = s.id
WHERE r.status = 'PENDING'
ORDER BY r.target_date ASC;
