package beans;

import java.sql.Date;
import java.sql.Timestamp; // Use Timestamp for datetime fields

public class KintaiBean {
	
	//勤怠属性(プロパティ)
	private int id; // STAFF_ID
	private Date kintaiDate; // WORK_DATE
	private String workWeek; // WORK_WEEK
	private Integer correctionId; // CORRECTION_ID (Integer to allow null)
	private Timestamp jobFromTime; // JOB_FROM_TIME
	private Timestamp jobToTime; // JOB_TO_TIME
	private Integer correctionUsTime; // CORRECTION_US_TIME (Integer to allow null)
	private Integer correctionMidTime; // CORRECTION_MID_TIME (Integer to allow null)
	private Integer indirectTime; // INDIRECT_TIME (Integer to allow null)
	private Integer totalWorkTime; // TOTAL_WORK_TIME (Integer to allow null)
	private Integer totalDirectWorkTime; // TOTAL_DIRECT_WORK_TIME (Integer to allow null)
	private Integer overtime; // OVERTIME (Integer to allow null)
	private int abstractId; // ABSTRACT_ID
	private String remarks; // REMARKS
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Date getKintaiDate() {
		return kintaiDate;
	}
	public void setKintaiDate(Date kintaiDate) {
		this.kintaiDate = kintaiDate;
	}
	public String getWorkWeek() {
		return workWeek;
	}
	public void setWorkWeek(String workWeek) {
		this.workWeek = workWeek;
	}
	public Integer getCorrectionId() {
		return correctionId;
	}
	public void setCorrectionId(Integer correctionId) {
		this.correctionId = correctionId;
	}
	public Timestamp getJobFromTime() {
		return jobFromTime;
	}
	public void setJobFromTime(Timestamp jobFromTime) {
		this.jobFromTime = jobFromTime;
	}
	public Timestamp getJobToTime() {
		return jobToTime;
	}
	public void setJobToTime(Timestamp jobToTime) {
		this.jobToTime = jobToTime;
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
	public Integer getOvertime() {
		return overtime;
	}
	public void setOvertime(Integer overtime) {
		this.overtime = overtime;
	}
	public int getAbstractId() {
		return abstractId;
	}
	public void setAbstractId(int abstractId) {
		this.abstractId = abstractId;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
}
