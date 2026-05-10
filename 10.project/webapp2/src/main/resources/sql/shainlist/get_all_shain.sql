select 
ID
,NAME
,NAMEKANA
,ENTRY_YEAR
,GENDER
,PAID_LEAVE_DAYS
,(select NAME from aatm_jobclass where aatm_jobclass.ID = aatb_staff.CLASS_ID) AS JOBCLASS
from aatb_staff;