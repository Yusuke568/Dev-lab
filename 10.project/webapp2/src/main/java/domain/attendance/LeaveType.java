package domain.attendance;

/**
 * 勤怠・休暇区分
 */
public enum LeaveType {
    /** 通常出勤 */
    NORMAL,
    /** 全日有給休暇 */
    PAID_LEAVE,
    /** 前半半休 */
    HALF_PAID_AM,
    /** 後半半休 */
    HALF_PAID_PM,
    /** 特別休暇・慶弔 */
    SPECIAL_LEAVE,
    /** 欠勤 */
    ABSENCE,
    /** 代休・振替休日 */
    SUBSTITUTE_HOLIDAY
}
