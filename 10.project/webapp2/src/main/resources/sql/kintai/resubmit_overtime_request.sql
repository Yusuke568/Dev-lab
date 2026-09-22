UPDATE overtime_request
SET planned_overtime_minutes = ?,
    planned_start_time = ?,
    planned_end_time = ?,
    reason = ?,
    status = 'PENDING',
    decided_by = NULL,
    decided_at = NULL,
    created_at = NOW()
WHERE id = ?;
