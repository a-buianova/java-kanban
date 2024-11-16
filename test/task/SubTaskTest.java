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
        taskManager = new InMemoryTaskManager(); // Инициализация TaskManager
    }

    // 1. Проверка, что подзадача не может быть своим собственным эпиком
    @Test
    void testSubtaskCannotBeItsOwnEpic() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        // Создание подзадачи с ID эпика
        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());

        // Подзадача не может быть своим эпиком
        assertThrows(IllegalArgumentException.class, () -> taskManager.createSubTask(subtask));
    }

    // 2. Проверка, что можно создать подзадачу с правильным статусом
    @Test
    void testCreateSubtaskWithCorrectStatus() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);
        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());

        taskManager.createSubTask(subtask);
        assertNotNull(taskManager.getSubtask(subtask.getId())); // Подзадача должна быть добавлена в менеджер
        assertEquals(TaskStatus.NEW, taskManager.getSubtask(subtask.getId()).getStatus()); // Статус должен быть NEW
    }

    // 3. Проверка, что можно обновить статус подзадачи
    @Test
    void testUpdateSubtaskStatus() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(subtask);

        subtask.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubTask(subtask);

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getSubtask(subtask.getId()).getStatus()); // Статус подзадачи должен быть обновлен на IN_PROGRESS
    }

    // 4. Проверка, что подзадача имеет правильный статус после удаления
    @Test
    void testDeleteSubtask() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(subtask);

        taskManager.deleteSubtask(subtask.getId());
        assertNull(taskManager.getSubtask(subtask.getId())); // Подзадача должна быть удалена
    }

    // 5. Проверка, что не может быть создана подзадача с несуществующим эпиком
    @Test
    void testCreateSubtaskWithNonExistentEpic() {
        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, 99); // Не существует эпика с таким ID

        assertThrows(IllegalArgumentException.class, () -> taskManager.createSubTask(subtask)); // Должна быть ошибка при попытке создать подзадачу
    }

    // 6. Проверка, что при удалении эпика, все его подзадачи удаляются
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