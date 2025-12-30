package com.example.controller;

/**
 * プレゼンテーション層の遷移情報を保持するクラス。
 * Actionクラスの実行結果として返され、FrontControllerが次にどの画面に
 * どのように遷移するかを判断するために使用する。
 */
public class View {

    private final String path;
    private final boolean isRedirect;

    /**
     * フォワード用のコンストラクタ。
     *
     * @param path フォワード先のJSPパス
     */
    public View(String path) {
        this(path, false);
    }

    /**
     * コンストラクタ。
     *
     * @param path       遷移先のパス (JSPパス or URL)
     * @param isRedirect trueの場合はリダイレクト、falseの場合はフォワード
     */
    public View(String path, boolean isRedirect) {
        this.path = path;
        this.isRedirect = isRedirect;
    }

    /**
     * 遷移先のパスを取得します。
     * @return 遷移先のパス
     */
    public String getPath() {
        return path;
    }

    /**
     * リダイレクトを行うかどうかを判定します。
     * @return リダイレクトの場合はtrue
     */
    public boolean isRedirect() {
        return isRedirect;
    }
}
