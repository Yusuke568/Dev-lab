-- 上長（?）がお知らせを出せる部署：承認者になっている部署とその下の部署
WITH RECURSIVE managed AS (
    SELECT id FROM department WHERE approver_staff_id = ?
    UNION
    SELECT d.id FROM department d JOIN managed m ON d.parent_id = m.id
)
SELECT id, name
FROM department
WHERE id IN (SELECT id FROM managed)
ORDER BY sort_order ASC, id ASC;
