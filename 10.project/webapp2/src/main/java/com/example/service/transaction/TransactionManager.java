package com.example.service.transaction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.NamingException;

import com.example.infra.ConnectionBase;

/**
 * トランザクション管理の定型処理を実行するクラス。
 * このクラスは、ビジネスロジックからトランザクションの決まりきったコードを分離します。
 */
public class TransactionManager {

    /**
     * 指定されたデータベース操作を単一のトランザクション内で実行します。
     * <p>
     * このメソッドは、自動的に接続を確立し、トランザクションを開始します。
     * 操作が成功した場合はコミットし、例外が発生した場合はロールバックします。
     * 接続は常にクローズされます。
     *
     * @param <T>       操作の戻り値の型
     * @param operation 実行するデータベース操作
     * @return 操作の結果
     * @throws SQLException    データベース関連のエラーが発生した場合
     * @throws NamingException JNDIルックアップに失敗した場合
     * @throws IOException     操作中にI/Oエラーが発生した場合
     */
    public <T> T execute(TransactionalOperation<T> operation) throws SQLException, NamingException, IOException {
        try (Connection con = ConnectionBase.getConnection()) {
            try {
                con.setAutoCommit(false);
                T result = operation.execute(con);
                con.commit();
                return result;
            } catch (SQLException | IOException e) {
                con.rollback();
                throw e;
            } catch (Exception e) {
                con.rollback();
                throw new RuntimeException("An unexpected error occurred during the transaction.", e);
            }
        }
    }
}
