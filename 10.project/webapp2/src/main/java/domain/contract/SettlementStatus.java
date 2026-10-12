package domain.contract;

/**
 * 精算判定ステータス
 */
public enum SettlementStatus {
    /** 基準時間内 */
    WITHIN_RANGE,
    /** 下限割れ（控除対象） */
    SHORTAGE,
    /** 上限超過（増額対象） */
    OVERTIME
}
