package com.example.kintai.application.port.in;

import com.example.kintai.domain.model.employee.EmployeeId;

import java.time.YearMonth;

/**
 * 月次勤怠データを社内所定の「作業報告書」Excel（.xlsm）様式として出力するユースケース。
 */
public interface ExportWorkReportUseCase {

    /**
     * 作業報告書を作る。サーバーの置き場所が設定されていれば、そこにも置く。
     *
     * @param employeeId 対象の社員ID
     * @param yearMonth  対象の年月
     * @return 生成された .xlsm ファイルと、サーバーのフォルダに置いた結果
     */
    ExportedWorkReport exportMonth(EmployeeId employeeId, YearMonth yearMonth);

    class ExportedWorkReport {
        private final byte[] content;
        private final String archivedPath;
        private final String archiveError;
        private final String fileName;

        public ExportedWorkReport(byte[] content, String archivedPath, String archiveError) {
            this(content, archivedPath, archiveError, null);
        }

        public ExportedWorkReport(byte[] content, String archivedPath, String archiveError, String fileName) {
            this.content = content;
            this.archivedPath = archivedPath;
            this.archiveError = archiveError;
            this.fileName = fileName;
        }

        /** ダウンロード・サーバーのフォルダに置くファイル名（拡張子 .xlsm を除く。マスタで決めた形）。なければ null */
        public String getFileName() { return fileName; }

        public byte[] getContent() { return content; }

        /** サーバーのフォルダに置いたファイルの場所。置かなかった・置けなかったときは null */
        public String getArchivedPath() { return archivedPath; }

        /** サーバーのフォルダに置けなかった理由。問題がなければ null（ダウンロードはできている） */
        public String getArchiveError() { return archiveError; }
    }
}
