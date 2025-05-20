package manager;

import task.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TaskConversionUtils {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static String taskToCSV(Task task) {
        StringBuilder sb = new StringBuilder();

        sb.append(task.getId()).append(",");
        sb.append(task.getType()).append(",");
        sb.append(task.getName()).append(",");
        sb.append(task.getStatus()).append(",");
        sb.append(task.getDescription()).append(",");

        if (task instanceof SubTask) {
            sb.append(((SubTask) task).getEpicId());
        }

        sb.append(",");

        if (task.getStartTime() != null) {
            sb.append(task.getStartTime().format(formatter));
        }

        sb.append(",");

        if (task.getDuration() != null) {
            sb.append(task.getDuration().toMinutes());
        }

        return sb.toString();
    }

    public static Task taskFromCSV(String csvLine) {
        String[] fields = csvLine.split(",", -1);
        if (fields.length < 8) {
            throw new IllegalArgumentException("Недостаточно полей: " + csvLine);
        }

        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        TaskStatus status = TaskStatus.valueOf(fields[3]);
        String description = fields[4];
        String epicIdRaw = fields[5];
        String startTimeRaw = fields[6];
        String durationRaw = fields[7];

        LocalDateTime startTime = startTimeRaw.isBlank() ? null : LocalDateTime.parse(startTimeRaw, formatter);
        Duration duration = durationRaw.isBlank() ? null : Duration.ofMinutes(Long.parseLong(durationRaw));

        switch (type) {
            case TASK -> {
                Task task = new Task(name, description, duration, startTime);
                task.setId(id);
                task.setStatus(status);
                return task;
            }
            case EPIC -> {
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            }
            case SUBTASK -> {
                if (epicIdRaw.isBlank()) {
                    throw new IllegalArgumentException("Subtask должен содержать ID эпика");
                }
                int epicId = Integer.parseInt(epicIdRaw);
                SubTask sub = new SubTask(name, description, status, duration, startTime, epicId);
                sub.setId(id);
                return sub;
            }
            default -> throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    static String historyToString(HistoryManager manager) {
        return manager.getHistory().stream()
                .map(task -> String.valueOf(task.getId()))
                .collect(Collectors.joining(","));
    }

    static List<Integer> historyFromString(String value) {
        if (value == null || value.isBlank()) return Collections.emptyList();
        return Arrays.stream(value.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}