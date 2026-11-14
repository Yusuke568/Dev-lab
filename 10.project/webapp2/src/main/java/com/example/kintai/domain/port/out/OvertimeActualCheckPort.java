package com.example.kintai.domain.port.out;

import com.example.kintai.domain.service.OvertimeFormCalculator.Step;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 残業の実績確認（事前申請と同じ承認ルートの順に、各段が実績を確認する）の保存先。
 */
public interface OvertimeActualCheckPort {

    String PENDING = "PENDING";
    String CONFIRMED = "CONFIRMED";

    /** 実績確認の状態。status は null（まだ）/ PENDING / CONFIRMED、level は確認待ちの段 */
    record State(String status, Integer level) {
        public static final State NONE = new State(null, null);
    }

    State findState(int requestId);

    /** 確認を始める：段ごとの候補者を入れ直し、1段目の確認待ちにする */
    void start(int requestId, List<List<Integer>> levels);

    /** 確認をやめる（段を消し、状態を「まだ」に戻す） */
    void reset(int requestId);

    /** 段の確認者として確認した日時を入れる。その段をまだ誰も確認していなければ true */
    boolean check(int requestId, int level, int approverStaffId);

    /** コメント（任意。空なら null）つきで確認する */
    default boolean check(int requestId, int level, int approverStaffId, String comment) {
        return check(requestId, level, approverStaffId);
    }

    void updateState(int requestId, String status, Integer level);

    /** 承認者が今の段の候補者で、まだ誰も確認していない申請（申請ID → 申請者の氏名。社員・対象日の順） */
    Map<Integer, String> findPendingForApprover(int approverStaffId);

    /** 申請ごとの確認の段（approved は確認済みか、decidedOn は確認した日） */
    Map<Integer, List<Step>> findSteps(Collection<Integer> requestIds);
}
