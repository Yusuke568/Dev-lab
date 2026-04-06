package com.example.application.port.in;

/**
 * 次に採番すべき社員IDを取得するためのユースケースの入力ポート。
 */
public interface GetNextShainIdUseCase {

    /**
     * 次の社員IDを取得します。
     * @return 次の社員ID
     */
    int getNextShainId();
}
