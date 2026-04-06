package com.example.application.port.in;

/**
 * 社員情報を登録するためのユースケースの入力ポート。
 */
public interface InsertShainUseCase {

    /**
     * 指定された情報で新しい社員を登録します。
     * @param command 登録する社員の情報を持つコマンド
     */
    void insertShain(InsertShainCommand command);
}
