SELECT id, staff_id, target_date, planned_overtime_minutes, planned_start_time, planned_end_time, reason,
       status, decided_by, decided_at, created_at
FROM overtime_request
WHERE staff_id = ? AND target_date = ?;
