package com.example.shared.web;

/**
 * プレゼンチE�Eション層の遷移惁E��を保持するクラス、E
 * Actionクラスの実行結果として返され、FrontControllerが次にどの画面に
 * どのように遷移するかを判断するために使用する、E
 */
public class View {

    private final String path;
    private final boolean isRedirect;

    /**
     * フォワード用のコンストラクタ、E
     *
     * @param path フォワード�EのJSPパス
     */
    public View(String path) {
        this(path, false);
    }

    /**
     * コンストラクタ、E
     *
     * @param path       遷移先�Eパス (JSPパス or URL)
     * @param isRedirect trueの場合�Eリダイレクト、falseの場合�EフォワーチE
     */
    public View(String path, boolean isRedirect) {
        this.path = path;
        this.isRedirect = isRedirect;
    }

    /**
     * 遷移先�Eパスを取得します、E
     * @return 遷移先�Eパス
     */
    public String getPath() {
        return path;
    }

    /**
     * リダイレクトを行うかどぁE��を判定します、E
     * @return リダイレクト�E場合�Etrue
     */
    public boolean isRedirect() {
        return isRedirect;
    }
}
