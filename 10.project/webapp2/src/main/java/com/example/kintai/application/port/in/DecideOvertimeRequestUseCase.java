package com.example.kintai.application.port.in;

/**
 * 残業事前申請を承認・却下するユースケース。
 */
public interface DecideOvertimeRequestUseCase {

    void decide(int requestId, boolean approve, String decidedBy);

    /** 理由（任意）つきで承認・却下する */
    default void decide(int requestId, boolean approve, String decidedBy, String comment) {
        decide(requestId, approve, decidedBy);
    }
}
