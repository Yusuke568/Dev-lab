package com.example.kintai.domain.port.out;

import com.example.kintai.domain.model.attendance.CorrectionMaster;

import java.util.List;

/**
 * 補正マスタの永続化操作を定義するポート。
 */
public interface CorrectionMasterPort {

    List<CorrectionMaster> findAll();

    void create(String name, int correctionUsTime, int correctionMidTime);
}
