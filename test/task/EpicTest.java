package task;

import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    // Проверка добавления подзадач в эпик
    @Test
    void testAddSubtaskToEpic() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask = new SubTask(2, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(subtask);

        assertTrue(epic.getSubtaskIds().contains(subtask.getId()),
                "ID подзадачи должен присутствовать в списке эпика.");
    }

    // Проверка, что эпик без подзадач имеет статус NEW
    @Test
    void testEpicStatusWhenNoSubtasks() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        assertEquals(TaskStatus.NEW, epic.getStatus(),
                "Статус эпика должен быть NEW, если у него нет подзадач.");
    }

    // Проверка, что статус эпика обновляется при добавлении подзадачи со статусом IN_PROGRESS
    @Test
    void testEpicStatusChangedBasedOnSubtaskStatus() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask = new SubTask(2, "Subtask 1", "Description", TaskStatus.IN_PROGRESS, epic.getId());
        taskManager.createSubTask(subtask);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(),
                "Если хотя бы одна подзадача в IN_PROGRESS, эпик должен быть IN_PROGRESS.");
    }

    // Проверка, что статус эпика обновляется на DONE, если все подзадачи выполнены
    @Test
    void testEpicStatusWhenAllSubtasksDone() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask1 = new SubTask(2, "Subtask 1", "Description", TaskStatus.DONE, epic.getId());
        SubTask subtask2 = new SubTask(3, "Subtask 2", "Description", TaskStatus.DONE, epic.getId());

        taskManager.createSubTask(subtask1);
        taskManager.createSubTask(subtask2);

        assertEquals(TaskStatus.DONE, epic.getStatus(),
                "Если все подзадачи эпика выполнены, эпик должен быть DONE.");
    }
}
