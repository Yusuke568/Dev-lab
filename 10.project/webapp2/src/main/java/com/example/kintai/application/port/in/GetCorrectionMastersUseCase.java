package com.example.kintai.application.port.in;

import com.example.kintai.domain.model.attendance.CorrectionMaster;

import java.util.List;

/**
 * 登録済みの補正マスタ一覧を取得するユースケース。
 */
public interface GetCorrectionMastersUseCase {

    List<CorrectionMaster> getCorrectionMasters();
}
