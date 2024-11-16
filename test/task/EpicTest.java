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

    // Проверка, что объект Epic нельзя добавить в самого себя в виде подзадачи
    @Test
    void testEpicCannotAddItselfAsSubtask() {
        // Тест на такой кейс написать не возможно, т.к. метод taskManager.createSubTask() принимает только
        // объекты типа SubTask, таким образом тест просто не скомпилируется.
    }

    // Проверка добавления подзадач в эпик
    @Test
    void testAddSubtaskToEpic() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(subtask);

        epic.addSubtask(subtask);

        assertNotNull(epic.getSubtasks());
        assertTrue(epic.getSubtasks().contains(subtask));
    }

    // Проверка, что эпик без подзадач имеет статус NEW
    @Test
    void testEpicStatusWhenNoSubtasks() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        assertEquals(TaskStatus.NEW, epic.getStatus()); // Статус эпика должен быть NEW, так как у него нет подзадач
    }

    // Проверка, что статус эпика обновляется при добавлении подзадач
    @Test
    void testEpicStatusChangedBasedOnSubtaskStatus() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);
        assertEquals(TaskStatus.NEW, epic.getStatus());

        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.IN_PROGRESS, epic.getId());
        taskManager.createSubTask(subtask);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    // Проверка, что статус эпика обновляется на DONE, если все подзадачи выполнены
    @Test
    void testEpicStatusWhenAllSubtasksDone() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask1 = new SubTask(1, "Subtask 1", "Description", TaskStatus.DONE, epic.getId());
        SubTask subtask2 = new SubTask(2, "Subtask 2", "Description", TaskStatus.DONE, epic.getId());

        taskManager.createSubTask(subtask1);
        taskManager.createSubTask(subtask2);

        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);

        assertEquals(TaskStatus.DONE, epic.getStatus()); // Статус эпика должен быть DONE, так как все подзадачи выполнены
    }
}
