package beans;

import java.sql.Date;
import java.sql.Time;

public class KintaiBean {
	
	//勤怠属性(プロパティ)
	private int id;
	private Date kintaidate;
	private Time kintaifrom;
	private Time kintaito;
	private String tekiyoukbn;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Date getKintaiDate() {
		return kintaidate;
	}
	public void setKintaiDate(Date kintaidate) {
		this.kintaidate = kintaidate;
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
	
	
}
