package domain.auth;

/**
 * ユーザー権限ロール
 */
public enum UserRole {
    /** 一般エンジニア（本人の勤怠・週報入力のみ） */
    MEMBER,
    /** 直属上司 / リーダー / 営業（配下の閲覧・週報一次レビュー・隠しメモ対応） */
    SUPERVISOR,
    /** 管理者 / 部長 / 労務（全員の閲覧・月次確定承認・ロック解除・マスタ管理） */
    ADMIN;

    public static UserRole fromClassId(String classId) {
        if (classId == null) return MEMBER;
        switch (classId.trim()) {
            case "2": // 部長
            case "4": // 管理長
            case "6": // 次長
            case "8": // 所長
                return ADMIN;
            case "3": // 班長
            case "5": // 課長
            case "9": // リーダー
                return SUPERVISOR;
            default:
                return MEMBER;
        }
    }
}
