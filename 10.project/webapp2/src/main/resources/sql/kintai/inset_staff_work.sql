insert into work_month_table(STAFF_ID
,WORK_DATE
<<<<<<< HEAD
,WORK_WEEK
=======
>>>>>>> ef369d98792231d78d6225993f704111cea9ae00
,CORRECTION_ID
,JOB_FROM_TIME
,JOB_TO_TIME
,CORRECTION_US_TIME
,CORRECTION_MID_TIME
,INDIRECT_TIME
,TOTAL_WORK_TIME
,TOTAL_DIRECT_WORK_TIME
,OVERTIME
,ABSTRACT_ID
<<<<<<< HEAD
,REMARKS) values(?,?,?,null,?,?,null,null,null,null,null,?,(select ID from class_master where NAME = ?),null);
=======
,REMARKS) values(?,?,null,?,?,null,null,null,null,null,null,(select ID from class_master where NAME = ?),null);
>>>>>>> ef369d98792231d78d6225993f704111cea9ae00
