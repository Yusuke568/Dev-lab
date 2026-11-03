package com.example.master.domain.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 権限グループと、そのグループで使える機能コード。
 */
public class PermissionGroup {

    private final int id;
    private final String name;
    private final Set<String> featureCodes;

    public PermissionGroup(int id, String name, Set<String> featureCodes) {
        this.id = id;
        this.name = name;
        this.featureCodes = featureCodes;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public Set<String> getFeatureCodes() { return Collections.unmodifiableSet(featureCodes); }

    /**
     * JSP でチェックボックスの状態を出すため（${group.featureMap.MASTER_DEPARTMENT}）。
     * 以前の「マスタ管理（全マスタ）」が残っているグループは、すべてのマスタにチェックを付ける
     * （そのまま保存すると、マスタごとの権限に置き換わる）。
     */
    public Map<String, Boolean> getFeatureMap() {
        Map<String, Boolean> map = new HashMap<>();
        for (String code : featureCodes) {
            if (com.example.permission.domain.model.Feature.LEGACY_MASTER_ADMIN.equals(code)) {
                for (com.example.permission.domain.model.Feature f : com.example.permission.domain.model.Feature.masterFeatures()) {
                    map.put(f.name(), Boolean.TRUE);
                }
            } else {
                map.put(code, Boolean.TRUE);
            }
        }
        return map;
    }
}
