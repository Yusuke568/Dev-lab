package com.example.kintai.application.port.in;

/**
 * 補正マスタを新規登録するユースケース。
 */
public interface RegisterCorrectionMasterUseCase {

    /**
     * @param name              補正名称
     * @param correctionUsTime  補正(通)：分単位
     * @param correctionMidTime 補正(深)：分単位
     */
    void register(String name, int correctionUsTime, int correctionMidTime);
}
