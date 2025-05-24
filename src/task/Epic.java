package task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description);
        this.duration = Duration.ZERO;
        this.startTime = null;
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    // Можно рассчитывать duration по подзадачам в менеджере (в getDuration)
    @Override
    public Duration getDuration() {
        return duration; // из родительского класса
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    @Override
    public LocalDateTime getEndTime() {
        return (startTime != null && duration != null) ? startTime.plus(duration) : null;
    }

    public void setEndTime(LocalDateTime endTime) {
        if (startTime != null && endTime != null) {
            this.duration = Duration.between(startTime, endTime);
        }
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
                ", startTime=" + startTime +
                ", duration=" + (duration != null ? duration.toMinutes() + "m" : "null") +
                ", endTime=" + getEndTime() +
                '}';
    }
}