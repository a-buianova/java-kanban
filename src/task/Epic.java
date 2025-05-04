package task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends task.Task {
    private Duration duration = Duration.ZERO;     // [ТЗ-1]
    private LocalDateTime endTime;                 // [ТЗ-1]
    private final List<Integer> subtaskIds = new ArrayList<>(); // [ТЗ-2] храним id подзадач

    public Epic(String name, String description) {
        super(name, description);
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                // 👇 [ТЗ-1] расчетные поля
                ", startTime=" + startTime +
                ", duration=" + (duration != null ? duration.toMinutes() + "m" : "null") +
                ", endTime=" + endTime +
                '}';
    }
}