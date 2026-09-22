package com.example.kintai.application.service;

import com.example.kintai.application.port.in.GetCorrectionMastersUseCase;
import com.example.kintai.application.port.in.RegisterCorrectionMasterUseCase;
import com.example.kintai.domain.model.attendance.CorrectionMaster;
import com.example.kintai.domain.port.out.CorrectionMasterPort;

import java.util.List;

/**
 * 補正マスタ管理のユースケース実装。
 */
public class CorrectionMasterService implements GetCorrectionMastersUseCase, RegisterCorrectionMasterUseCase {

    private final CorrectionMasterPort correctionMasterPort;

    public CorrectionMasterService(CorrectionMasterPort correctionMasterPort) {
        this.correctionMasterPort = correctionMasterPort;
    }

    @Override
    public List<CorrectionMaster> getCorrectionMasters() {
        return correctionMasterPort.findAll();
    }

    @Override
    public void register(String name, int correctionUsTime, int correctionMidTime) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("補正名称を入力してください。");
        }
        correctionMasterPort.create(name, correctionUsTime, correctionMidTime);
    }
}
