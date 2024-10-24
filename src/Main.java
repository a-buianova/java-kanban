import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();
        Epic epic1 = new Epic(1, "", "");
        Epic epic2 = new Epic(2, "", "");
        SubTask subTask1_1 = new SubTask(1, "", "", TaskStatus.NEW, epic1.getId());
        SubTask subTask1_2 = new SubTask(2, "", "", TaskStatus.NEW, epic1.getId());
        SubTask subTask2_1 = new SubTask(3, "", "", TaskStatus.NEW, epic2.getId());
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        manager.createSubTask(subTask1_1);
        manager.createSubTask(subTask1_2);
        manager.createSubTask(subTask2_1);
        System.out.println(manager.getAllEpics());
        subTask2_1.setStatus(TaskStatus.DONE);
        manager.updateSubTask(subTask2_1);
        System.out.println(epic2.getStatus());

    }
}