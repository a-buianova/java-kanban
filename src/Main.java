import manager.InMemoryTaskManager;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import task.Epic;
import task.SubTask;
import task.TaskStatus;

public class Main {

    public static void main(String[] args) {

        TaskManager manager = new InMemoryTaskManager();

        Epic epic1 = manager.createEpic(new Epic(0, "Организация праздника", "Организовать день рождения"));
        Epic epic2 = manager.createEpic(new Epic(0, "Организация вечеринки", "Организовать вечеринку"));

        SubTask epicOneTaskOne = manager.createSubTask(new SubTask(0, "Забронировать ресторан", "Найти и забронировать ресторан", TaskStatus.NEW, epic1.getId()));
        SubTask epicOneTaskTwo = manager.createSubTask(new SubTask(0, "Отправить приглашения", "Разослать пригласительные", TaskStatus.NEW, epic1.getId()));
        SubTask epicTwoTaskOne = manager.createSubTask(new SubTask(0, "Приготовить торт", "Испечь торт", TaskStatus.NEW, epic2.getId()));

        System.out.println("Список эпиков:");
        System.out.println(manager.getAllEpics());

        System.out.println("\nУстанавливаем подзадаче DONE:");
        epicTwoTaskOne.setStatus(TaskStatus.DONE);
        manager.updateSubTask(epicTwoTaskOne);
        System.out.println("Статус эпика 2 после изменения подзадачи: " + manager.getEpic(epic2.getId()).getStatus());

        System.out.println("\nУдаляем подзадачу:");
        manager.deleteSubtask(epicOneTaskOne.getId());
        System.out.println("Статус эпика 1 после удаления подзадачи: " + manager.getEpic(epic1.getId()).getStatus());

        System.out.println("\nИтоговый список задач:");
        System.out.println(manager.getAllTasks());

        System.out.println("\nИтоговый список эпиков:");
        System.out.println(manager.getAllEpics());

        System.out.println("\nИстория просмотров задач:");
        System.out.println(manager.getHistory());
    }
}
