package manager;

import task.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final Path file;

    public FileBackedTaskManager(Path file) {
        this.file = file;
        loadFromFile(); // Загружаем данные из файла при создании
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
    public void updateSubTask(SubTask subTask) {
        super.updateSubTask(subTask);
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

    private void save() {
        try {
            List<String> lines = new ArrayList<>();
            Files.write(file, lines, StandardOpenOption.TRUNCATE_EXISTING); // Очистка перед записью

            for (Task task : getAllTasks()) {
                lines.add(TaskConverter.taskToCSV(task));
            }
            for (Epic epic : getAllEpics()) {
                lines.add(TaskConverter.taskToCSV(epic));
            }
            for (SubTask subTask : getAllSubtasks()) {
                lines.add(TaskConverter.taskToCSV(subTask));
            }
            Files.write(file, lines, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении задач в файл.", e);
        }
    }

    public static FileBackedTaskManager loadFromFile(Path file) {
        return new FileBackedTaskManager(file);
    }

    private void loadFromFile() {
        if (!Files.exists(file)) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(file);
            Map<Integer, Epic> loadedEpics = new HashMap<>();
            List<SubTask> loadedSubTasks = new ArrayList<>();

            for (String line : lines) {
                try {
                    Task task = TaskConverter.csvToTask(line);
                    if (task instanceof Epic) {
                        loadedEpics.put(task.getId(), (Epic) task);
                        super.createEpic((Epic) task);
                    } else if (task instanceof SubTask) {
                        loadedSubTasks.add((SubTask) task);
                    } else {
                        super.createTask(task);
                    }
                } catch (IllegalArgumentException e) {
                    throw new ManagerSaveException("Ошибка при загрузке задач: некорректный формат CSV", e);
                }
            }

            for (SubTask subTask : loadedSubTasks) {
                if (loadedEpics.containsKey(subTask.getEpicId())) {
                    super.createSubTask(subTask);
                } else {
                    throw new ManagerSaveException("Ошибка: Подзадача ссылается на несуществующий эпик.");
                }
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при чтении файла", e);
        }
    }
}
