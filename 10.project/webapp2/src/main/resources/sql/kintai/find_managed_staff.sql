-- 承認者（1つ目・2つ目の ? に社員ID）が承認者・副承認者になっている部署と、その配下の部署に所属する社員（兼任を含む。本人は除く）
WITH RECURSIVE managed AS (
    -- 主承認者（department.approver_staff_id）か副承認者（department_approver）になっている部署
    SELECT d.id FROM department d, (SELECT CAST(? AS integer) AS staff_id) me
    WHERE d.approver_staff_id = me.staff_id
       OR EXISTS (SELECT 1 FROM department_approver da WHERE da.department_id = d.id AND da.staff_id = me.staff_id)
    UNION
    SELECT d.id FROM department d JOIN managed m ON d.parent_id = m.id
)
SELECT
    s.id AS STAFF_ID,
    s.name AS STAFF_NAME,
    d.name AS DEPARTMENT_NAME
FROM staff_table s
LEFT JOIN department d ON s.department_id = d.id
WHERE (s.department_id IN (SELECT id FROM managed)
       OR EXISTS (SELECT 1 FROM staff_department sd WHERE sd.staff_id = s.id AND sd.department_id IN (SELECT id FROM managed)))
  AND s.id <> ?
ORDER BY d.sort_order ASC, d.id ASC, s.id ASC;
