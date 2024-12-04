package task;

public class SubTask extends Task implements Cloneable {
    private int epicId;

    public SubTask(int id, String title, String description, TaskStatus status, int epicId) {
        super(id, title, description, status);

        if (epicId <= 0) {
            throw new IllegalArgumentException("ID эпика должен быть положительным числом.");
        }

        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SubTask subTask = (SubTask) o;
        return epicId == subTask.epicId;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + epicId;
        return result;
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", epicId=" + epicId +
                '}';
    }



    @Override
    public SubTask clone() {
        return (SubTask) super.clone();
    }
}