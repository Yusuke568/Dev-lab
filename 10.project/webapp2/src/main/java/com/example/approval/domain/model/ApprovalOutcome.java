package com.example.approval.domain.model;

/**
 * 承認者が1段分の判定をしたあとの、申請全体の状態。
 */
public enum ApprovalOutcome {
    /** 最終段まで承認された */
    APPROVED,
    /** 次の段の承認待ちに進んだ */
    NEXT_LEVEL,
    /** 却下された（以降の段は処理しない） */
    REJECTED
}
