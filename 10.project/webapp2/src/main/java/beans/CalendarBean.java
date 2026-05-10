package beans;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

// Removed JsonDeserialize import as it's not needed for Timestamp if handled correctly or new deserializer will be required


/**
 * 勤怠カレンダー1日分のデータを保持するBean。
 * DB（kttb_work_month）および画面表示用の中間オブジェクトとして利用。
 *
 * @author 中島祐介
 * @version 1.00
 * @since 2026-05-09
 * @see java.time.LocalDate
 */


public class CalendarBean {

	private LocalDate kintaidate;
	private DayOfWeek week;
	private boolean isholiday;
	//勤怠属性(プロパティ)
	private int id;
	private int jikangai; // OVERTIME
	
	// Changed from Time to Timestamp, removed JsonDeserialize
	private String kintaifrom; 
	private String kintaito;
	
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
	
	public String getJapaneseWeek() {
	    return switch (week) {
	        case MONDAY -> "月";
	        case TUESDAY -> "火";
	        case WEDNESDAY -> "水";
	        case THURSDAY -> "木";
	        case FRIDAY -> "金";
	        case SATURDAY -> "土";
	        case SUNDAY -> "日";
	    };
	}
	
	private static final Map<String, DayOfWeek> STR_TO_DAY = Map.of(
		    "MON", DayOfWeek.MONDAY,
		    "TUE", DayOfWeek.TUESDAY,
		    "WED", DayOfWeek.WEDNESDAY,
		    "THU", DayOfWeek.THURSDAY,
		    "FRI", DayOfWeek.FRIDAY,
		    "SAT", DayOfWeek.SATURDAY,
		    "SUN", DayOfWeek.SUNDAY
		);

		@JsonSetter("week")
		public void setWeek(String s) {
		    this.week = STR_TO_DAY.get(s);
		}
	
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

	@JsonIgnore
	public void setKintaidate(Date kintaidate) {
		this.kintaidate = kintaidate.toLocalDate();
	}

	public void setKintaidate(LocalDate kintaidate) { // Added for direct LocalDate setting
		this.kintaidate = kintaidate;
	}
	
	@JsonSetter("kintaidate")
	public void setKintaidate(String date) {
	    if (date == null || date.isEmpty()) {
	        this.kintaidate = null;
	    } else {
	        this.kintaidate = LocalDate.parse(date);
	    }
	}
	
	
	public DayOfWeek getWeek() {
		return this.week;
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
	
	@JsonSetter("kintaifrom")
	public void setKintaifrom(String kintaifrom) {
	    this.kintaifrom = kintaifrom;
	}

	@JsonSetter("kintaito")
	public void setKintaito(String kintaito) {
	    this.kintaito = kintaito;
	}

	// Changed from Time to Timestamp
	public String getKintaifrom() {
		return kintaifrom;
	}


	// Changed from Time to Timestamp
	public String getKintaito() {
		return kintaito;
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
