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
 * 社員削除確認画面表示のアクションクラス。
 */
public class ShainDeleteFormAction implements Action {

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            ShainService shainService = new ShainService();
            
            // リクエストから削除対象の社員IDを取得
            int id = Integer.parseInt(request.getParameter("id"));
            
            // 社員情報を取得
            Shain shain = shainService.getShainById(id);
            if (shain == null) {
                request.setAttribute("errorMessage", "指定された社員IDの社員は存在しません。");
                return new View("/WEB-INF/view/error.jsp");
            }
            
            // リクエストスコープにセット
            request.setAttribute("shain", shain);
            
            // 削除確認JSPにフォワード
            return new View("/WEB-INF/view/shaindelete.jsp");
            
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "データベースへの接続中にエラーが発生しました。");
            return new View("/WEB-INF/view/error.jsp");
        } catch (NumberFormatException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "社員IDが不正です。");
            return new View("/WEB-INF/view/error.jsp");
        }
    }
}
