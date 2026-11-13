package com.example.kintai.domain.port.out;

/**
 * 社員ごとの「いつもの補正CD」（勤務区分が未設定の日に入力を始めたときの初期値）。
 */
public interface DefaultCorrectionPort {

    /** いつもの補正CD。なければ null */
    Integer findDefaultCorrectionId(String staffId);

    /** いつもの補正CD を保存する（null で外す） */
    void saveDefaultCorrectionId(String staffId, Integer correctionId);
}
