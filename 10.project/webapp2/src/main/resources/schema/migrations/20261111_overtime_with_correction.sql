-- ============================================================================
-- 保存済みの勤怠の「総勤務時間・勤務時間(直接)・時間外」を、作業報告書と同じ計算に直す
--
--   変更前：総勤務 ＝ 実働 ＋ 補正、          直接 ＝ 総勤務 − 間接、時間外 ＝ 実働（補正なし）− 8:00
--   変更後：総勤務 ＝ 実働 ＋ 補正 ＋ 間接、   直接 ＝ 総勤務 − 間接、時間外 ＝ 総勤務 − 8:00
--
-- 画面で保存し直した日は、アプリが新しい計算で保存するので、このスクリプトは不要。
-- 過去の月もまとめて直したいときだけ実行する。時間外が増える日があるので、36協定の年間の時間外の合計も変わる。
--
-- 2回実行すると間接の時間が二重に足されるので、実行したことを applied_migration に記録し、2回目は何もしない。
-- ============================================================================

CREATE TABLE IF NOT EXISTS applied_migration (
    name        varchar(100) PRIMARY KEY,
    applied_at  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM applied_migration WHERE name = '20261111_overtime_with_correction') THEN
        RAISE NOTICE '20261111_overtime_with_correction は実行済みのため、何もしません';
        RETURN;
    END IF;

    -- 右辺はすべて更新前の値を使う（PostgreSQL の UPDATE の決まり）
    UPDATE work_month_table
    SET total_work_time        = total_work_time + COALESCE(indirect_time, 0),
        total_direct_work_time = total_work_time,
        overtime               = GREATEST(0, total_work_time + COALESCE(indirect_time, 0) - 480)
    WHERE total_work_time IS NOT NULL;

    INSERT INTO applied_migration (name) VALUES ('20261111_overtime_with_correction');
END
$$;
