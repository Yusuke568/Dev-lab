package com.example.kintai.domain.model.attendance;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;

/**
 * 勤務時間（値オブジェクト）。
 *
 * 開始時刻と終了時刻を持ち、勤務時間を計算する不変オブジェクト。
 * 終了時刻が開始時刻より前なら、日をまたいで翌日に終わったものとして扱う（例：9:00〜翌1:00）。
 */
public final class WorkTime implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LocalTime startTime;
    private final LocalTime endTime;
    /** 終了時刻が翌日か（日またぎ） */
    private final boolean endsNextDay;

    /**
     * 終了時刻が開始時刻より前なら翌日の終了とみなす。
     */
    public WorkTime(LocalTime startTime, LocalTime endTime) {
        Objects.requireNonNull(startTime, "startTime must not be null");
        Objects.requireNonNull(endTime, "endTime must not be null");
        this.startTime = startTime;
        this.endTime = endTime;
        this.endsNextDay = endTime.isBefore(startTime);
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    /** 終了時刻が翌日か（日またぎの勤務か）。 */
    public boolean isEndsNextDay() {
        return endsNextDay;
    }

    /** 開始から終了までの長さ（日またぎなら翌日の終了時刻まで）。 */
    public Duration getDuration() {
        Duration duration = Duration.between(startTime, endTime);
        return endsNextDay ? duration.plusDays(1) : duration;
    }

    /** 開始時刻を、開始日の0時からの分で返す。 */
    public int getStartMinuteOfDay() {
        return startTime.getHour() * 60 + startTime.getMinute();
    }

    /** 終了時刻を、開始日の0時からの分で返す（日またぎなら1440分以上になる）。 */
    public int getEndMinuteFromStartDay() {
        int end = endTime.getHour() * 60 + endTime.getMinute();
        return endsNextDay ? end + 24 * 60 : end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkTime workTime = (WorkTime) o;
        return endsNextDay == workTime.endsNextDay
                && startTime.equals(workTime.startTime) && endTime.equals(workTime.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime, endsNextDay);
    }

    @Override
    public String toString() {
        return "WorkTime{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                (endsNextDay ? " (翌日)" : "") +
                '}';
    }
}
