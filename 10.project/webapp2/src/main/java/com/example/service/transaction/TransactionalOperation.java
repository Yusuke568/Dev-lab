package com.example.service.transaction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;


/**
 * トランザクション内で実行されるデータベース操作を定義するための関数型インターフェース。
 *
 * @param <T> この操作の戻り値の型
 */
@FunctionalInterface
public interface TransactionalOperation<T> {

    /**
     * トランザクション内でデータベース操作を実行します。
     *
     * @param con アクティブなデータベース接続
     * @return 操作の結果
     * @throws SQLException データベースアクセスエラーが発生した場合
     * @throws IOException ファイルI/Oエラーなど、DAO操作中に発生する可能性のあるエラー
     */
    T execute(Connection con) throws SQLException, IOException;
}
