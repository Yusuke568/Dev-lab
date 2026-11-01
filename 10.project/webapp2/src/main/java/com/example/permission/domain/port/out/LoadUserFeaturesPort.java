package com.example.permission.domain.port.out;

import java.util.Optional;
import java.util.Set;

/**
 * 社員 → 役職 → 権限グループ → 機能 をたどって、社員が使える機能を読み込むポート。
 */
public interface LoadUserFeaturesPort {

    /**
     * @return 機能コードの集合。役職に権限グループが設定されていない場合は empty
     */
    Optional<Set<String>> loadFeatureCodes(int staffId);
}
