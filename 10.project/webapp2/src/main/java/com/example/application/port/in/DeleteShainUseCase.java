package com.example.application.port.in;

/**
 * 社員情報を削除するためのユースケースの入力ポート。
 */
public interface DeleteShainUseCase {

    /**
     * 指定されたIDの社員情報を削除します。
     * @param id 削除する社員のID
     */
    void deleteShainById(int id);
}
