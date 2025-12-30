package com.example.service.transaction;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.NamingException;

import com.example.infra.ConnectionBase;

/**
 * トランザクション管理の定型処理を実行するクラス。
 * テンプレートメソッドパターンのように振る舞い、ビジネスロジックから
 * トランザクションの決まりきったコードを分離する。
 */
public class TransactionManager {

    /**
     * 指定されたデータベース操作を単一のトランザクション内で実行します。
     *
     * @param <T> 操作の戻り値の型
     * @param operation 実行するデータベース操作
     * @return 操作の結果
     * @throws SQLException データベース関連のエラーが発生した場合
     * @throws NamingException JNDIルックアップに失敗した場合
     */
    public <T> T execute(TransactionalOperation<T> operation) throws SQLException, NamingException, IOException {
        Connection con = null;
        try {
            con = ConnectionBase.getConnection();
            con.setAutoCommit(false);

            T result = operation.execute(con);

            con.commit();
            return result;

        } catch (SQLException | NamingException | IOException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException e2) {
                    // 元の例外にロールバック失敗の例外を追加
                    e.addSuppressed(e2);
                }
            }
            // 捕捉した例外をそのままスロー
            throw e;
        } catch (Exception e) {
            // SQLExceptionやNamingException以外の予期せぬ実行時例外
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException e2) {
                    e.addSuppressed(e2);
                }
            }
            // 実行時例外としてラップしてスロー
            throw new RuntimeException("An unexpected error occurred during the transaction.", e);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    // クローズ失敗の例外は元の例外を上書きしないようにログ出力に留めるか、
                    // addSuppressedで追加するなどの考慮が必要だが、ここではシンプルにスタックトレースを出力。
                    e.printStackTrace();
                }
            }
        }
    }
}
