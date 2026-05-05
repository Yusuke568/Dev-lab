package com.example.shain.application.port.in;

import com.example.shain.domain.model.Shain;
import com.example.shain.domain.model.ShainId;
import java.util.Optional;

/**
 * 社員情報取得ユースケース。
 */
public interface GetShainByIdUseCase {
    Optional<Shain> getShainById(ShainId id);
}
