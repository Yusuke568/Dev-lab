package com.example.application.port.in;

import com.example.entity.Shain;

/**
 * 社員情報を更新するためのユースケースの入力ポート。
 */
public interface UpdateShainUseCase {

    /**
     * 指定された社員情報を更新します。
     * @param shain 更新する社員情報を持つエンティティ
     */
    void updateShain(Shain shain);
}
