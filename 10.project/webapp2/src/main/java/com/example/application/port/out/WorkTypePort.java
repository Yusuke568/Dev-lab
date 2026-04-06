package com.example.application.port.out;

import com.example.entity.WorkType;
import java.util.List;

/**
 * 勤務区分（適用マスタ）へのアクセスを提供する永続化ポート。
 */
public interface WorkTypePort {

    /**
     * 全ての勤務区分を取得します。
     * @return 勤務区分のリスト
     */
    List<WorkType> findAll();
}
