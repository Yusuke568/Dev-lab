package com.example.controller.action;

import com.example.application.port.in.GetAllClassmastersUseCase;
import com.example.application.port.in.GetNextShainIdUseCase;
import com.example.controller.Action;
import com.example.controller.View;
import com.example.entity.Classmaster;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * 社員登録フォーム表示のアクションクラス。（新アーキテクチャ）
 */
public class ShainInsertFormAction implements Action {

    private final GetAllClassmastersUseCase getAllClassmastersUseCase;
    private final GetNextShainIdUseCase getNextShainIdUseCase;

    public ShainInsertFormAction(GetAllClassmastersUseCase getAllClassmastersUseCase, GetNextShainIdUseCase getNextShainIdUseCase) {
        this.getAllClassmastersUseCase = getAllClassmastersUseCase;
        this.getNextShainIdUseCase = getNextShainIdUseCase;
    }

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
            
            // ユースケースを使って役職リストと次の社員IDを取得
            List<Classmaster> jobList = getAllClassmastersUseCase.getAllClassmasters();
            int nextID = getNextShainIdUseCase.getNextShainId();
            
            // リクエストスコープにセットする
            request.setAttribute("yearList", yearList);
            request.setAttribute("jobList", jobList);
            request.setAttribute("nextID", nextID);
            
            // 登録フォームJSPにフォワード
            return new View("/WEB-INF/view/shaininsert.jsp");
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "データの取得中にエラーが発生しました。");
            return new View("/WEB-INF/view/error.jsp");
        }
    }
}
