package com.example.entity;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

/**
 * カレンダーの一日分の情報を保持するDTO (Data Transfer Object) クラス。
 * 主に勤怠表示画面で使用される。
 */
public class CalendarDay implements Serializable {
	private static final long serialVersionUID = 1L;

	private LocalDate kintaidate;
	private DayOfWeek week;
	private boolean isholiday;
	private int id; // staff_id
	private int jikangai; // OVERTIME

	private Timestamp kintaifrom;
	private Timestamp kintaito;

	private int abstractId;
	private String memo; // REMARKS
	private String tekiyoukbn;

	private Integer correctionId;
	private Integer correctionUsTime;
	private Integer correctionMidTime;
	private Integer indirectTime;
	private Integer totalWorkTime;
	private Integer totalDirectWorkTime;

	// このマップは将来的にはユーティリティクラスへの移動を検討
	private static Map<String, DayOfWeek> japaneseDays = Map.of(
			"月", DayOfWeek.MONDAY,
			"火", DayOfWeek.TUESDAY,
			"水", DayOfWeek.WEDNESDAY,
			"木", DayOfWeek.THURSDAY,
			"金", DayOfWeek.FRIDAY,
			"土", DayOfWeek.SATURDAY,
			"日", DayOfWeek.SUNDAY);

	// Getters and Setters

	public String getTekiyoukbn() {
		return tekiyoukbn;
	}

	public void setTekiyoukbn(String tekiyoukbn) {
		this.tekiyoukbn = tekiyoukbn;
	}

	public int getJikangai() {
		return jikangai;
	}

	public void setJikangai(int jikangai) {
		this.jikangai = jikangai;
	}

	public LocalDate getKintaidate() {
		return kintaidate;
	}

	public java.util.Date getDateOfKintaidate() {
		if (kintaidate == null)
			return null;
		return Date.from(
				kintaidate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	public void setKintaidate(Date kintaidate) {
		if (kintaidate != null) {
			this.kintaidate = kintaidate.toLocalDate();
		} else {
			this.kintaidate = null;
		}
	}

	public void setKintaidate(LocalDate kintaidate) {
		this.kintaidate = kintaidate;
	}

	public DayOfWeek getWeek() {
		return this.week;
	}

	public void setWeek(String weekStr) {
		this.week = japaneseDays.get(weekStr);
	}

	public void setWeek(DayOfWeek week) {
		this.week = week;
	}

	public boolean getIsholiday() {
		return isholiday;
	}

	public void setIsholiday(boolean isholiday) {
		this.isholiday = isholiday;
	}

	// kintaidateが現在の日付かどうかを判定
	public boolean getIstoday() {
		return kintaidate != null && kintaidate.equals(LocalDate.now());
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Timestamp getKintaifrom() {
		return kintaifrom;
	}

	public void setKintaifrom(Timestamp kintaifrom) {
		this.kintaifrom = kintaifrom;
	}

	public Timestamp getKintaito() {
		return kintaito;
	}

	public void setKintaito(Timestamp kintaito) {
		this.kintaito = kintaito;
	}

	public int getAbstractId() {
		return abstractId;
	}

	public void setAbstractId(int abstractId) {
		this.abstractId = abstractId;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public Integer getCorrectionId() {
		return correctionId;
	}

	public void setCorrectionId(Integer correctionId) {
		this.correctionId = correctionId;
	}

	public Integer getCorrectionUsTime() {
		return correctionUsTime;
	}

	public void setCorrectionUsTime(Integer correctionUsTime) {
		this.correctionUsTime = correctionUsTime;
	}

	public Integer getCorrectionMidTime() {
		return correctionMidTime;
	}

	public void setCorrectionMidTime(Integer correctionMidTime) {
		this.correctionMidTime = correctionMidTime;
	}

	public Integer getIndirectTime() {
		return indirectTime;
	}

	public void setIndirectTime(Integer indirectTime) {
		this.indirectTime = indirectTime;
	}

	public Integer getTotalWorkTime() {
		return totalWorkTime;
	}

	public void setTotalWorkTime(Integer totalWorkTime) {
		this.totalWorkTime = totalWorkTime;
	}

	public Integer getTotalDirectWorkTime() {
		return totalDirectWorkTime;
	}

	public void setTotalDirectWorkTime(Integer totalDirectWorkTime) {
		this.totalDirectWorkTime = totalDirectWorkTime;
	}
}
