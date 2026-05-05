package com.example.shain.application.port.in;

import com.example.shain.domain.model.ShainId;

/**
 * 次の社員ID取得ユースケース。
 */
public interface GetNextShainIdUseCase {
    ShainId getNextId();
}
