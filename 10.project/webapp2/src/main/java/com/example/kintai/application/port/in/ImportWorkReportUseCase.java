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
     * 取り込む前の確認用に、作業報告書の中身（データのある月・勤怠の日数・週次作業報告の週数・名前欄）を読む。保存はしない。
     */
    default Preview preview(byte[] xlsm) {
        throw new UnsupportedOperationException();
    }

    /** 取り込む前の確認用の中身 */
    class Preview implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private final List<String> months;
        private final int dayCount;
        private final int weekCount;
        private final String staffName;

        public Preview(List<String> months, int dayCount, int weekCount, String staffName) {
            this.months = months;
            this.dayCount = dayCount;
            this.weekCount = weekCount;
            this.staffName = staffName;
        }

        public List<String> getMonths() { return months; }
        public int getDayCount() { return dayCount; }
        public int getWeekCount() { return weekCount; }
        public String getStaffName() { return staffName; }
    }

    /**
     * 取り込み結果。1日単位でエラーを捕捉するため、一部の日・月が失敗しても
     * 残りは取り込まれる。失敗した項目は理由付きでskippedに記録する。
     */
    class ImportResult {
        private final int importedMonthCount;
        private final List<String> skipped;
        /** 保存できた勤怠の日数 */
        private final int savedDayCount;
        /** 保存できた週次作業報告の週数 */
        private final int savedWeekCount;
        /** 取り込んだ月（例：2025-12, 2026-01, …）。画面に出して、どのシートを読んだか分かるようにする */
        private final List<String> months;

        public ImportResult(int importedMonthCount, List<String> skipped) {
            this(importedMonthCount, skipped, 0, 0, List.of());
        }

        public ImportResult(int importedMonthCount, List<String> skipped, int savedDayCount, int savedWeekCount,
                            List<String> months) {
            this.importedMonthCount = importedMonthCount;
            this.skipped = skipped;
            this.savedDayCount = savedDayCount;
            this.savedWeekCount = savedWeekCount;
            this.months = months;
        }

        public int getImportedMonthCount() { return importedMonthCount; }
        public List<String> getSkipped() { return skipped; }
        public int getSavedDayCount() { return savedDayCount; }
        public int getSavedWeekCount() { return savedWeekCount; }
        public List<String> getMonths() { return months; }
    }
}
