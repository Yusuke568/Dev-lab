package com.example.controller.action;

import com.example.application.port.in.DeleteShainUseCase;
import com.example.controller.Action;
import com.example.controller.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 社員情報削除の実行アクションクラス。（新アーキテクチャ）
 */
public class ShainDeleteExecuteAction implements Action {

    private final DeleteShainUseCase deleteShainUseCase;

    public ShainDeleteExecuteAction(DeleteShainUseCase deleteShainUseCase) {
        this.deleteShainUseCase = deleteShainUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            // リクエストから削除対象の社員IDを取得
            int id = Integer.parseInt(request.getParameter("id"));

            // ユースケースを実行してDBから削除
            deleteShainUseCase.deleteShainById(id);
            
            // 社員一覧画面へリダイレクト
            return new View("/shainList.do", true);
            
        } catch (NumberFormatException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "社員IDが不正です。");
            return new View("/WEB-INF/view/error.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "データベースからの削除中にエラーが発生しました。");
            return new View("/WEB-INF/view/error.jsp");
        }
    }
}
