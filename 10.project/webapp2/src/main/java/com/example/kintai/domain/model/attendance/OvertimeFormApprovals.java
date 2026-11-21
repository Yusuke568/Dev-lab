package com.example.kintai.domain.model.attendance;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 時間外申請書（個人別時間外勤務申請書兼実績報告書・Excel）から読んだ、日ごとの承認欄。
 * 申請の承認欄（P〜R列）と実績報告の承認欄（AD〜AF列）は、どちらも 課長／所属長／社長 の3つ。
 */
public record OvertimeFormApprovals(String staffName, List<Row> rows) implements Serializable {

    /** 承認欄の列の名前（1つ目が課長、2つ目が所属長、3つ目が社長） */
    public static final List<String> COLUMN_NAMES = List.of("課長", "所属長", "社長");

    /** 承認欄1つ：日付が入っている／「－」（ルートにない段）／空欄 */
    public record Mark(LocalDate date, boolean skipped) implements Serializable {
        public static final Mark BLANK = new Mark(null, false);
        public static final Mark SKIPPED = new Mark(null, true);

        public boolean hasDate() {
            return date != null;
        }

        /** 画面に出す文字（11/6・－・空） */
        public String getLabel() {
            if (date != null) return date.getMonthValue() + "/" + date.getDayOfMonth();
            return skipped ? "－" : "";
        }
    }

    /** 1日分：対象日、申請の承認欄、実績報告の承認欄（どちらも3つ） */
    public record Row(LocalDate date, List<Mark> planned, List<Mark> actual) implements Serializable {

        public boolean hasAnyDate() {
            return planned.stream().anyMatch(Mark::hasDate) || actual.stream().anyMatch(Mark::hasDate);
        }

        public String getPlannedLabel() {
            return label(planned);
        }

        public String getActualLabel() {
            return label(actual);
        }

        private static String label(List<Mark> marks) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < marks.size(); i++) {
                if (i > 0) sb.append(" / ");
                String l = marks.get(i).getLabel();
                sb.append(l.isEmpty() ? "　" : l);
            }
            return sb.toString();
        }
    }
}
