package com.example.controller.action;

import com.example.application.port.in.InsertShainCommand;
import com.example.application.port.in.InsertShainUseCase;
import com.example.controller.Action;
import com.example.controller.View;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 社員登録実行のアクションクラス。（新アーキテクチャ）
 */
public class ShainInsertExecuteAction implements Action {

    private final InsertShainUseCase insertShainUseCase;

    public ShainInsertExecuteAction(InsertShainUseCase insertShainUseCase) {
        this.insertShainUseCase = insertShainUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            // リクエストからコマンドオブジェクトを作成
            InsertShainCommand command = new InsertShainCommand(
                    request.getParameter("name"),
                    request.getParameter("namekana"),
                    request.getParameter("gender"),
                    Integer.parseInt(request.getParameter("entryyear")),
                    request.getParameter("jobclass")
            );
            
            // ユースケースを実行してDBへ登録
            insertShainUseCase.insertShain(command);
            
            // 社員一覧画面にリダイレクト
            return new View("/shainList.do", true);
            
        } catch (NumberFormatException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "不正なデータ形式です。入社年は半角数字で入力してください。");
            // 本来は入力フォームに戻すべきだが、今回はエラーページに飛ばす
            return new View("/WEB-INF/view/error.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "データベースへの登録中にエラーが発生しました。");
            return new View("/WEB-INF/view/error.jsp");
        }
    }
}
