package task;

import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubTaskTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    // Проверка, что подзадача не может быть своим собственным эпиком
    @Test
    void testSubtaskCannotBeItsOwnEpic() {

        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, 1);

        assertThrows(IllegalArgumentException.class, () -> taskManager.createSubTask(subtask));
    }

    //Проверка, что можно создать подзадачу с правильным статусом
    @Test
    void testCreateSubtaskWithCorrectStatus() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);
        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());

        SubTask createdSubtask = taskManager.createSubTask(subtask);
        assertNotNull(taskManager.getSubtask(createdSubtask.getId())); // Подзадача должна быть добавлена в менеджер
        assertEquals(TaskStatus.NEW, taskManager.getSubtask(createdSubtask.getId()).getStatus()); // Статус должен быть NEW
    }

    //Проверка, что можно обновить статус подзадачи
    @Test
    void testUpdateSubtaskStatus() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        SubTask createdSubTask = taskManager.createSubTask(subtask);

        createdSubTask.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubTask(createdSubTask);

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getSubtask(createdSubTask.getId()).getStatus()); // Статус подзадачи должен быть обновлен на IN_PROGRESS
    }

    //Проверка, что подзадача имеет правильный статус после удаления
    @Test
    void testDeleteSubtask() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(subtask);

        taskManager.deleteSubtask(subtask.getId());
        assertNull(taskManager.getSubtask(subtask.getId())); // Подзадача должна быть удалена
    }

    //Проверка, что не может быть создана подзадача с несуществующим эпиком
    @Test
    void testCreateSubtaskWithNonExistentEpic() {
        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, 99); // Не существует эпика с таким ID

        assertThrows(IllegalArgumentException.class, () -> taskManager.createSubTask(subtask)); // Должна быть ошибка при попытке создать подзадачу
    }

    //Проверка, что при удалении эпика, все его подзадачи удаляются
    @Test
    void testDeleteEpicRemovesSubtasks() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(subtask);

        taskManager.deleteEpic(epic.getId());
        assertNull(taskManager.getSubtask(subtask.getId())); // Подзадача должна быть удалена вместе с эпиком
    }
}