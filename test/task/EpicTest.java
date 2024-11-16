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
        taskManager = new InMemoryTaskManager(); // Инициализация TaskManager
    }

    // 1. Проверка, что объект Epic нельзя добавить в самого себя в виде подзадачи
    @Test
    void testEpicCannotAddItselfAsSubtask() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        // Попытка создать подзадачу с ID эпика в качестве эпика подзадачи
        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(subtask); // Подзадача добавляется

        // Попытка добавить эпик как подзадачу
        assertThrows(IllegalArgumentException.class, () -> epic.addSubtask(subtask)); // Эпик не должен добавлять себя как подзадачу
    }

    // 2. Проверка добавления подзадач в эпик
    @Test
    void testAddSubtaskToEpic() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        // Создание подзадачи для эпика
        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(subtask); // Добавляем подзадачу в менеджер

        // Добавление подзадачи в эпик
        epic.addSubtask(subtask);

        assertNotNull(epic.getSubtasks()); // Убедимся, что подзадача добавлена в эпик
        assertTrue(epic.getSubtasks().contains(subtask)); // Проверяем, что подзадача действительно есть в списке подзадач эпика
    }

    // 3. Проверка, что подзадача не может быть добавлена дважды в один эпик
    @Test
    void testCannotAddSubtaskTwice() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        // Создание подзадачи для эпика
        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(subtask);

        // Добавление подзадачи в эпик
        epic.addSubtask(subtask);
        assertEquals(1, epic.getSubtasks().size()); // Подзадача должна быть только одна

        // Попытка добавить ту же подзадачу снова
        epic.addSubtask(subtask);
        assertEquals(1, epic.getSubtasks().size()); // Подзадача не должна добавиться второй раз
    }

    // 4. Проверка, что эпик без подзадач имеет статус NEW
    @Test
    void testEpicStatusWhenNoSubtasks() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        assertEquals(TaskStatus.NEW, epic.getStatus()); // Статус эпика должен быть NEW, так как у него нет подзадач
    }

    // 5. Проверка, что статус эпика обновляется при добавлении подзадач
    @Test
    void testEpicStatusWhenSubtaskAdded() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(subtask);

        epic.addSubtask(subtask);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus()); // Статус эпика должен измениться на IN_PROGRESS, когда у него есть подзадачи
    }

    // 6. Проверка, что статус эпика обновляется на DONE, если все подзадачи выполнены
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
