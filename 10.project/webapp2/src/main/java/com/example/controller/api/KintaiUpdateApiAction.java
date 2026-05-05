package com.example.controller.api;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.adapter.in.web.dto.KintaiRecordDto;
import com.example.application.port.in.UpdateKintaiUseCase;
import com.example.adapter.in.web.Action;
import com.example.adapter.in.web.View;

/**
 * 勤怠惁E��を非同期で更新するAPIアクション、E
 */
public class KintaiUpdateApiAction implements Action {

    private final UpdateKintaiUseCase updateKintaiUseCase;

    public KintaiUpdateApiAction(UpdateKintaiUseCase updateKintaiUseCase) {
        this.updateKintaiUseCase = updateKintaiUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        try {
            // リクエスト�EチE��からJSONチE�Eタを読み込む
            StringBuilder jsonBuffer = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                jsonBuffer.append(line);
            }
            String jsonData = jsonBuffer.toString();

            // JSONをJavaオブジェクトにマッピング
            ObjectMapper mapper = new ObjectMapper();
            List<KintaiRecordDto> newKintaiList = mapper.readValue(jsonData, new TypeReference<List<KintaiRecordDto>>() {});
            
            // Serviceを呼び出してDBを更新
            updateKintaiUseCase.updateKintai(newKintaiList);
            
            // 成功スチE�Eタスを設宁E
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            e.printStackTrace();
            // エラーが発生した場合�E、HTTPスチE�Eタス500を返す
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "勤怠更新処琁E��にエラーが発生しました、E);
        }

        // 画面遷移は行わなぁE�Eでnullを返す
        return null;
    }
}
