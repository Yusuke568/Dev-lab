package com.example.controller.action;

import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.controller.Action;
import com.example.controller.View;
import com.example.entity.Shain;
import com.example.service.ShainService;

/**
 * 社員登録実行のアクションクラス。
 */
public class ShainInsertExecuteAction implements Action {

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            // 社員サービスを作成
            ShainService shainService = new ShainService();
            
            // リクエストからShainエンティティの作成
            Shain shain = new Shain();
            
            // NOTE: IDはDBで自動採番されるか、Serviceで採番ロジックを持つべき。
            // ここではServiceのロジックを呼ぶ。
            shain.setId(shainService.getNextShainId()); 
            shain.setName(request.getParameter("name"));
            shain.setNamekana(request.getParameter("namekana"));
            shain.setGender(request.getParameter("gender"));
            shain.setEntryyear(Integer.parseInt(request.getParameter("entryyear")));
            shain.setJobclass(request.getParameter("jobclass"));
            
            // DBへ登録
            shainService.insertShain(shain);
            
            // 社員一覧画面にリダイレクト
            return new View("/shainList.do", true);
            
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "データベースへの登録中にエラーが発生しました。");
            return new View("/WEB-INF/view/error.jsp");
        } catch (NumberFormatException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "不正なデータ形式です。入社年は半角数字で入力してください。");
            // 本来は入力フォームに戻すべきだが、今回はエラーページに飛ばす
            return new View("/WEB-INF/view/error.jsp");
        }
    }
}
