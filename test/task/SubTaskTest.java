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
        // Перед созданием подзадачи проверяем, что должен существовать эпик
        assertThrows(IllegalArgumentException.class, () -> new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, 1));
    }

    // Проверка, что можно создать подзадачу с правильным статусом
    @Test
    void testCreateSubtaskWithCorrectStatus() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask = new SubTask(2, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        SubTask createdSubtask = taskManager.createSubTask(subtask);

        assertNotNull(taskManager.getSubtaskById(createdSubtask.getId()), "Подзадача должна быть добавлена в менеджер");
        assertEquals(TaskStatus.NEW, taskManager.getSubtaskById(createdSubtask.getId()).getStatus(), "Статус подзадачи должен быть NEW");
    }

    // Проверка, что можно обновить статус подзадачи
    @Test
    void testUpdateSubtaskStatus() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask = new SubTask(2, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        SubTask createdSubTask = taskManager.createSubTask(subtask);

        createdSubTask.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubtask(createdSubTask);

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getSubtaskById(createdSubTask.getId()).getStatus(),
                "Статус подзадачи должен быть обновлен на IN_PROGRESS");
    }

    // Проверка, что подзадача удаляется корректно
    @Test
    void testDeleteSubtask() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask = new SubTask(2, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(subtask);

        taskManager.deleteSubtask(subtask.getId());
        assertNull(taskManager.getSubtaskById(subtask.getId()), "Подзадача должна быть удалена");
    }

    // Проверка, что нельзя создать подзадачу с несуществующим эпиком
    @Test
    void testCreateSubtaskWithNonExistentEpic() {
        SubTask subtask = new SubTask(2, "Subtask 1", "Description", TaskStatus.NEW, 99); // Эпика с ID 99 не существует

        assertThrows(IllegalArgumentException.class, () -> taskManager.createSubTask(subtask),
                "Попытка создать подзадачу с несуществующим эпиком должна вызывать ошибку");
    }

    // Проверка, что при удалении эпика, все его подзадачи удаляются
    @Test
    void testDeleteEpicRemovesSubtasks() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask = new SubTask(2, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(subtask);

        taskManager.deleteEpic(epic.getId());
        assertNull(taskManager.getSubtaskById(subtask.getId()), "Подзадача должна быть удалена вместе с эпиком");
    }
}
