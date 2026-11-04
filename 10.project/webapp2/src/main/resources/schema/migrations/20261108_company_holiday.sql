-- ============================================================================
-- 会社休日マスタ（何度実行しても安全）
--   年末年始・創立記念日など、国民の祝日以外の会社の休日を日付で登録する。
--   勤怠画面では祝日と同じく色を付け、月の行を自動で作るときは「休日」にする。
-- ============================================================================

CREATE TABLE IF NOT EXISTS company_holiday (
    holiday_date  date PRIMARY KEY,
    name          varchar(100) NOT NULL,
    created_at    timestamp DEFAULT CURRENT_TIMESTAMP
);

-- 会社休日マスタの権限を、権限グループを設定できるグループ（管理者など）に付ける
INSERT INTO permission_group_feature (group_id, feature_code)
SELECT DISTINCT g.group_id, 'MASTER_COMPANY_HOLIDAY'
FROM permission_group_feature g
WHERE g.feature_code IN ('MASTER_PERMISSION_GROUP', 'MASTER_ADMIN')
  AND NOT EXISTS (SELECT 1 FROM permission_group_feature x
                  WHERE x.group_id = g.group_id AND x.feature_code = 'MASTER_COMPANY_HOLIDAY');
