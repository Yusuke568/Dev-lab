package com.example.controller.action;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.example.controller.Action;
import com.example.controller.View;
import com.example.entity.Classmaster;
import com.example.service.ClassmasterService;
import com.example.service.ShainService;

/**
 * 社員登録フォーム表示のアクションクラス。
 */
public class ShainInsertFormAction implements Action {

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            //入社年リストを作成
            int currentYear = Year.now().getValue();
            List<Integer> yearList = new ArrayList<>();
            for (int year = currentYear; year > currentYear - 150; year--) {
                yearList.add(year);
            }
            
            // Serviceのインスタンス化
            ClassmasterService classmasterService = new ClassmasterService();
            ShainService shainService = new ShainService();
        
            // 役職リストと次の社員IDを取得
            List<Classmaster> jobList = classmasterService.getAllClassmaster();
            int nextID = shainService.getNextShainId();
            
            // リクエストスコープにセットする
            request.setAttribute("yearList", yearList);
            request.setAttribute("jobList", jobList);
            request.setAttribute("nextID", nextID);
            
            // 登録フォームJSPにフォワード
            return new View("/WEB-INF/view/shaininsert.jsp");
            
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "データベースへの接続中にエラーが発生しました。");
            return new View("/WEB-INF/view/error.jsp");
        }
    }
}
