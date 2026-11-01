SELECT
    l.PASSWORD,
    s.ID,
    s.NAME,
    s.CLASS_ID
FROM
    login_table l
    JOIN staff_table s ON l.USERNAME = s.ID
WHERE
    l.USERNAME = ? AND l.ENABLED = 1;
