package domain.attendance;

import java.util.Objects;

/**
 * 作業明細セグメント
 * （直接労働/間接労働ごとの内訳分数と作業メモ）
 */
public class WorkTimeSegment {
    private final String id;
    private final WorkCategory category;
    private final int durationMinutes;
    private final String taskDescription;

    public WorkTimeSegment(String id, WorkCategory category, int durationMinutes, String taskDescription) {
        if (durationMinutes < 0) {
            throw new IllegalArgumentException("作業分数は0以上である必要があります。");
        }
        this.id = Objects.requireNonNull(id, "IDは必須です。");
        this.category = Objects.requireNonNull(category, "作業区分は必須です。");
        this.durationMinutes = durationMinutes;
        this.taskDescription = taskDescription;
    }

    public String getId() { return id; }
    public WorkCategory getCategory() { return category; }
    public int getDurationMinutes() { return durationMinutes; }
    public String getTaskDescription() { return taskDescription; }
}
