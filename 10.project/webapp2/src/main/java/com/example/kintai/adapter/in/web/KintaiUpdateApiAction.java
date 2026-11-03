package com.example.kintai.adapter.in.web;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.kintai.adapter.in.web.dto.KintaiRecordDto;
import com.example.kintai.application.port.in.UpdateAttendanceUseCase;
import com.example.kintai.application.port.in.UpdateAttendanceUseCase.AttendanceUpdateCommand;
import com.example.shared.web.Action;
import com.example.shared.web.JsonApiSupport;
import com.example.shared.web.View;

/**
 * 勤怠情報を非同期で更新するAPIアクション。
 */
public class KintaiUpdateApiAction implements Action {

    private final UpdateAttendanceUseCase updateAttendanceUseCase;
    private final com.example.kintai.application.port.in.AttendanceAccessUseCase attendanceAccessUseCase;

    public KintaiUpdateApiAction(UpdateAttendanceUseCase updateAttendanceUseCase,
                                 com.example.kintai.application.port.in.AttendanceAccessUseCase attendanceAccessUseCase) {
        this.updateAttendanceUseCase = updateAttendanceUseCase;
        this.attendanceAccessUseCase = attendanceAccessUseCase;
    }

    @Override
    public View execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 保存できるのは、本人の勤怠と、承認権限を持つ人にとっての組織図上の配下の社員の勤怠だけ
        HttpSession session = request.getSession(false);
        String loginUser = session != null ? (String) session.getAttribute("loginUser") : null;
        if (loginUser == null) {
            JsonApiSupport.writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "ログインし直してください。");
            return null;
        }
        if (!JsonApiSupport.isJsonRequest(request)) {
            JsonApiSupport.writeError(response, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "送信形式が正しくありません。");
            return null;
        }

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
            @SuppressWarnings("unchecked")
            java.util.Set<String> features = (java.util.Set<String>) session.getAttribute("features");
            java.util.Set<String> targets = newKintaiList.stream()
                    .map(dto -> String.valueOf(dto.getId()))
                    .collect(Collectors.toSet());
            for (String target : targets) {
                if (!attendanceAccessUseCase.canAccess(loginUser, features, target)) {
                    JsonApiSupport.writeError(response, HttpServletResponse.SC_FORBIDDEN, "この社員の勤怠を保存する権限がありません。");
                    return null;
                }
            }

            List<AttendanceUpdateCommand> commands = newKintaiList.stream()
                    .map(this::toCommand)
                    .collect(Collectors.toList());

            // ユースケースを呼び出してDBを更新
            updateAttendanceUseCase.updateAttendance(commands);

            // 成功ステータスを設定
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            // 入力内容のエラー（退勤が出勤より前など）は、理由を画面に表示できるよう 400 で返す
            IllegalArgumentException invalid = findInvalidInput(e);
            if (invalid != null) {
                JsonApiSupport.writeError(response, HttpServletResponse.SC_BAD_REQUEST, invalid.getMessage());
            } else {
                e.printStackTrace();
                JsonApiSupport.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "勤怠の保存中にエラーが発生しました。");
            }
        }

        // 画面遷移は行わないのでnullを返す
        return null;
    }

    /** TransactionManager に包まれた例外の中から、入力内容のエラーを探す。 */
    private static IllegalArgumentException findInvalidInput(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof IllegalArgumentException && t.getMessage() != null) {
                return (IllegalArgumentException) t;
            }
        }
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
