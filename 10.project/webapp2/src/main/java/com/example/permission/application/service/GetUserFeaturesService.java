package com.example.permission.application.service;

import com.example.permission.application.port.in.GetUserFeaturesUseCase;
import com.example.permission.domain.model.Feature;
import com.example.permission.domain.port.out.LoadUserFeaturesPort;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * 社員が使える機能を、役職に設定された権限グループから求める。
 *
 * 役職に権限グループが設定されていない社員（移行直後など）は、
 * 自分の勤怠・残業申請・休暇申請だけを使える「一般」と同じ扱いにする。
 */
public class GetUserFeaturesService implements GetUserFeaturesUseCase {

    private static final Set<Feature> DEFAULT_FEATURES =
            EnumSet.of(Feature.ATTENDANCE, Feature.OVERTIME_REQUEST, Feature.LEAVE_REQUEST);

    private final LoadUserFeaturesPort loadUserFeaturesPort;

    public GetUserFeaturesService(LoadUserFeaturesPort loadUserFeaturesPort) {
        this.loadUserFeaturesPort = loadUserFeaturesPort;
    }

    @Override
    public Set<Feature> getFeatures(String staffId) {
        Optional<Set<String>> codes = loadUserFeaturesPort.loadFeatureCodes(Integer.parseInt(staffId));
        if (!codes.isPresent()) {
            return EnumSet.copyOf(DEFAULT_FEATURES);
        }
        Set<Feature> features = EnumSet.noneOf(Feature.class);
        for (String code : codes.get()) {
            if (Feature.LEGACY_MASTER_ADMIN.equals(code)) {
                // 以前の「マスタ管理（全マスタ）」は、マスタごとの機能すべてとして扱う
                features.addAll(Feature.masterFeatures());
                continue;
            }
            try {
                features.add(Feature.valueOf(code));
            } catch (IllegalArgumentException ignored) {
                // 廃止した機能コードが DB に残っていても無視する
            }
        }
        return features;
    }
}
