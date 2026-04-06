package com.example.adapter.out.persistence;

import com.example.application.port.out.TransactionManager;
import com.example.infra.ConnectionBase;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;

/**
 * トランザクション管理のインフラストラクチャ実装。
 * JNDI からデータソースを取得し、ThreadLocal を使用してコネクションを管理します。
 */
public class TransactionManagerImpl implements TransactionManager {

    private Connection getNewConnection() throws Exception {
        String localName = "java:comp/env/jdbc/kintai";
        Context context = new InitialContext();
        DataSource ds = (DataSource) context.lookup(localName);
        return ds.getConnection();
    }

    @Override
    public <T> T executeInTransaction(TransactionOperation<T> operation) {
        try (Connection con = getNewConnection()) {
            con.setAutoCommit(false);
            // コネクションを ThreadLocal に保存
            ConnectionBase.setCurrentConnection(con);
            try {
                T result = operation.execute();
                con.commit();
                return result;
            } catch (Exception e) {
                con.rollback();
                throw new RuntimeException("トランザクションの実行中にエラーが発生しました", e);
            } finally {
                ConnectionBase.clearCurrentConnection();
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("データベース接続の取得に失敗しました", e);
        }
    }

    @Override
    public void executeInTransaction(TransactionRunnable operation) {
        executeInTransaction(() -> {
            operation.run();
            return null;
        });
    }
}
