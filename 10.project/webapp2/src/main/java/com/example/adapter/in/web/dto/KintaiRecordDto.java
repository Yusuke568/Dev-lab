package com.example.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * JavaScriptから送信される勤怠レコードのDTO。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KintaiRecordDto {
    private int id; // staffId
    private String kintaidate; // yyyy-MM-dd
    private String week;
    private String kintaifrom; // HH:mm
    private String kintaito;   // HH:mm
    private int jikangai;      // 分単位の残業時間
    private Integer abstractId;
    private String memo;
    private Integer correctionId;
    private Integer correctionUsTime;
    private Integer correctionMidTime;
    private Integer indirectTime;
    private Integer totalWorkTime;
    private Integer totalDirectWorkTime;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getKintaidate() { return kintaidate; }
    public void setKintaidate(String kintaidate) { this.kintaidate = kintaidate; }
    public String getWeek() { return week; }
    public void setWeek(String week) { this.week = week; }
    public String getKintaifrom() { return kintaifrom; }
    public void setKintaifrom(String kintaifrom) { this.kintaifrom = kintaifrom; }
    public String getKintaito() { return kintaito; }
    public void setKintaito(String kintaito) { this.kintaito = kintaito; }
    public int getJikangai() { return jikangai; }
    public void setJikangai(int jikangai) { this.jikangai = jikangai; }
    public Integer getAbstractId() { return abstractId; }
    public void setAbstractId(Integer abstractId) { this.abstractId = abstractId; }
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
    public Integer getCorrectionId() { return correctionId; }
    public void setCorrectionId(Integer correctionId) { this.correctionId = correctionId; }
    public Integer getCorrectionUsTime() { return correctionUsTime; }
    public void setCorrectionUsTime(Integer correctionUsTime) { this.correctionUsTime = correctionUsTime; }
    public Integer getCorrectionMidTime() { return correctionMidTime; }
    public void setCorrectionMidTime(Integer correctionMidTime) { this.correctionMidTime = correctionMidTime; }
    public Integer getIndirectTime() { return indirectTime; }
    public void setIndirectTime(Integer indirectTime) { this.indirectTime = indirectTime; }
    public Integer getTotalWorkTime() { return totalWorkTime; }
    public void setTotalWorkTime(Integer totalWorkTime) { this.totalWorkTime = totalWorkTime; }
    public Integer getTotalDirectWorkTime() { return totalDirectWorkTime; }
    public void setTotalDirectWorkTime(Integer totalDirectWorkTime) { this.totalDirectWorkTime = totalDirectWorkTime; }
}
