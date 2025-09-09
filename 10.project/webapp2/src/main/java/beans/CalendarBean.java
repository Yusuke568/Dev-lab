package beans;

import java.sql.Date;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

public class CalendarBean {

	private LocalDate kintaidate;
	private DayOfWeek week;
	private boolean isholiday;
	//勤怠属性(プロパティ)
	private int id;
	private Time kintaifrom;
	private Time kintaito;
	private String tekiyoukbn;
	private String memo;

	private static Map<String, DayOfWeek> japaneseDays = Map.of(
			"月", DayOfWeek.MONDAY,
			"火", DayOfWeek.TUESDAY,
			"水", DayOfWeek.WEDNESDAY,
			"木", DayOfWeek.THURSDAY,
			"金", DayOfWeek.FRIDAY,
			"土", DayOfWeek.SATURDAY,
			"日", DayOfWeek.SUNDAY);

	public LocalDate getKintaidate() {
		return kintaidate;
	}

	public void setKintaidate(Date kintaidate) {
		this.kintaidate = kintaidate.toLocalDate();
	}

	public DayOfWeek getWeek() {
		return week;
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
	public Time getKintaifrom() {
		return kintaifrom;
	}
	public void setKintaifrom(Time time) {
		this.kintaifrom = time;
	}
	public Time getKintaito() {
		return kintaito;
	}
	public void setKintaito(Time kintaito) {
		this.kintaito = kintaito;
	}
	public String getTekiyoukbn() {
		return tekiyoukbn;
	}
	public void setTekiyoukbn(String tekiyoukbn) {
		this.tekiyoukbn = tekiyoukbn;
	}	
	
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	

}
