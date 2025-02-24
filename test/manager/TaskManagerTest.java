package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;
import task.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void testCreateAndGetEpicWithSubtasks() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask = new SubTask(2, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(subtask);

        List<SubTask> subtasks = taskManager.getSubtasksForEpic(epic.getId());
        assertEquals(1, subtasks.size(), "Эпик должен содержать одну подзадачу.");
        assertEquals(subtask.getId(), subtasks.get(0).getId(), "ID подзадачи должен совпадать.");
    }

    @Test
    void testAddDuplicateSubtaskToEpic() {
        Epic epic = new Epic(1, "Epic", "Description");
        taskManager.createEpic(epic);

        SubTask subtask = new SubTask(2, "Subtask", "Description", TaskStatus.NEW, epic.getId());
        epic.addSubtask(subtask.getId()); // ✅ Передаём ID вместо объекта

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            epic.addSubtask(subtask.getId()); // ✅ Передаём ID вместо объекта
        });

        assertEquals("Подзадача с ID 2 уже добавлена.", exception.getMessage());
    }

    @Test
    void testDeleteEpicWithSubtasks() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask = new SubTask(2, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(subtask);

        taskManager.deleteEpic(epic.getId());

        assertNull(taskManager.getEpic(epic.getId()), "Эпик должен быть удален.");
        assertTrue(taskManager.getSubtasksForEpic(epic.getId()).isEmpty(), "Все подзадачи эпика должны быть удалены.");
    }
}
