package com.example.controller.action;

import com.example.application.port.in.UpdateShainUseCase;
import com.example.controller.Action;
import com.example.controller.View;
import com.example.entity.Shain;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 社員情報更新の実行アクションクラス。（新アーキテクチャ）
 */
public class ShainUpdateExecuteAction implements Action {

    private final UpdateShainUseCase updateShainUseCase;

    public ShainUpdateExecuteAction(UpdateShainUseCase updateShainUseCase) {
        this.updateShainUseCase = updateShainUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            // リクエストからShainエンティティを作成
            Shain shain = new Shain();
            shain.setId(Integer.parseInt(request.getParameter("id")));
            shain.setName(request.getParameter("name"));
            shain.setNamekana(request.getParameter("namekana"));
            shain.setGender(request.getParameter("gender"));
            shain.setEntryyear(Integer.parseInt(request.getParameter("entryyear")));
            shain.setJobclass(request.getParameter("jobclass"));

            // ユースケースを実行してDBへ反映
            updateShainUseCase.updateShain(shain);
            
            // 社員一覧画面へリダイレクト
            return new View("/shainList.do", true);
            
        } catch (NumberFormatException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "不正なデータ形式です。IDや入社年は半角数字で入力してください。");
            return new View("/WEB-INF/view/error.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "データベースへの更新中にエラーが発生しました。");
            return new View("/WEB-INF/view/error.jsp");
        }
    }
}
