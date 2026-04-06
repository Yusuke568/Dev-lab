package com.example.controller.action;

import com.example.application.port.in.GetAllClassmastersUseCase;
import com.example.application.port.in.GetShainByIdUseCase;
import com.example.controller.Action;
import com.example.controller.View;
import com.example.entity.Classmaster;
import com.example.entity.Shain;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * 社員更新フォーム表示のアクションクラス。（新アーキテクチャ）
 */
public class ShainUpdateFormAction implements Action {

    private final GetShainByIdUseCase getShainByIdUseCase;
    private final GetAllClassmastersUseCase getAllClassmastersUseCase;

    public ShainUpdateFormAction(GetShainByIdUseCase getShainByIdUseCase, GetAllClassmastersUseCase getAllClassmastersUseCase) {
        this.getShainByIdUseCase = getShainByIdUseCase;
        this.getAllClassmastersUseCase = getAllClassmastersUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            // リクエストから更新対象の社員IDを取得
            int id = Integer.parseInt(request.getParameter("id"));
            
            // ユースケースを使って社員情報を取得
            Optional<Shain> shainOptional = getShainByIdUseCase.getShainById(id);
            if (!shainOptional.isPresent()) {
                request.setAttribute("errorMessage", "指定された社員IDの社員は存在しません。");
                return new View("/WEB-INF/view/error.jsp");
            }
            
            // ユースケースを使って役職リストを取得
            List<Classmaster> jobList = getAllClassmastersUseCase.getAllClassmasters();
            
            // リクエストスコープにセット
            request.setAttribute("shain", shainOptional.get());
            request.setAttribute("jobList", jobList);
            
            // 更新フォームJSPにフォワード
            return new View("/WEB-INF/view/shainupdate.jsp");
            
        } catch (NumberFormatException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "社員IDが不正です。");
            return new View("/WEB-INF/view/error.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "データの取得中にエラーが発生しました。");
            return new View("/WEB-INF/view/error.jsp");
        }
    }
}
