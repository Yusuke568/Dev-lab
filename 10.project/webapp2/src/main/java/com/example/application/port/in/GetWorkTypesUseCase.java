package com.example.application.port.in;

import com.example.entity.WorkType;
import java.util.List;

/**
 * 勤務区分リストを取得するユースケース。
 */
public interface GetWorkTypesUseCase {

    /**
     * 利用可能なすべての勤務区分を取得します。
     * @return 勤務区分のリスト
     */
    List<WorkType> getWorkTypes();
}
