package com.example.application.port.out;

/**
 * アプリケーションサービス層でトランザクションを管理するためのインターフェース。
 * インフラ層（データベース）の具体的な実装に依存しないようにします。
 */
public interface TransactionManager {

    /**
     * 戻り値を持つ処理をトランザクション内で実行します。
     *
     * @param <T> 戻り値の型
     * @param operation トランザクション内で実行する処理
     * @return 処理の結果
     */
    <T> T executeInTransaction(TransactionOperation<T> operation);

    /**
     * 戻り値を持たない処理をトランザクション内で実行します。
     *
     * @param operation トランザクション内で実行する処理
     */
    void executeInTransaction(TransactionRunnable operation);

    /**
     * 戻り値を持つトランザクション操作の関数型インターフェース。
     *
     * @param <T> 戻り値の型
     */
    @FunctionalInterface
    interface TransactionOperation<T> {
        T execute() throws Exception;
    }

    /**
     * 戻り値を持たないトランザクション操作の関数型インターフェース。
     */
    @FunctionalInterface
    interface TransactionRunnable {
        void run() throws Exception;
    }
}
