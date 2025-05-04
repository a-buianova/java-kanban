import manager.InMemoryTaskManager;
import manager.TaskManager;
import task.Epic;
import task.SubTask;
import task.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {

        TaskManager manager = new InMemoryTaskManager();

        Epic epic1 = new Epic("Организация праздника", "Организовать день рождения");
        Epic epic2 = new Epic("Организация вечеринки", "Организовать вечеринку");

        manager.createEpic(epic1);
        manager.createEpic(epic2);

        SubTask epicOneTaskOne = new SubTask(
            "Забронировать ресторан",
            "Найти и забронировать ресторан",
            Duration.ofMinutes(60),
            LocalDateTime.of(2025, 5, 2, 10, 0),
            epic1.getId()
        );

        SubTask epicOneTaskTwo = new SubTask(
            "Отправить приглашения",
            "Разослать пригласительные",
            Duration.ofMinutes(30),
            LocalDateTime.of(2025, 5, 2, 12, 0),
            epic1.getId()
        );

        SubTask epicTwoTaskOne = new SubTask(
            "Приготовить торт",
            "Испечь торт",
            Duration.ofMinutes(45),
            LocalDateTime.of(2025, 5, 3, 14, 0),
            epic2.getId()
        );

        manager.createSubTask(epicOneTaskOne);
        manager.createSubTask(epicOneTaskTwo);
        manager.createSubTask(epicTwoTaskOne);

        System.out.println("Список эпиков: " + manager.getAllEpics());

        epicTwoTaskOne.setStatus(TaskStatus.DONE);
        manager.updateSubTask(epicTwoTaskOne);
        System.out.println("Статус эпика 2 после изменения подзадачи: " + epic2.getStatus());

        manager.deleteSubtask(epicOneTaskOne.getId());
        System.out.println("Статус эпика 1 после удаления подзадачи: " + epic1.getStatus());

        System.out.println("Список задач после удаления подзадачи: " + manager.getAllTasks());
        System.out.println("Список эпиков после удаления подзадачи: " + manager.getAllEpics());

        manager.getEpic(epic1.getId());
        manager.getSubtask(epicTwoTaskOne.getId());
        System.out.println("История просмотров задач: " + manager.getHistory());

        System.out.println("\n== Приоритетная сортировка задач ==");
        manager.getPrioritizedTasks().forEach(System.out::println);

        System.out.println("\n== Проверка пересечений ==");
        SubTask conflictTask = new SubTask(
                "Пересекающаяся задача",
                "Задача пересекается по времени с существующей",
                Duration.ofMinutes(45),
                LocalDateTime.of(2025, 5, 2, 12, 15), // пересекается с epicOneTaskTwo
                epic1.getId()
        );

        try {
            manager.createSubTask(conflictTask);
        } catch (Exception e) {
            System.out.println("Ошибка при добавлении пересекающейся задачи: " + e.getMessage());
        }
    }
}
