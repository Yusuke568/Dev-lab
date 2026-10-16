package com.example.kintai.adapter.in.web.dto;

/**
 * 週次作業報告保存APIのリクエストボディ用DTO（JSON <-> Java）。
 */
public class WeeklyWorkReportDto {

    private String weekEndDate; // "yyyy-MM-dd"
    private String clientName;
    private String projectName;
    private String processName;
    private String progressStatus;
    private String progressDetail;
    private String personalNote;

    public String getWeekEndDate() { return weekEndDate; }
    public void setWeekEndDate(String weekEndDate) { this.weekEndDate = weekEndDate; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }

    public String getProgressStatus() { return progressStatus; }
    public void setProgressStatus(String progressStatus) { this.progressStatus = progressStatus; }

    public String getProgressDetail() { return progressDetail; }
    public void setProgressDetail(String progressDetail) { this.progressDetail = progressDetail; }

    public String getPersonalNote() { return personalNote; }
    public void setPersonalNote(String personalNote) { this.personalNote = personalNote; }
}
