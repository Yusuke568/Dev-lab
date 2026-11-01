select 
ID
,NAME
,NAMEKANA
,ENTRY_YEAR
,GENDER
,PAID_LEAVE_DAYS
,(select NAME from class_master where class_master.ID = STAFF_TABLE.CLASS_ID) AS JOBCLASS,
COALESCE((select ENABLED from login_table where login_table.USERNAME = CAST(STAFF_TABLE.ID AS varchar)), 1) AS ENABLED
from STAFF_TABLE