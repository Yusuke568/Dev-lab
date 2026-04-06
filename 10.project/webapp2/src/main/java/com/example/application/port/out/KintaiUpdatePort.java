package com.example.application.port.out;

/**
 * 勤怠情報を更新するためのポート。
 */
public interface KintaiUpdatePort {
    void update(DailyWorkRecord dailyWorkRecord);
    DailyWorkRecord findByDate(int staffId, java.time.LocalDate date);
}
