package com.example.shared.transaction;

/**
 * アプリケーションサービス層でトランザクションを管琁E��るため�Eインターフェース、E
 * インフラ層�E�データベ�Eス�E��E具体的な実裁E��依存しなぁE��ぁE��します、E
 */
public interface TransactionManager {

    /**
     * 戻り値を持つ処琁E��トランザクション冁E��実行します、E
     *
     * @param <T> 戻り値の垁E
     * @param operation トランザクション冁E��実行する�E琁E
     * @return 処琁E�E結果
     */
    <T> T executeInTransaction(TransactionOperation<T> operation);

    /**
     * 戻り値を持たなぁE�E琁E��トランザクション冁E��実行します、E
     *
     * @param operation トランザクション冁E��実行する�E琁E
     */
    void executeInTransaction(TransactionRunnable operation);

    /**
     * 戻り値を持つトランザクション操作�E関数型インターフェース、E
     *
     * @param <T> 戻り値の垁E
     */
    @FunctionalInterface
    interface TransactionOperation<T> {
        T execute() throws Exception;
    }

    /**
     * 戻り値を持たなぁE��ランザクション操作�E関数型インターフェース、E
     */
    @FunctionalInterface
    interface TransactionRunnable {
        void run() throws Exception;
    }
}
