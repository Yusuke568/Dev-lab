package com.example.shain.application.port.in;

import com.example.shain.domain.model.ShainId;

/**
 * 社員削除ユースケース。
 */
public interface DeleteShainUseCase {
    void deleteShain(ShainId id);
}
