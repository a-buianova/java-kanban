import manager.InMemoryTaskManager;
import manager.TaskManager;
import task.Epic;
import task.SubTask;
import task.TaskStatus;

public class Main {

    public static void main(String[] args) {

        TaskManager manager = new InMemoryTaskManager();

        Epic epic1 = new Epic(1, "Организация праздника", "Организовать день рождения");
        Epic epic2 = new Epic(2, "Организация вечеринки", "Организовать вечеринку");

        manager.createEpic(epic1);
        manager.createEpic(epic2);

        SubTask epicOneTaskOne = new SubTask(101, "Забронировать ресторан", "Найти и забронировать ресторан", TaskStatus.NEW, epic1.getId());
        SubTask epicOneTaskTwo = new SubTask(102, "Отправить приглашения", "Разослать пригласительные", TaskStatus.NEW, epic1.getId());
        SubTask epicTwoTaskOne = new SubTask(103, "Приготовить торт", "Испечь торт", TaskStatus.NEW, epic2.getId());

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

        System.out.println("История просмотров задач: " + manager.getHistory());
    }
}
