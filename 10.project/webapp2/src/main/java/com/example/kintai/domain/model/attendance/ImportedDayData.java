package com.example.kintai.domain.model.attendance;

import java.time.LocalDate;

/**
 * 作業報告書Excelから読み取った1日分の勤怠データ（取り込み用の一時データ）。
 * 永続化は行わず、既存のUpdateAttendanceUseCaseに変換して渡すための中間データ。
 */
public class ImportedDayData {

    private final LocalDate date;
    private final String startTime; // "HH:mm" or null
    private final String endTime;   // "HH:mm" or null
    private final Integer correctionUsTime; // 分
    private final Integer correctionMidTime; // 分
    private final Integer indirectTime; // 分
    private final String category; // AN列の値（例: "有休"）。未対応の値はそのままmemoに残すのみで区分には反映しない
    private final String memo;

    public ImportedDayData(LocalDate date, String startTime, String endTime,
                            Integer correctionUsTime, Integer correctionMidTime, Integer indirectTime,
                            String category, String memo) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.correctionUsTime = correctionUsTime;
        this.correctionMidTime = correctionMidTime;
        this.indirectTime = indirectTime;
        this.category = category;
        this.memo = memo;
    }

    public LocalDate getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public Integer getCorrectionUsTime() { return correctionUsTime; }
    public Integer getCorrectionMidTime() { return correctionMidTime; }
    public Integer getIndirectTime() { return indirectTime; }
    public String getCategory() { return category; }
    public String getMemo() { return memo; }

    /** 出退勤・補正・間接時間・摘要・備考のいずれも入っていないか。 */
    public boolean isEmpty() {
        return startTime == null && endTime == null
                && (correctionUsTime == null || correctionUsTime == 0)
                && (correctionMidTime == null || correctionMidTime == 0)
                && (indirectTime == null || indirectTime == 0)
                && (category == null || category.isBlank())
                && (memo == null || memo.isBlank());
    }
}
