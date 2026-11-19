package com.example.master.adapter.in.web;

import com.example.kintai.domain.service.WorkReportFileName;
import com.example.master.application.port.in.MasterMaintenanceUseCase;

import javax.servlet.http.HttpServletRequest;
import java.time.YearMonth;

/**
 * 作業報告書の設定（ファイル名の形）。適用マスタの権限で使う。
 * 形は、勤怠画面からのダウンロード・部下のまとめ出し（ZIP）・サーバーの保存フォルダのファイル名に使う。
 */
public class MasterWorkReportAction extends MasterActionSupport {

    /** 画面の見本に使う社員 */
    private static final String SAMPLE_ID = "123";
    private static final String SAMPLE_NAME = "山田太郎";

    public MasterWorkReportAction(MasterMaintenanceUseCase masterUseCase) {
        super(masterUseCase);
    }

    @Override
    protected String selfPath() {
        return "/masterWorkReport.do";
    }

    @Override
    protected String show(HttpServletRequest request) {
        String pattern = null;
        try {
            pattern = masterUseCase.getWorkReportFileNamePattern();
        } catch (RuntimeException e) {
            e.printStackTrace(); // マイグレーション前などで読めなくても画面は開く
        }
        request.setAttribute("fileNamePattern", pattern == null ? WorkReportFileName.DEFAULT_PATTERN : pattern);
        request.setAttribute("fileNameIsDefault", pattern == null);
        request.setAttribute("fileNameSample",
                WorkReportFileName.render(pattern, YearMonth.now(), SAMPLE_ID, SAMPLE_NAME) + ".xlsm");
        request.setAttribute("sampleName", SAMPLE_NAME);
        request.setAttribute("sampleId", SAMPLE_ID);
        request.setAttribute("maxLength", WorkReportFileName.MAX_LENGTH);
        return "/WEB-INF/view/master/master_work_report.jsp";
    }

    @Override
    protected String save(HttpServletRequest request) {
        masterUseCase.saveWorkReportFileNamePattern(request.getParameter("fileNamePattern"));
        String saved = masterUseCase.getWorkReportFileNamePattern();
        return "作業報告書のファイル名を「" + WorkReportFileName.render(saved, YearMonth.now(), SAMPLE_ID, SAMPLE_NAME)
                + ".xlsm」の形にしました。";
    }
}
