package com.example.auth.testsupport;

import com.example.shared.transaction.TransactionManager;

import java.util.List;

/**
 * TransactionManagerImpl の代わり。開始時にメモリ上のデータを控えておき、例外が起きたら元に戻す。
 * 例外は本物と同じように RuntimeException に包んで投げ直す。
 */
public class FakeTransactionManager implements TransactionManager {

    /** トランザクションで元に戻す対象 */
    public interface Snapshottable {
        void takeSnapshot();

        void restoreSnapshot();
    }

    private final List<Snapshottable> targets;

    public FakeTransactionManager(Snapshottable... targets) {
        this.targets = List.of(targets);
    }

    @Override
    public <T> T executeInTransaction(TransactionOperation<T> operation) {
        targets.forEach(Snapshottable::takeSnapshot);
        try {
            return operation.execute();
        } catch (Exception e) {
            targets.forEach(Snapshottable::restoreSnapshot);
            throw new RuntimeException("トランザクションの実行中にエラーが発生しました", e);
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
