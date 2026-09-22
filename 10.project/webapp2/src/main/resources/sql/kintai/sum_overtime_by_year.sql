SELECT COALESCE(SUM(OVERTIME), 0) AS total_overtime
FROM work_month_table
WHERE STAFF_ID = ? AND YEAR(WORK_DATE) = ?;
