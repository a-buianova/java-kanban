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

        SubTask subTask1_1 = new SubTask(101, "Забронировать ресторан", "Найти и забронировать ресторан", TaskStatus.NEW, epic1.getId());
        SubTask subTask1_2 = new SubTask(102, "Отправить приглашения", "Разослать пригласительные", TaskStatus.NEW, epic1.getId());
        SubTask subTask2_1 = new SubTask(103, "Приготовить торт", "Испечь торт", TaskStatus.NEW, epic2.getId());

        manager.createSubTask(subTask1_1);
        manager.createSubTask(subTask1_2);
        manager.createSubTask(subTask2_1);

        System.out.println("Список эпиков: " + manager.getAllEpics());

        subTask2_1.setStatus(TaskStatus.DONE);
        manager.updateSubTask(subTask2_1);
        System.out.println("Статус эпика 2 после изменения подзадачи: " + epic2.getStatus());

        manager.deleteSubtask(subTask1_1.getId());
        System.out.println("Статус эпика 1 после удаления подзадачи: " + epic1.getStatus());

        System.out.println("Список задач после удаления подзадачи: " + manager.getAllTasks());
        System.out.println("Список эпиков после удаления подзадачи: " + manager.getAllEpics());

        System.out.println("История просмотров задач: " + manager.getHistory());
    }
}
