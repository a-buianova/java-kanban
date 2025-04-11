import manager.FileBackedTaskManager;
import manager.TaskManager;
import task.Epic;
import task.SubTask;
import task.TaskStatus;
import task.*;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
            TaskManager manager = new FileBackedTaskManager(Path.of("tasks.csv")); // FileBackedTaskManager

            // Создание задач
            Task task = new Task("Простая задача", "Описание простой задачи");
            manager.createTask(task);

            Epic epic = new Epic("Эпик 1", "Описание эпика");
            manager.createEpic(epic);

            SubTask subTask1 = new SubTask("Подзадача 1", "Описание подзадачи", TaskStatus.NEW, epic.getId());
            SubTask subTask2 = new SubTask("Подзадача 2", "Описание подзадачи", TaskStatus.DONE, epic.getId());

            manager.createSubTask(subTask1);
            manager.createSubTask(subTask2);

            // Вывод всех задач
            System.out.println("Все задачи:");
            for (Task t : manager.getAllTasks()) {
                System.out.println(t);
            }

            System.out.println("\nВсе эпики:");
            for (Epic e : manager.getAllEpics()) {
                System.out.println(e);
            }

            System.out.println("\nВсе подзадачи:");
            for (SubTask s : manager.getAllSubtasks()) {
                System.out.println(s);
            }

            // Получение истории
            manager.getTaskById(task.getId());
            manager.getEpicById(epic.getId());
            manager.getSubtaskById(subTask1.getId());

            System.out.println("\nИстория:");
            for (Task h : manager.getHistory()) {
                System.out.println(h);
            }
        }
    }
