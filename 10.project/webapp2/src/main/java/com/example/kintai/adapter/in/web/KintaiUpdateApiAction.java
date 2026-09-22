package com.example.kintai.adapter.in.web;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.kintai.adapter.in.web.dto.KintaiRecordDto;
import com.example.kintai.application.port.in.UpdateAttendanceUseCase;
import com.example.kintai.application.port.in.UpdateAttendanceUseCase.AttendanceUpdateCommand;
import com.example.shared.web.Action;
import com.example.shared.web.View;

/**
 * 勤怠情報を非同期で更新するAPIアクション。
 */
public class KintaiUpdateApiAction implements Action {

    private final UpdateAttendanceUseCase updateAttendanceUseCase;

    public KintaiUpdateApiAction(UpdateAttendanceUseCase updateAttendanceUseCase) {
        this.updateAttendanceUseCase = updateAttendanceUseCase;
    }

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
            List<KintaiRecordDto> newKintaiList = mapper.readValue(jsonData, new TypeReference<List<KintaiRecordDto>>() {});

            List<AttendanceUpdateCommand> commands = newKintaiList.stream()
                    .map(this::toCommand)
                    .collect(Collectors.toList());

            // ユースケースを呼び出してDBを更新
            updateAttendanceUseCase.updateAttendance(commands);

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

    private AttendanceUpdateCommand toCommand(KintaiRecordDto dto) {
        LocalDate date = LocalDate.parse(dto.getKintaidate(), DateTimeFormatter.ISO_LOCAL_DATE);
        return new AttendanceUpdateCommand(
                String.valueOf(dto.getId()),
                date,
                dto.getWeek(),
                dto.getKintaifrom(),
                dto.getKintaito(),
                dto.getJikangai(),
                dto.getAbstractId(),
                dto.getMemo(),
                dto.getCorrectionId(),
                dto.getCorrectionUsTime(),
                dto.getCorrectionMidTime(),
                dto.getIndirectTime(),
                dto.getTotalWorkTime(),
                dto.getTotalDirectWorkTime(),
                dto.isTemporary()
        );
    }
}
