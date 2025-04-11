package task;

import java.util.Objects;

public class SubTask extends Task {
    private int epicId;

    public SubTask(int id, String title, String description, TaskStatus status, int epicId) {
        super(id, title, description, status);
        if (id == epicId) {
            throw new IllegalArgumentException("Подзадача не может быть своим собственным эпиком.");
        }
        setEpicId(epicId);
    }

    public SubTask(String title, String description, TaskStatus status, int epicId) {
        this(0, title, description, status, epicId);
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        if (epicId <= 0) {
            throw new IllegalArgumentException("ID эпика должен быть положительным числом.");
        }
        this.epicId = epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubTask)) return false;
        if (!super.equals(o)) return false;
        SubTask subTask = (SubTask) o;
        return epicId == subTask.epicId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicId);
    }

    @Override
    public String toString() {
        return super.toString() + ", epicId=" + epicId;
    }
}