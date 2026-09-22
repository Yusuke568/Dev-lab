SELECT
    h.staff_id,
    s.NAME AS staff_name,
    h.work_date,
    h.decision,
    h.decided_by,
    d.NAME AS decided_by_name,
    h.decided_at
FROM kintai_approval_history h
JOIN STAFF_TABLE s ON h.staff_id = s.id
LEFT JOIN STAFF_TABLE d ON h.decided_by = d.id
