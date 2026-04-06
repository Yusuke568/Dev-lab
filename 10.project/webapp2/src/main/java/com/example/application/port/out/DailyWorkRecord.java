package com.example.application.port.out;

import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * 永続化層へ渡すための勤怠データレコード。
 */
public class DailyWorkRecord {
    private int id;
    private LocalDate kintaidate;
    private String week;
    private Integer correctionId;
    private Timestamp kintaifrom;
    private Timestamp kintaito;
    private Integer correctionUsTime;
    private Integer correctionMidTime;
    private Integer indirectTime;
    private Integer totalWorkTime;
    private Integer totalDirectWorkTime;
    private int jikangai;
    private int abstractId;
    private String memo;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDate getKintaidate() { return kintaidate; }
    public void setKintaidate(LocalDate kintaidate) { this.kintaidate = kintaidate; }
    public String getWeek() { return week; }
    public void setWeek(String week) { this.week = week; }
    public Integer getCorrectionId() { return correctionId; }
    public void setCorrectionId(Integer correctionId) { this.correctionId = correctionId; }
    public Timestamp getKintaifrom() { return kintaifrom; }
    public void setKintaifrom(Timestamp kintaifrom) { this.kintaifrom = kintaifrom; }
    public Timestamp getKintaito() { return kintaito; }
    public void setKintaito(Timestamp kintaito) { this.kintaito = kintaito; }
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
    public int getJikangai() { return jikangai; }
    public void setJikangai(int jikangai) { this.jikangai = jikangai; }
    public int getAbstractId() { return abstractId; }
    public void setAbstractId(int abstractId) { this.abstractId = abstractId; }
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
}
