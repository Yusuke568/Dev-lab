package com.example.shared.error;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 例外から、画面に出してよいメッセージを決める。
 * <p>
 * 入力の誤りや業務ルールの違反は、日本語のメッセージを付けた IllegalArgumentException / IllegalStateException
 * で投げている（トランザクションの中で投げると RuntimeException に包まれる）。原因をたどってそれが見つかれば、
 * そのメッセージを出す。
 * それ以外（DB や ファイルの読み書きの失敗など）は、SQL 文やサーバーのフォルダ名を含むことがあるので画面には出さず、
 * 詳しい内容はサーバーのログに残して、決まった文言を出す。
 */
public final class UserMessages {

    private static final Logger LOG = Logger.getLogger(UserMessages.class.getName());
    /** 原因をたどる深さの上限（原因が輪になっていても止まるように） */
    private static final int MAX_DEPTH = 20;

    private UserMessages() {
    }

    /**
     * 画面に出すメッセージを返す。業務のメッセージが見つからなければ、ログに残して fallback を返す。
     */
    public static String of(Throwable e, String fallback) {
        Throwable found = findThrowable(e);
        if (found == null) {
            LOG.log(Level.WARNING, fallback, e);
            return fallback;
        }
        if (found.getCause() != null) {
            // 業務のメッセージでも、ファイルや DB の失敗を包んだものは、原因をログに残す
            LOG.log(Level.WARNING, found.getMessage(), e);
        }
        return found.getMessage();
    }

    /**
     * 原因をたどって、利用者に向けた業務のメッセージを探す。見つからなければ null。
     */
    public static String find(Throwable e) {
        Throwable found = findThrowable(e);
        return found != null ? found.getMessage() : null;
    }

    private static Throwable findThrowable(Throwable e) {
        Throwable t = e;
        for (int depth = 0; t != null && depth < MAX_DEPTH; depth++) {
            if ((t instanceof IllegalArgumentException || t instanceof IllegalStateException)
                    && isForUsers(t.getMessage())) {
                return t;
            }
            if (t.getCause() == t) {
                break;
            }
            t = t.getCause();
        }
        return null;
    }

    /**
     * 利用者向けに書いたメッセージか。業務のメッセージは日本語で書いているので、英数字だけのもの
     * （ライブラリや DB が付けたもの）は除く。
     */
    static boolean isForUsers(String message) {
        return message != null && !message.isBlank() && message.codePoints().anyMatch(c -> c > 0x7F);
    }
}
