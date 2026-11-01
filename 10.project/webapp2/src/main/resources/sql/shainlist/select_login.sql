SELECT
    l.PASSWORD,
    s.ID,
    s.NAME,
    s.CLASS_ID
FROM
    login_table l
    JOIN staff_table s ON l.USERNAME = CAST(s.ID AS varchar)
WHERE
    l.USERNAME = ? AND l.ENABLED = 1;
