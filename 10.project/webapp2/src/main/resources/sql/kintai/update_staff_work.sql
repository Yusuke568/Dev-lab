UPDATE work_month_table
SET
    CORRECTION_ID = null,
    JOB_FROM_TIME = ?,
    JOB_TO_TIME = ?,
    CORRECTION_US_TIME = null,
    CORRECTION_MID_TIME = null,
    INDIRECT_TIME = null,
    TOTAL_WORK_TIME = null,
    TOTAL_DIRECT_WORK_TIME = null,
    OVERTIME = ?,
    ABSTRACT_ID = (SELECT ID FROM class_master WHERE NAME = ?),
    REMARKS = ?
WHERE STAFF_ID = ?
AND WORK_DATE = ?;
