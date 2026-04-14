SELECT 
    s.ID
    ,s.NAME
    ,s.NAMEKANA
    ,s.ENTRY_YEAR
    ,s.GENDER
    ,s.PAID_LEAVE_DAYS
    ,c.NAME AS JOBCLASS
FROM 
    STAFF_TABLE s
LEFT JOIN 
    class_master c ON s.CLASS_ID = c.ID;