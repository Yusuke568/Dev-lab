package com.example.controller.api;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.controller.Action;
import com.example.controller.View;
import com.example.entity.CalendarDay;
import com.example.service.KintaiService;

/**
 * 勤怠情報を非同期で更新するAPIアクション。
 */
public class KintaiUpdateApiAction implements Action {

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            // リクエストボディからJSONデータを読み込む
            StringBuilder jsonBuffer = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                jsonBuffer.append(line);
            }
            String jsonData = jsonBuffer.toString();

            // JSONをJavaオブジェクトにマッピング
            ObjectMapper mapper = new ObjectMapper();
            List<CalendarDay> newKintaiList = mapper.readValue(jsonData, new TypeReference<List<CalendarDay>>() {});
            
            // Serviceを呼び出してDBを更新
            KintaiService kintaiService = new KintaiService();
            kintaiService.updateKintaiList(newKintaiList);
            
            // 成功ステータスを設定
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            e.printStackTrace();
            // エラーが発生した場合は、HTTPステータス500を返す
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "勤怠更新処理中にエラーが発生しました。");
        }

        // 画面遷移は行わないのでnullを返す
        return null;
    }
}
