import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
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

    public void updateStatus() {
        if (subtasks.isEmpty()) {
            this.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allDone = true;
        boolean allNew = true;

        for (SubTask subtask : subtasks) {
            if (subtask.getStatus() != TaskStatus.DONE) {
                allDone = false;
            }
            if (subtask.getStatus() != TaskStatus.NEW) {
                allNew = false;
            }
        }

        if (allDone) {
            this.setStatus(TaskStatus.DONE);
        } else if (allNew) {
            this.setStatus(TaskStatus.NEW);
        } else {
            this.setStatus(TaskStatus.IN_PROGRESS);
        }
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
}