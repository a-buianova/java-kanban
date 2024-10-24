import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();
        Scanner scanner = new Scanner(System.in);

        String taskTitle;
        do {
            System.out.println("Введите название задачи:");
            taskTitle = scanner.nextLine();
            if (taskTitle == null || taskTitle.isEmpty()) {
                System.out.println("Название задачи не может быть пустым. Попробуйте снова.");
            }
        } while (taskTitle == null || taskTitle.isEmpty());

        String taskDescription;
        do {
            System.out.println("Введите описание задачи:");
            taskDescription = scanner.nextLine();
            if (taskDescription == null || taskDescription.isEmpty()) {
                System.out.println("Описание задачи не может быть пустым. Попробуйте снова.");
            }
        } while (taskDescription == null || taskDescription.isEmpty());

        Task task1 = new Task(0, taskTitle, taskDescription, TaskStatus.NEW);
        manager.createTask(task1);

        String epicTitle;
        String epicDescription;

        do {
            System.out.println("Введите название эпика:");
            epicTitle = scanner.nextLine();
            if (epicTitle == null || epicTitle.isEmpty()) {
                System.out.println("Название эпика не может быть пустым. Попробуйте снова.");
            }
        } while (epicTitle == null || epicTitle.isEmpty());

        do {
            System.out.println("Введите описание эпика:");
            epicDescription = scanner.nextLine();
            if (epicDescription == null || epicDescription.isEmpty()) {
                System.out.println("Описание эпика не может быть пустым. Попробуйте снова.");
            }
        } while (epicDescription == null || epicDescription.isEmpty());

        Epic epic1 = new Epic(0, epicTitle, epicDescription);
        manager.createEpic(epic1);

        // Ввод подзадач
        SubTask subtask1 = new SubTask(0, "Забронировать ресторан", "Найти и забронировать ресторан", TaskStatus.NEW, epic1.getId());
        SubTask subtask2 = new SubTask(0, "Отправить приглашения", "Разослать пригласительные", TaskStatus.NEW, epic1.getId());

        manager.createSubTask(subtask1);
        manager.createSubTask(subtask2);

        System.out.println(manager.getSubtasksForEpic(epic1.getId()));

        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);
        epic1.updateStatus();

        System.out.println("Эпик после выполнения всех задач: " + epic1);
    }
}