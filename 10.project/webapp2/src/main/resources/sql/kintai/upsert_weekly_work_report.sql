INSERT INTO weekly_work_report
    (staff_id, week_end_date, client_name, project_name, process_name, progress_status, progress_detail, personal_note)
VALUES (?, ?, ?, ?, ?, ?, ?, ?)
ON DUPLICATE KEY UPDATE
    client_name = VALUES(client_name),
    project_name = VALUES(project_name),
    process_name = VALUES(process_name),
    progress_status = VALUES(progress_status),
    progress_detail = VALUES(progress_detail),
    personal_note = VALUES(personal_note);
