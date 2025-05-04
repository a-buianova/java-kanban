import manager.FileBackedTaskManager;
import task.*;

import java.nio.file.Path;


public class Main {
    public static void main(String[] args) {
        Path path = Path.of("tasks.csv");

        System.out.println("=== Шаг 1: Создание задач и сохранение в файл ===");

        // Создание менеджера и задач
        FileBackedTaskManager manager = new FileBackedTaskManager(path);

        Task task = new Task("Простая задача", "Описание простой задачи");
        manager.createTask(task);

        Epic epic = new Epic("Эпик 1", "Описание эпика");
        manager.createEpic(epic);

        SubTask subTask1 = new SubTask("Подзадача 1", "Описание подзадачи 1", TaskStatus.NEW, epic.getId());
        SubTask subTask2 = new SubTask("Подзадача 2", "Описание подзадачи 2", TaskStatus.DONE, epic.getId());

        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);

        manager.getTaskById(task.getId());
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subTask1.getId());

        System.out.println("\n=== Шаг 2: Загрузка менеджера из файла ===");

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(path);

        System.out.println("\n=== Загруженные задачи ===");
        for (Task t : loadedManager.getAllTasks()) {
            System.out.println(t);
        }

        System.out.println("\n=== Загруженные эпики ===");
        for (Epic e : loadedManager.getAllEpics()) {
            System.out.println(e);
        }

        System.out.println("\n=== Загруженные подзадачи ===");
        for (SubTask s : loadedManager.getAllSubtasks()) {
            System.out.println(s);
        }

        System.out.println("\n=== Восстановленная история ===");
        for (Task h : loadedManager.getHistory()) {
            System.out.println(h);
        }
    }
}