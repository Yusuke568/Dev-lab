package com.example.kintai.domain.model.attendance;

/**
 * 補正マスタ（補正CD）。派遣先ごとに異なる勤務時間計上ルールを、
 * 事前に登録しておいた補正時間（補正(通)・補正(深)）として管理する。
 */
public class CorrectionMaster {
    private final int id;
    private final String name;
    private final int correctionUsTime;  // 分単位
    private final int correctionMidTime; // 分単位

    public CorrectionMaster(int id, String name, int correctionUsTime, int correctionMidTime) {
        this.id = id;
        this.name = name;
        this.correctionUsTime = correctionUsTime;
        this.correctionMidTime = correctionMidTime;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getCorrectionUsTime() { return correctionUsTime; }
    public int getCorrectionMidTime() { return correctionMidTime; }

    public String getCorrectionUsTimeFormatted() { return formatMinutes(correctionUsTime); }
    public String getCorrectionMidTimeFormatted() { return formatMinutes(correctionMidTime); }

    private static String formatMinutes(int totalMinutes) {
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return String.format("%02d:%02d", hours, minutes);
    }
}
