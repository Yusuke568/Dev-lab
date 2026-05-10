SELECT COUNT(*) AS count
FROM kttb_work_month
WHERE STAFF_ID = ?
  AND DATE_FORMAT(WORK_DATE, '%Y%m') = CONCAT(?, LPAD(?, 2, '0'));