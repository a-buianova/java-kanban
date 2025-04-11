package manager;

import task.*;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final Path filePath;

    public FileBackedTaskManager(Path filePath) {
        this.filePath = filePath;
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
        if (!epics.containsKey(subTask.getEpicId())) {
            throw new IllegalArgumentException("Эпик с ID " + subTask.getEpicId() + " не найден.");
        }
        SubTask createdSubTask = super.createSubTask(subTask);
        save();
        return createdSubTask;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(SubTask subtask) {
        super.updateSubtask(subtask);
        save();
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

    protected void save() {
        List<String> lines = new ArrayList<>();
        lines.add("id,type,name,status,description,epic");

        for (Task task : getAllTasks()) {
            lines.add(TaskConverter.taskToCSV(task));
        }
        for (Epic epic : getAllEpics()) {
            lines.add(TaskConverter.taskToCSV(epic));
        }
        for (SubTask subTask : getAllSubtasks()) {
            lines.add(TaskConverter.taskToCSV(subTask));
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл: " + e.getMessage(), e);
        }
    }

    public static FileBackedTaskManager loadFromFile(Path filePath) {
        FileBackedTaskManager manager = new FileBackedTaskManager(filePath);

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String header = reader.readLine();

            // ✅ Разрешаем пустой файл
            if (header == null || header.isBlank()) {
                return manager;
            }

            // ✅ Проверяем корректность заголовка
            if (!header.equals("id,type,name,status,description,epic")) {
                throw new ManagerSaveException("Неверный формат заголовка файла.");
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                Task task;
                try {
                    task = TaskConverter.taskFromCSV(line);
                } catch (IllegalArgumentException e) {
                    throw new ManagerSaveException("Ошибка при чтении строки: " + line, e);
                }

                switch (task.getType()) {
                    case TASK:
                        manager.tasks.put(task.getId(), task);
                        break;
                    case EPIC:
                        manager.epics.put(task.getId(), (Epic) task);
                        break;
                    case SUBTASK:
                        SubTask subTask = (SubTask) task;
                        Epic epic = manager.epics.get(subTask.getEpicId());
                        if (epic == null) {
                            throw new ManagerSaveException("Эпик с ID " + subTask.getEpicId() + " не найден.");
                        }
                        manager.subtasks.put(subTask.getId(), subTask);
                        epic.addSubtask(subTask.getId());
                        break;
                }

                manager.updateNextIdIfNecessary(task.getId());
            }

        } catch (IOException | IllegalArgumentException e) {
            throw new ManagerSaveException("Ошибка при загрузке из файла: " + e.getMessage(), e);
        }

        return manager;
    }
}