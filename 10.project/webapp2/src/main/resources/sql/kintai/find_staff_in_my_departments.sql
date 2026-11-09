-- 社員（1つ目・2つ目の ? に社員ID）が所属する部署（主所属・兼任）と、その下の部署に所属する社員（兼任を含む。本人も含む）
-- 週報担当が作業報告書を取り込める範囲
WITH RECURSIVE mine AS (
    SELECT s.department_id AS id FROM staff_table s WHERE s.id = ? AND s.department_id IS NOT NULL
    UNION
    SELECT sd.department_id FROM staff_department sd WHERE sd.staff_id = ?
    UNION
    SELECT d.id FROM department d JOIN mine m ON d.parent_id = m.id
)
SELECT
    s.id AS STAFF_ID,
    s.name AS STAFF_NAME,
    d.name AS DEPARTMENT_NAME
FROM staff_table s
LEFT JOIN department d ON s.department_id = d.id
WHERE s.department_id IN (SELECT id FROM mine)
   OR EXISTS (SELECT 1 FROM staff_department sd WHERE sd.staff_id = s.id AND sd.department_id IN (SELECT id FROM mine))
ORDER BY d.sort_order ASC, d.id ASC, s.id ASC;
