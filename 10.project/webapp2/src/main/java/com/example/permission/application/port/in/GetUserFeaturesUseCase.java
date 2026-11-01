package com.example.permission.application.port.in;

import com.example.permission.domain.model.Feature;

import java.util.Set;

/**
 * ログインした社員が使える機能を取得するユースケース。
 */
public interface GetUserFeaturesUseCase {

    Set<Feature> getFeatures(String staffId);
}
