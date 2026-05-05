package com.example.shain.application.port.in;

import com.example.shain.domain.model.Shain;
import java.util.List;

/**
 * 社員一覧取得ユースケース。
 */
public interface GetShainListUseCase {
    List<Shain> getShainList();
}
