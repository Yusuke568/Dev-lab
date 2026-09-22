package com.example.kintai.application.port.in;

/**
 * 残業事前申請を承認・却下するユースケース。
 */
public interface DecideOvertimeRequestUseCase {

    void decide(int requestId, boolean approve, String decidedBy);
}
