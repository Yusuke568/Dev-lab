package beans;

import java.sql.Date;
import java.sql.Timestamp; // Changed from java.sql.Time
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

// Removed JsonDeserialize import as it's not needed for Timestamp if handled correctly or new deserializer will be required


public class CalendarBean {

	private LocalDate kintaidate;
	private DayOfWeek week;
	private boolean isholiday;
	//勤怠属性(プロパティ)
	private int id;
	private int jikangai; // OVERTIME
	
	// Changed from Time to Timestamp, removed JsonDeserialize
	private Timestamp kintaifrom; 
	private Timestamp kintaito;
	
	private int abstractId; // Changed from String tekiyoukbn to int abstractId
	private String memo; // REMARKS
	private String tekiyoukbn;


	// New fields from work_month_table
	private Integer correctionId; // CORRECTION_ID
	private Integer correctionUsTime; // CORRECTION_US_TIME
	private Integer correctionMidTime; // CORRECTION_MID_TIME
	private Integer indirectTime; // INDIRECT_TIME
	private Integer totalWorkTime; // TOTAL_WORK_TIME
	private Integer totalDirectWorkTime; // TOTAL_DIRECT_WORK_TIME

	private static Map<String, DayOfWeek> japaneseDays = Map.of(
			"月", DayOfWeek.MONDAY,
			"火", DayOfWeek.TUESDAY,
			"水", DayOfWeek.WEDNESDAY,
			"木", DayOfWeek.THURSDAY,
			"金", DayOfWeek.FRIDAY,
			"土", DayOfWeek.SATURDAY,
			"日", DayOfWeek.SUNDAY);

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
	    if (kintaidate == null) return null;

	    return Date.from(
	        kintaidate
	            .atStartOfDay(ZoneId.systemDefault())
	            .toInstant()
	    );
	}

	public void setKintaidate(Date kintaidate) {
		this.kintaidate = kintaidate.toLocalDate();
	}

	public void setKintaidate(LocalDate kintaidate) { // Added for direct LocalDate setting
		this.kintaidate = kintaidate;
	}
	
	public DayOfWeek getWeek() {
		return this.week;
	}

	public void setWeek(String Week) {
		this.week = japaneseDays.get(Week);
	}
	public void setWeek(DayOfWeek Week) {
		this.week = Week;
	}
	

	public boolean getIsholiday() {
		return isholiday;
	}

	public void setIsholiday(boolean isholiday) {
		this.isholiday = isholiday;
	}

	//日付が現在かどうか
	public boolean getIstoday() {
		return kintaidate.equals(LocalDate.now());
	}
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	// Changed from Time to Timestamp
	public Timestamp getKintaifrom() {
		return kintaifrom;
	}
	public void setKintaifrom(Timestamp kintaifrom) { // Changed from Time to Timestamp
		this.kintaifrom = kintaifrom;
	}

	// Changed from Time to Timestamp
	public Timestamp getKintaito() {
		return kintaito;
	}
	public void setKintaito(Timestamp kintaito) { // Changed from Time to Timestamp
		this.kintaito = kintaito;
	}

	// Changed from String tekiyoukbn to int abstractId
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

	// Getters and Setters for new fields
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
