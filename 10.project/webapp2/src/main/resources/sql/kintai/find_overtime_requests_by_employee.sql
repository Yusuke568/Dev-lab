SELECT id, staff_id, target_date, planned_overtime_minutes, planned_start_time, planned_end_time, reason,
       status, decided_by, decided_at, created_at, current_level, project_name, category, correction_minutes, correction_mid_minutes, actual_confirmed_on, base_levels
FROM overtime_request
WHERE staff_id = ?
ORDER BY target_date DESC;
