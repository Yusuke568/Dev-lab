-- 承認者（1つ目・2つ目の ? に社員ID）が承認者になっている部署と、その配下の部署に所属する社員（本人は除く）
WITH RECURSIVE managed AS (
    SELECT id FROM department WHERE approver_staff_id = ?
    UNION
    SELECT d.id FROM department d JOIN managed m ON d.parent_id = m.id
)
SELECT
    s.id AS STAFF_ID,
    s.name AS STAFF_NAME,
    d.name AS DEPARTMENT_NAME
FROM staff_table s
JOIN department d ON s.department_id = d.id
WHERE s.department_id IN (SELECT id FROM managed)
  AND s.id <> ?
ORDER BY d.sort_order ASC, d.id ASC, s.id ASC;
