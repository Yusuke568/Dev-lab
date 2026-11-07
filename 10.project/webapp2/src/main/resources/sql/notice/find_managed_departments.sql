-- 上長（?）がお知らせを出せる部署：承認者・副承認者になっている部署とその下の部署
WITH RECURSIVE managed AS (
    -- 主承認者（department.approver_staff_id）か副承認者（department_approver）になっている部署
    SELECT d.id FROM department d, (SELECT CAST(? AS integer) AS staff_id) me
    WHERE d.approver_staff_id = me.staff_id
       OR EXISTS (SELECT 1 FROM department_approver da WHERE da.department_id = d.id AND da.staff_id = me.staff_id)
    UNION
    SELECT d.id FROM department d JOIN managed m ON d.parent_id = m.id
)
SELECT id, name
FROM department
WHERE id IN (SELECT id FROM managed)
ORDER BY sort_order ASC, id ASC;
