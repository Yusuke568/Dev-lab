-- 社員（1つ目の ?）に表示するお知らせ：所属部署とその上の部署あてのもので、掲載期限（2つ目の ?）が切れていないもの
WITH RECURSIVE upper_departments AS (
    SELECT d.id, d.parent_id
    FROM department d
    JOIN staff_table s ON s.department_id = d.id
    WHERE s.id = ?
    UNION
    SELECT p.id, p.parent_id
    FROM department p
    JOIN upper_departments u ON p.id = u.parent_id
)
SELECT n.id, n.author_staff_id, a.name AS author_name, n.department_id, d.name AS department_name,
       n.body, n.expires_on, n.created_at
FROM notice n
JOIN department d ON d.id = n.department_id
LEFT JOIN staff_table a ON a.id = n.author_staff_id
WHERE n.department_id IN (SELECT id FROM upper_departments)
  AND (n.expires_on IS NULL OR n.expires_on >= ?)
ORDER BY n.created_at DESC
LIMIT 20;
