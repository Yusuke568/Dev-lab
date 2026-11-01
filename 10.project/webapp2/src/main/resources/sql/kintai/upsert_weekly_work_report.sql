INSERT INTO weekly_work_report
    (staff_id, week_end_date, client_name, project_name, process_name, progress_status, progress_detail, personal_note)
VALUES (?, ?, ?, ?, ?, ?, ?, ?)
ON CONFLICT (staff_id, week_end_date) DO UPDATE SET
    client_name = EXCLUDED.client_name,
    project_name = EXCLUDED.project_name,
    process_name = EXCLUDED.process_name,
    progress_status = EXCLUDED.progress_status,
    progress_detail = EXCLUDED.progress_detail,
    personal_note = EXCLUDED.personal_note;
