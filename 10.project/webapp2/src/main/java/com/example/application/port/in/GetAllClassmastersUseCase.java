package com.example.application.port.in;

import com.example.entity.Classmaster;
import java.util.List;

/**
 * すべての役職情報を取得するためのユースケースの入力ポート。
 */
public interface GetAllClassmastersUseCase {

    /**
     * すべての役職情報をリストとして取得します。
     * @return 役職情報のリスト
     */
    List<Classmaster> getAllClassmasters();
}
