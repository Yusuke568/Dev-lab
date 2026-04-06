package com.example.application.port.in;

import com.example.entity.Shain;
import java.util.Optional;

/**
 * 社員IDを指定して社員情報を取得するためのユースケースの入力ポート。
 */
public interface GetShainByIdUseCase {

    /**
     * 指定されたIDの社員情報を取得します。
     * @param id 取得する社員のID
     * @return 社員情報を含むOptional、見つからない場合は空のOptional
     */
    Optional<Shain> getShainById(int id);
}
