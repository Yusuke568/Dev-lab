SELECT staff_id, week_end_date, client_name, project_name, process_name,
       progress_status, progress_detail, problem_countermeasure, personal_note, leader_comment
FROM weekly_work_report
WHERE staff_id = ? AND week_end_date = ?;
