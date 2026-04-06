package com.example.kintai.domain.model.attendance;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;

/**
 * 勤務時間（値オブジェクト）
 *
 * 開始時刻と終了時刻を持ち、勤務時間を計算する責務を持つ不変オブジェクト。
 */
public final class WorkTime implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LocalTime startTime;
    private final LocalTime endTime;

    public WorkTime(LocalTime startTime, LocalTime endTime) {
        Objects.requireNonNull(startTime, "startTime must not be null");
        Objects.requireNonNull(endTime, "endTime must not be null");

        if (endTime.isBefore(startTime)) {
            // Note: This logic might need to be adjusted for shifts spanning midnight.
            // For now, we assume same-day shifts.
            throw new IllegalArgumentException("End time must be after start time.");
        }
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * 勤務時間（期間）を計算します。
     * @return 勤務時間の Duration
     */
    public Duration getDuration() {
        return Duration.between(startTime, endTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkTime workTime = (WorkTime) o;
        return startTime.equals(workTime.startTime) && endTime.equals(workTime.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime);
    }

    @Override
    public String toString() {
        return "WorkTime{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
