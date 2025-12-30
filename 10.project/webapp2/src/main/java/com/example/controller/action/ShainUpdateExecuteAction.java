package com.example.controller.action;

import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.example.controller.Action;
import com.example.controller.View;
import com.example.entity.Shain;
import com.example.service.ShainService;

/**
 * 社員情報更新の実行アクションクラス。
 */
public class ShainUpdateExecuteAction implements Action {

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            ShainService shainService = new ShainService();

            // リクエストからShainエンティティを作成
            Shain shain = new Shain();
            shain.setId(Integer.parseInt(request.getParameter("id")));
            shain.setName(request.getParameter("name"));
            shain.setNamekana(request.getParameter("namekana"));
            shain.setGender(request.getParameter("gender"));
            shain.setEntryyear(Integer.parseInt(request.getParameter("entryyear")));
            shain.setJobclass(request.getParameter("jobclass"));

            // DBへ反映
            shainService.updateShain(shain);
            
            // 社員一覧画面へリダイレクト
            return new View("/shainList.do", true);
            
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "データベースへの更新中にエラーが発生しました。");
            return new View("/WEB-INF/view/error.jsp");
        } catch (NumberFormatException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "不正なデータ形式です。IDや入社年は半角数字で入力してください。");
            return new View("/WEB-INF/view/error.jsp");
        }
    }
}
