package com.example.controller.action;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.example.common.MyUtil;
import com.example.controller.Action;
import com.example.controller.View;
import com.example.entity.CalendarDay;
import com.example.entity.Shain;
import com.example.entity.WorkType;
import com.example.service.KintaiService;
import com.example.service.ShainService;
import com.example.service.WorkTypeService;

/**
 * 勤怠入力画面を表示するアクションクラス。
 */
public class KintaiDisplayAction implements Action {

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            // Serviceクラスのインスタンス化
            KintaiService kintaiService = new KintaiService();
            WorkTypeService workTypeService = new WorkTypeService();
            ShainService shainService = new ShainService();
            
            // パラメータ取得
            int id = Integer.parseInt(request.getParameter("id"));
            int year = Integer.parseInt(request.getParameter("year"));
            int month = Integer.parseInt(request.getParameter("month"));
            
            // データの取得
            List<WorkType> workTypeList = workTypeService.getWorkTypeList();
            Shain shain = shainService.getShainById(id);

            // 対象社員の対象月勤怠情報がない場合、初期化処理を行う
            if(!kintaiService.isStaffidwork(id)) {
                kintaiService.initializeMonthlyKintai(id, year, month);
            }

            // 月次の勤怠情報を取得する
            List<CalendarDay> calendarList = kintaiService.getmonthKintai(id, year, month);

            // JSPへのデータ設定
            request.setAttribute("calendarList", calendarList);
            request.setAttribute("statusOptions", workTypeList);
            request.setAttribute("shain", shain);
            request.setAttribute("staff_id", id);
            
            // JavaScriptで使用するためのJSON文字列を作成
            // (MyUtil.getJsonString が WorkType に対応しているか確認が必要だが、ここではそのまま使用)
            StringBuilder json = MyUtil.getJsonString(workTypeList, "name");
            request.setAttribute("statusOptionsJson", json.toString());
            
            // 転送先を設定
            return new View("/WEB-INF/view/kintai.jsp");

        } catch (SQLException | NamingException | IOException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "勤怠情報の表示中にエラーが発生しました。");
            return new View("/WEB-INF/view/error.jsp");
        } catch (NumberFormatException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "ID、年、月の形式が正しくありません。");
            return new View("/WEB-INF/view/error.jsp");
        }
    }
}
