-- 時間外申請書（Excel）用：社員の期間の、申請中・承認済みの残業事前申請と、承認ルートの段（候補者ごとに1行）
SELECT r.id, r.staff_id, r.target_date, r.planned_overtime_minutes, r.planned_start_time, r.planned_end_time, r.reason,
       r.status, r.decided_by, r.decided_at, r.created_at, r.current_level, r.project_name, r.category,
       a.level AS step_level, a.decision AS step_decision, a.decided_at AS step_decided_at, s.NAME AS approver_name
FROM overtime_request r
LEFT JOIN approval_step a ON a.request_type = 'OVERTIME' AND a.request_id = r.id
LEFT JOIN STAFF_TABLE s ON s.id = a.approver_staff_id
WHERE r.staff_id = ?
  AND r.target_date BETWEEN ? AND ?
  AND r.status IN ('PENDING', 'APPROVED')
ORDER BY r.target_date, a.level, a.id;
