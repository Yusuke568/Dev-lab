package com.example.kintai.application.port.in;

import com.example.kintai.domain.model.employee.EmployeeId;

import java.io.InputStream;
import java.util.List;

/**
 * 社内所定の「作業報告書」Excel（.xlsm）を読み込み、勤怠データ・週次作業報告を一括登録するユースケース。
 */
public interface ImportWorkReportUseCase {

    ImportResult importFile(EmployeeId employeeId, InputStream xlsmInputStream);

    /**
     * 取り込み結果。1日単位でエラーを捕捉するため、一部の日・月が失敗しても
     * 残りは取り込まれる。失敗した項目は理由付きでskippedに記録する。
     */
    class ImportResult {
        private final int importedMonthCount;
        private final List<String> skipped;

        public ImportResult(int importedMonthCount, List<String> skipped) {
            this.importedMonthCount = importedMonthCount;
            this.skipped = skipped;
        }

        public int getImportedMonthCount() { return importedMonthCount; }
        public List<String> getSkipped() { return skipped; }
    }
}
