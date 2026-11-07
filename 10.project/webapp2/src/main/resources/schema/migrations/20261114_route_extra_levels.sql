-- ============================================================================
-- 承認ルートに「条件つきで足す段」を持たせる（残業事前申請だけ）
--   condition_type
--     MONTHLY_OVERTIME … 当月の時間外（実績＋今回の申請）が threshold_minutes 以上（例：18.5時間＝1110分、45時間＝2700分）
--     NIGHT            … 申請の予定が深夜（22:00〜翌5:00）にかかる（threshold_minutes は使わない）
--   seq が同じ行が1つの段。条件に当たった段だけを、ルートのふだんの段のあとに seq の順で足す。
--   sort_order は段の中の並び（先頭がその段の主承認者）。
-- 何度実行しても安全。DO ブロックは使わない。
-- ============================================================================

CREATE TABLE IF NOT EXISTS approval_route_extra_step (
    route_id          integer NOT NULL REFERENCES approval_route (id) ON DELETE CASCADE,
    seq               integer NOT NULL CHECK (seq >= 1),
    condition_type    varchar(20) NOT NULL,
    threshold_minutes integer,
    staff_id          integer NOT NULL,
    sort_order        integer NOT NULL DEFAULT 0,
    PRIMARY KEY (route_id, seq, staff_id)
);
