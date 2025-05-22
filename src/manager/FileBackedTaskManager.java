package manager;

import task.Epic;
import task.SubTask;
import task.Task;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final Path filePath;

    public FileBackedTaskManager(Path filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("filePath не может быть null");
        }
        this.filePath = filePath;
    }

    @Override
    public Task createTask(Task task) {
        Task created = super.createTask(task);
        save();
        return created;
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic created = super.createEpic(epic);
        save();
        return created;
    }

    @Override
    public SubTask createSubTask(SubTask subTask) {
        SubTask created = super.createSubTask(subTask);
        save();
        return created;
    }

    @Override
    public Task updateTask(Task task) {
        Task updated = super.updateTask(task);
        save();
        return updated;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic updated = super.updateEpic(epic);
        save();
        return updated;
    }

    @Override
    public SubTask updateSubTask(SubTask subtask) {
        SubTask updated = super.updateSubTask(subtask);
        save();
        return updated;
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

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public Optional<Task> getTask(int id) {
        Optional<Task> task = super.getTask(id);
        save();
        return task;
    }

    @Override
    public Optional<SubTask> getSubtask(int id) {
        Optional<SubTask> sub = super.getSubtask(id);
        save();
        return sub;
    }

    @Override
    public Optional<Epic> getEpic(int id) {
        Optional<Epic> epic = super.getEpic(id);
        save();
        return epic;
    }

    protected void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            writer.write("id,type,name,status,description,epic,startTime,duration");
            writer.newLine();

            for (Task task : getAllTasks()) {
                writer.write(TaskConversionUtils.taskToCSV(task));
                writer.newLine();
            }
            for (Epic epic : getAllEpics()) {
                writer.write(TaskConversionUtils.taskToCSV(epic));
                writer.newLine();
            }
            for (SubTask sub : getAllSubTasks()) {
                writer.write(TaskConversionUtils.taskToCSV(sub));
                writer.newLine();
            }

            writer.newLine();
            String history = TaskConversionUtils.historyToString(historyManager);
            if (!history.isBlank()) {
                writer.write(history);
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл", e);
        }
    }

    public static FileBackedTaskManager loadFromFile(Path filePath) {
        FileBackedTaskManager manager = new FileBackedTaskManager(filePath);
        List<String> lines;

        try {
            lines = Files.readAllLines(filePath);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при чтении файла", e);
        }

        if (lines.size() <= 1) {  // файл пустой или только заголовок
            return manager;
        }

        Map<Integer, Task> allTasks = new HashMap<>();
        boolean readingHistory = false;
        List<Integer> historyIds = new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);

            if (line.isBlank()) {
                readingHistory = true;
                continue;
            }

            if (!readingHistory) {
                try {
                    Task task = TaskConversionUtils.taskFromCSV(line);
                    int id = task.getId();
                    allTasks.put(id, task);

                    if (task instanceof Epic) {
                        manager.epics.put(id, (Epic) task);
                    } else if (task instanceof SubTask) {
                        manager.subtasks.put(id, (SubTask) task);
                    } else {
                        manager.tasks.put(id, task);
                    }

                    manager.nextId = Math.max(manager.nextId, id + 1);
                } catch (Exception e) {
                    throw new ManagerSaveException("Ошибка при разборе строки: " + line, e);
                }
            } else {
                try {
                    historyIds = TaskConversionUtils.historyFromString(line);
                } catch (Exception e) {
                    throw new ManagerSaveException("Ошибка при разборе истории: " + line, e);
                }
            }
        }

        for (SubTask sub : manager.subtasks.values()) {
            Epic epic = manager.epics.get(sub.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().add(sub.getId());
            } else {
                throw new ManagerSaveException("Подзадача ссылается на несуществующий эпик: " + sub.getId());
            }
        }

        for (Epic epic : manager.epics.values()) {
            manager.recalculateEpicFields(epic);
        }

        for (int id : historyIds) {
            Task task = allTasks.get(id);
            if (task != null) {
                manager.historyManager.add(task);
            }
        }

        return manager;
    }



}