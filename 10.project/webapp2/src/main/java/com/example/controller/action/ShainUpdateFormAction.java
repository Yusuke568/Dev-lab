package com.example.controller.action;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.controller.Action;
import com.example.controller.View;
import com.example.entity.Classmaster;
import com.example.entity.Shain;
import com.example.service.ClassmasterService;
import com.example.service.ShainService;

/**
 * 社員更新フォーム表示のアクションクラス。
 */
public class ShainUpdateFormAction implements Action {

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            ShainService shainService = new ShainService();
            ClassmasterService classmasterService = new ClassmasterService();
        
            // リクエストから更新対象の社員IDを取得
            int id = Integer.parseInt(request.getParameter("id"));
            
            // 社員情報を取得
            Shain shain = shainService.getShainById(id);
            if (shain == null) {
                request.setAttribute("errorMessage", "指定された社員IDの社員は存在しません。");
                return new View("/WEB-INF/view/error.jsp");
            }
            
            // 役職リストを取得
            List<Classmaster> jobList = classmasterService.getAllClassmaster();
            
            // リクエストスコープにセット
            request.setAttribute("shain", shain);
            request.setAttribute("jobList", jobList);
            
            // 更新フォームJSPにフォワード
            return new View("/WEB-INF/view/shainupdate.jsp");
            
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
