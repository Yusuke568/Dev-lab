-- SES勤怠管理 サンプルデータ投入SQL

-- 案件契約
INSERT INTO project_contracts (id, client_name, billing_lower_limit_hours, billing_upper_limit_hours, default_start_time, default_end_time, default_break_minutes)
VALUES ('contract_001', '某大手SIerシステム開発案件', 140.00, 180.00, '09:00:00', '18:00:00', 60)
ON DUPLICATE KEY UPDATE client_name = VALUES(client_name);

-- アサイン（社員ID: 1 または emp_001）
INSERT INTO assignments (id, employee_id, project_contract_id, start_date, end_date, allocation_ratio)
VALUES ('asg_001', '1', 'contract_001', '2026-10-01', '2026-10-31', 1.00)
ON DUPLICATE KEY UPDATE allocation_ratio = VALUES(allocation_ratio);
