package controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * すべてのアクションクラスが実装するインターフェース。
 * フロントコントローラーから呼び出される処理の規約を定めます。
 */
public interface Action {

    /**
     * アクションに対応するビジネスロジックを実行します。
     *
     * @param request  HTTPリクエスト
     * @param response HTTPレスポンス
     * @return フォワード先のJSPパス
     * @throws ServletException
     * @throws IOException
     */
    public String execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException;
}
