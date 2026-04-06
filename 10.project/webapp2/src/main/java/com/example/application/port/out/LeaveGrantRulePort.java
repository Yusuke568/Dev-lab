package com.example.application.port.out;

import com.example.entity.LeaveGrantRule;
import java.util.List;

/**
 * 有給休暇付与ルールの永続化に関する操作を定義する出力ポート。
 */
public interface LeaveGrantRulePort {

    /**
     * すべての有給休暇付与ルールを永続化層から検索します。
     * @return 見つかった付与ルールのリスト
     */
    List<LeaveGrantRule> findAll();
}
