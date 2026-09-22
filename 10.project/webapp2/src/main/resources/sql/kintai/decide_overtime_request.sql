UPDATE overtime_request
SET status = ?,
    decided_by = ?,
    decided_at = NOW()
WHERE id = ?;
