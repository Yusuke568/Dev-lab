package com.example.controller.action;

import com.example.application.port.in.GetShainByIdUseCase;
import com.example.controller.Action;
import com.example.controller.View;
import com.example.entity.Shain;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * 社員削除確認画面表示のアクションクラス。（新アーキテクチャ）
 */
public class ShainDeleteFormAction implements Action {

    private final GetShainByIdUseCase getShainByIdUseCase;

    public ShainDeleteFormAction(GetShainByIdUseCase getShainByIdUseCase) {
        this.getShainByIdUseCase = getShainByIdUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            // リクエストから削除対象の社員IDを取得
            int id = Integer.parseInt(request.getParameter("id"));
            
            // ユースケースを使って社員情報を取得
            Optional<Shain> shainOptional = getShainByIdUseCase.getShainById(id);
            if (!shainOptional.isPresent()) {
                request.setAttribute("errorMessage", "指定された社員IDの社員は存在しません。");
                return new View("/WEB-INF/view/error.jsp");
            }
            
            // リクエストスコープにセット
            request.setAttribute("shain", shainOptional.get());
            
            // 削除確認JSPにフォワード
            return new View("/WEB-INF/view/shaindelete.jsp");
            
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
