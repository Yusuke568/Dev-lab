-- ============================================================================
-- 残業の実績確認
--   事前申請が承認済みで、勤怠の時間外の実績が申請と合ったら（残業なしも含む）、
--   事前申請と同じ承認ルートの順に、各段の承認者が実績を確認する。
--   時間外申請書（Excel）の実績報告の承認欄には、各段の確認日を入れる。
-- 何度実行しても安全。DO ブロックは使わない。
-- ============================================================================

-- 段ごとの確認者（候補者ごとに1行。誰か1人が確認すれば、その段は終わり）
CREATE TABLE IF NOT EXISTS overtime_actual_step (
    id                 integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_id         integer NOT NULL,
    level              integer NOT NULL,
    approver_staff_id  integer NOT NULL,
    checked_at         timestamp,
    CONSTRAINT uq_overtime_actual_step UNIQUE (request_id, level, approver_staff_id)
);
CREATE INDEX IF NOT EXISTS idx_overtime_actual_step_approver ON overtime_actual_step (approver_staff_id, checked_at);

-- 実績確認の状態（NULL＝まだ / PENDING＝確認待ち / CONFIRMED＝全段確認済み）と、確認待ちの段
ALTER TABLE overtime_request ADD COLUMN IF NOT EXISTS actual_status varchar(10);
ALTER TABLE overtime_request ADD COLUMN IF NOT EXISTS actual_level integer;
COMMENT ON COLUMN overtime_request.actual_status IS '実績確認の状態（NULL / PENDING / CONFIRMED）';
COMMENT ON COLUMN overtime_request.actual_level IS '実績確認の確認待ちの段';
