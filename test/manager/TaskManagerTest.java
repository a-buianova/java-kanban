package manager;

import org.junit.jupiter.api.*;
import task.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        this.taskManager = new InMemoryTaskManager(); // Инициализация TaskManager
    }

    @AfterEach
    void tearDown() {
        this.taskManager = null; // Очищаем TaskManager после каждого теста
    }

    // 1. Проверка создания задач
    @Test
    void testCreateTask() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        taskManager.createTask(task);
        assertNotNull(taskManager.getTask(task.getId())); // Задача должна быть добавлена в менеджер
    }

    // 2. Проверка создания эпиков
    @Test
    void testCreateEpic() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);
        assertNotNull(taskManager.getEpic(epic.getId())); // Эпик должен быть добавлен в менеджер
    }

    // 3. Проверка создания подзадач
    @Test
    void testCreateSubTask() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);
        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(subtask);
        assertNotNull(taskManager.getSubtask(subtask.getId())); // Подзадача должна быть добавлена в менеджер
    }

    // 4. Проверка обновления подзадачи
    @Test
    void testUpdateSubTask() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);
        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(subtask);

        subtask.setStatus(TaskStatus.DONE);
        taskManager.updateSubTask(subtask);

        assertEquals(TaskStatus.DONE, taskManager.getSubtask(subtask.getId()).getStatus()); // Статус подзадачи должен обновиться
    }

    // 5. Проверка удаления задачи
    @Test
    void testDeleteTask() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        taskManager.createTask(task);
        taskManager.deleteTask(task.getId());
        assertNull(taskManager.getTask(task.getId())); // Задача должна быть удалена
    }

    // 6. Проверка удаления эпика
    @Test
    void testDeleteEpic() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);
        taskManager.deleteEpic(epic.getId());
        assertNull(taskManager.getEpic(epic.getId())); // Эпик должен быть удален
    }

    // 7. Проверка удаления подзадачи
    @Test
    void testDeleteSubtask() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);
        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(subtask);
        taskManager.deleteSubtask(subtask.getId());
        assertNull(taskManager.getSubtask(subtask.getId())); // Подзадача должна быть удалена
    }

    // 8. Проверка сортировки задач по статусу
    @Test
    void testGetTasksSortedByStatus() {
        Task task1 = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        Task task2 = new Task(2, "Task 2", "Description", TaskStatus.DONE);
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        List<Task> sortedTasks = taskManager.getTasksSortedByStatus();
        assertEquals(task1, sortedTasks.get(0)); // Задача с новым статусом должна быть первой
        assertEquals(task2, sortedTasks.get(1)); // Задача с завершенным статусом должна быть второй
    }

    // 9. Проверка сортировки подзадач по статусу
    @Test
    void testGetSubtasksSortedByStatus() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask1 = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        SubTask subtask2 = new SubTask(2, "Subtask 2", "Description", TaskStatus.DONE, epic.getId());

        taskManager.createSubTask(subtask1);
        taskManager.createSubTask(subtask2);

        List<SubTask> sortedSubtasks = taskManager.getSubtasksSortedByStatus(epic.getId());
        assertEquals(subtask1, sortedSubtasks.get(0)); // Подзадача с новым статусом должна быть первой
        assertEquals(subtask2, sortedSubtasks.get(1)); // Подзадача с завершенным статусом должна быть второй
    }

    // 10. Проверка истории задач
    @Test
    void testGetHistory() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        taskManager.createTask(task);
        taskManager.getTask(1);

        List<Task> history = taskManager.getHistory();
        assertNotNull(history);
        assertEquals(1, history.size()); // Должна быть добавлена одна задача в историю
        assertEquals(task.getId(), history.get(0).getId());
    }

    // 11. Проверка, что задачи добавляются в историю
    @Test
    void testHistoryManagerAddsTask() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        taskManager.createTask(task);
        taskManager.getTask(1);

        List<Task> history = taskManager.getHistory();
        assertNotNull(history);
        assertEquals(1, history.size());  // Должна быть добавлена одна задача в историю
        assertEquals(task.getId(), history.get(0).getId());
    }

    // 12. Проверка, что подзадачи добавляются в историю
    @Test
    void testHistoryManagerAddsSubtask() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);
        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(subtask);
        taskManager.getSubtask(subtask.getId());

        List<Task> history = taskManager.getHistory();
        assertNotNull(history);
        assertEquals(1, history.size());  // Должна быть добавлена одна подзадача в историю
        assertEquals(subtask.getId(), history.get(0).getId());
    }
    //13. Проверка, что задачи с одинаковым id не могут быть добавлены в менеджер
    @Test
    void testTaskIdConflict() {
        Task task1 = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        Task task2 = new Task(1, "Task 2", "Description", TaskStatus.NEW);

        taskManager.createTask(task1);
        assertThrows(IllegalArgumentException.class, () -> taskManager.createTask(task2));
    }

    // 14. Проверка на неизменность задачи после добавления в менеджер
    @Test
    void testTaskImmutability() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        taskManager.createTask(task);

        Task fetchedTask = taskManager.getTask(1);
        fetchedTask.setStatus(TaskStatus.DONE); // Попытка изменить статус после добавления

        Task unchangedTask = taskManager.getTask(1);
        assertNotEquals(fetchedTask.getStatus(), unchangedTask.getStatus()); // Проверяем, что статус не был изменен
    }

    // 15. Проверка инициализации менеджеров
    @Test
    void testHistoryManagerInitialization() {
        assertNotNull(Managers.getDefaultHistory(), "HistoryManager должен быть инициализирован");
    }

    @Test
    void testTaskManagerInitialization() {
        assertNotNull(new InMemoryTaskManager(), "TaskManager должен быть инициализирован");
    }

}
