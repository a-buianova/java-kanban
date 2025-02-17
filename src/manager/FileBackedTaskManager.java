package manager;

import task.Task;
import task.Epic;
import task.SubTask;
import task.TaskStatus;
import task.TaskType;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    // Method to save the current state to a file
    protected void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic\n");

            for (Task task : getAllTasks()) {
                writer.write(toString(task) + "\n");
            }
            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic) + "\n");
                for (SubTask subTask : getSubtasksForEpic(epic.getId())) {
                    writer.write(toString(subTask) + "\n");
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Error saving data to file", e);
        }
    }

    // Static method to load a manager from a file
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (int i = 1; i < lines.size(); i++) { // Skip header line
                Task task = fromString(lines.get(i));
                if (task instanceof Epic) {
                    manager.createEpic((Epic) task);
                } else if (task instanceof SubTask) {
                    manager.createSubTask((SubTask) task);
                } else {
                    manager.createTask(task);
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Error loading data from file", e);
        }
        return manager;
    }

    // Convert task to a CSV string
    private String toString(Task task) {
        String epicId = task instanceof SubTask ? String.valueOf(((SubTask) task).getEpicId()) : "";
        return String.format("%d,%s,%s,%s,%s,%s",
                task.getId(),
                task instanceof Epic ? TaskType.EPIC : task instanceof SubTask ? TaskType.SUBTASK : TaskType.TASK,
                task.getTitle(),
                task.getStatus(),
                task.getDescription(),
                epicId);
    }

    // Convert a CSV string to a Task object
    private static Task fromString(String value) {
        String[] fields = value.split(",");
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
                int epicId = Integer.parseInt(fields[5]);
                return new SubTask(id, title, description, status, epicId);
            default:
                throw new IllegalArgumentException("Unknown task type: " + type);
        }
    }

    @Override
    public Task createTask(Task task) {
        Task createdTask = super.createTask(task);
        save();
        return createdTask;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic createdEpic = super.createEpic(epic);
        save();
        return createdEpic;
    }

    @Override
    public SubTask createSubTask(SubTask subTask) {
        SubTask createdSubTask = super.createSubTask(subTask);
        save();
        return createdSubTask;
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

}
