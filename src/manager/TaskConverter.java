package manager;

import task.*;

public class TaskConverter {
    public static String taskToCSV(Task task) {
        String epicId = (task.getType() == TaskType.SUBTASK) ? String.valueOf(((SubTask) task).getEpicId()) : "";
        return String.format("%d,%s,%s,%s,%s,%s",
                task.getId(),
                task.getType(),
                task.getTitle(),
                task.getStatus(),
                task.getDescription(),
                epicId);
    }

    public static Task CSVtoTask(String line) {
        String[] fields = line.split(",");

        if (fields.length < 5) {
            throw new IllegalArgumentException("Некорректный формат CSV: " + line);
        }

        int id = Integer.parseInt(fields[0].trim());
        TaskType type = TaskType.valueOf(fields[1].trim());
        String title = fields[2].trim();
        TaskStatus status = TaskStatus.valueOf(fields[3].trim());
        String description = fields[4].trim();

        switch (type) {
            case TASK:
                return new Task(id, title, description, status);
            case EPIC:
                return new Epic(id, title, description);
            case SUBTASK:
                if (fields.length < 6) {
                    throw new IllegalArgumentException("Для подзадачи не указан ID эпика: " + line);
                }
                int epicId = Integer.parseInt(fields[5].trim());
                return new SubTask(id, title, description, status, epicId);
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }
}
