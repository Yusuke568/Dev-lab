package domain.compliance;

/**
 * 36協定 時間外アラートレベル
 */
public enum OvertimeLevel {
    /** 正常（35時間未満） */
    NORMAL,
    /** 注意（35時間以上〜45時間以下: イエローアラート） */
    WARNING_35H,
    /** 特別条項発動（45時間超〜80時間以下） */
    SPECIAL_CLAUSE_ACTIVE,
    /** 危険域（80時間超〜100時間未満） */
    DANGER_80H,
    /** 法定上限違反（100時間以上） */
    VIOLATION_100H
}
