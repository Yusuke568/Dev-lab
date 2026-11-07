-- 承認者（1つ目・2つ目の ? に社員ID）が承認者・副承認者になっている部署と、その配下の部署に所属する社員（兼任を含む）の申請中の勤怠
WITH RECURSIVE managed AS (
    -- 主承認者（department.approver_staff_id）か副承認者（department_approver）になっている部署
    SELECT d.id FROM department d, (SELECT CAST(? AS integer) AS staff_id) me
    WHERE d.approver_staff_id = me.staff_id
       OR EXISTS (SELECT 1 FROM department_approver da WHERE da.department_id = d.id AND da.staff_id = me.staff_id)
    UNION
    SELECT d.id FROM department d JOIN managed m ON d.parent_id = m.id
)
SELECT
    w.STAFF_ID,
    s.NAME AS STAFF_NAME,
    w.WORK_DATE,
    w.WORK_WEEK,
    w.JOB_FROM_TIME,
    w.JOB_TO_TIME,
    w.OVERTIME,
    a.NAME AS ABSTRACT_NAME,
    w.REMARKS
FROM work_month_table w
JOIN STAFF_TABLE s ON w.STAFF_ID = s.id
LEFT JOIN abstract_master a ON w.ABSTRACT_ID = a.ID
WHERE w.APPROVAL_STATUS = 1
  AND (s.department_id IN (SELECT id FROM managed)
       OR EXISTS (SELECT 1 FROM staff_department sd WHERE sd.staff_id = s.id AND sd.department_id IN (SELECT id FROM managed)))
  AND w.STAFF_ID <> ?
ORDER BY w.WORK_DATE ASC, w.STAFF_ID ASC;