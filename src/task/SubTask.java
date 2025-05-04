package task;


import java.time.Duration;
import java.time.LocalDateTime;

public class SubTask extends task.Task {
    private int epicId;

    public SubTask(String name, String description, int epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    public SubTask(String name, String description, Duration duration, LocalDateTime startTime, int epicId) {
        super(name, description, duration, startTime);
        this.epicId = epicId;
    }

    public SubTask(String name, String description, TaskStatus status, Duration duration, LocalDateTime startTime, int epicId) {
        super(name, description, duration, startTime);
        this.status = status;
        this.epicId = epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", epicId=" + epicId +
                ", startTime=" + startTime +
                ", duration=" + (duration != null ? duration.toMinutes() + "m" : "null") +
                ", endTime=" + getEndTime() +
                '}';
    }
}