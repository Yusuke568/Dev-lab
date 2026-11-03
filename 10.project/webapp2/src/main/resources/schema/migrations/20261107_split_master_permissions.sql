-- ============================================================================
-- マスタ管理の権限を、マスタごとに分ける（何度実行しても安全）
--
-- これまでの「マスタ管理」（MASTER_ADMIN）は全マスタをまとめた1つの権限だった。
-- 権限グループ画面でマスタごとに付け外しできるよう、次の7つに分ける。
--   MASTER_PERMISSION_GROUP 権限グループ / MASTER_JOB_CLASS 役職 / MASTER_DEPARTMENT 組織図 /
--   MASTER_APPROVAL_ROUTE 承認ルート設定 / MASTER_ABSTRACT 適用マスタ /
--   MASTER_LEAVE_RULE 有給付与ルール / MASTER_CORRECTION 補正CD
-- MASTER_ADMIN を持っていたグループには7つすべてを付け、MASTER_ADMIN は消す。
-- （アプリは実行前でも MASTER_ADMIN を全マスタとして扱うので、実行の順番は問わない）
-- ============================================================================

INSERT INTO permission_group_feature (group_id, feature_code)
SELECT old.group_id, f.code
FROM permission_group_feature old,
    (VALUES ('MASTER_PERMISSION_GROUP'), ('MASTER_JOB_CLASS'), ('MASTER_DEPARTMENT'), ('MASTER_APPROVAL_ROUTE'),
            ('MASTER_ABSTRACT'), ('MASTER_LEAVE_RULE'), ('MASTER_CORRECTION')) AS f(code)
WHERE old.feature_code = 'MASTER_ADMIN'
  AND NOT EXISTS (SELECT 1 FROM permission_group_feature x WHERE x.group_id = old.group_id AND x.feature_code = f.code);

DELETE FROM permission_group_feature WHERE feature_code = 'MASTER_ADMIN';
