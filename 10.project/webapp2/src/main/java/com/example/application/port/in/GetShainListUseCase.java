package com.example.application.port.in;

import com.example.entity.Shain;
import java.util.List;

/**
 * 社員一覧を取得するためのユースケースの入力ポート。
 */
public interface GetShainListUseCase {

    /**
     * すべての社員情報をリストとして取得します。
     * @return 社員情報のリスト
     */
    List<Shain> getShainList();
}
