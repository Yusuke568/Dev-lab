-- 承認者（1つ目・2つ目の ? に社員ID）が承認者になっている部署と、その配下の部署に所属する社員の申請中の勤怠
WITH RECURSIVE managed AS (
    SELECT id FROM department WHERE approver_staff_id = ?
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
  AND s.department_id IN (SELECT id FROM managed)
  AND w.STAFF_ID <> ?
ORDER BY w.WORK_DATE ASC, w.STAFF_ID ASC;