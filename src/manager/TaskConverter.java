package manager;

import task.*;

public class TaskConverter {

    public static String taskToCSV(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append(task.getId()).append(",");
        sb.append(task.getType()).append(",");
        sb.append(task.getTitle()).append(",");
        sb.append(task.getStatus()).append(",");
        sb.append(task.getDescription()).append(",");

        if (task.getType() == TaskType.SUBTASK) {
            sb.append(((SubTask) task).getEpicId());
        } else {
            sb.append("");
        }

        return sb.toString();
    }

    public static Task taskFromCSV(String csvLine) {
        String[] fields = csvLine.split(",", -1);

        if (fields.length < 5) {
            throw new IllegalArgumentException("Недостаточно полей для парсинга задачи: " + csvLine);
        }

        try {
            int id = Integer.parseInt(fields[0]);
            TaskType type = TaskType.valueOf(fields[1]);
            String title = fields[2];
            TaskStatus status = TaskStatus.valueOf(fields[3]);
            String description = fields[4];

            switch (type) {
                case TASK:
                    return new Task(id, title, description, status);
                case EPIC:
                    return new Epic(id, title, description);
                case SUBTASK:
                    if (fields.length < 6 || fields[5].isBlank()) {
                        throw new IllegalArgumentException("Подзадача без ID эпика: " + csvLine);
                    }
                    int epicId = Integer.parseInt(fields[5]);
                    return new SubTask(id, title, description, status, epicId);
                default:
                    throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка парсинга строки: " + csvLine, e);
        }
    }
}