package com.example.application.port.out;

import com.example.entity.Classmaster;
import java.util.List;

/**
 * 役職データの永続化に関する操作を定義する出力ポート。
 */
public interface ClassmasterPort {

    /**
     * すべての役職エンティティを永続化層から検索します。
     * @return 見つかった役職エンティティのリスト
     */
    List<Classmaster> findAll();
}
