package task;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task implements Cloneable {
    private List<SubTask> subtasks;

    public Epic(int id, String title, String description) {
        super(id, title, description, TaskStatus.NEW);
        this.subtasks = new ArrayList<>();
    }

    public void addSubtask(SubTask subtask) {
        if (subtask != null) {
            subtasks.add(subtask);
        }
    }

    public List<SubTask> getSubtasks() {
        return new ArrayList<>(subtasks);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Epic epic = (Epic) o;
        return subtasks.equals(epic.subtasks);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + subtasks.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtasks=" + subtasks +
                '}';
    }

    @Override
    public Epic clone() {
        Epic clone = (Epic) super.clone();
        for (SubTask subtask : this.subtasks) {
            clone.addSubtask(subtask.clone());
        }
        return clone;
    }
}
