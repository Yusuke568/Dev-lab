select 
ID
,NAME
,NAMEKANA
,ENTRY_YEAR
,GENDER
,PAID_LEAVE_DAYS
,(select NAME from class_master where class_master.ID = STAFF_TABLE.CLASS_ID) AS JOBCLASS
from STAFF_TABLE;