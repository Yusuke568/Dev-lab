INSERT INTO overtime_request
    (staff_id, target_date, planned_overtime_minutes, planned_start_time, planned_end_time, reason, status, created_at)
VALUES (?, ?, ?, ?, ?, ?, 'PENDING', NOW());
