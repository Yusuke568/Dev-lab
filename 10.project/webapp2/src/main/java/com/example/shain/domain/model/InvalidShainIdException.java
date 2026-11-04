package com.example.shain.domain.model;

import java.util.Optional;

/**
 * 社員IDの変更で、入力した新しいIDが使えないときの例外（形式が不正・すでに使われている）。
 * メッセージはそのまま画面に表示する。
 */
public class InvalidShainIdException extends RuntimeException {

    public InvalidShainIdException(String message) {
        super(message);
    }

    /** TransactionManager に包まれて届くことがあるので、原因をたどって探す */
    public static Optional<InvalidShainIdException> findIn(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof InvalidShainIdException) {
                return Optional.of((InvalidShainIdException) t);
            }
        }
        return Optional.empty();
    }
}
