package com.example.application.port.in;

import com.example.adapter.in.web.dto.KintaiRecordDto;
import java.util.List;

/**
 * 勤怠情報を更新するユースケース。
 */
public interface UpdateKintaiUseCase {

    /**
     * 複数件の勤怠レコードを更新または登録します。
     * @param records 更新する勤怠レコードのリスト
     */
    void updateKintai(List<KintaiRecordDto> records);
}
