-- 上長（?）が出したお知らせ（新しい順）
SELECT n.id, n.author_staff_id, a.name AS author_name, n.department_id, d.name AS department_name,
       n.body, n.expires_on, n.created_at
FROM notice n
JOIN department d ON d.id = n.department_id
LEFT JOIN staff_table a ON a.id = n.author_staff_id
WHERE n.author_staff_id = ?
ORDER BY n.created_at DESC
LIMIT 50;
