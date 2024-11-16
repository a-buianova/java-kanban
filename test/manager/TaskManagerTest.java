package manager;

import org.junit.jupiter.api.*;
import task.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        this.taskManager = new InMemoryTaskManager();
    }

    @AfterEach
    void tearDown() {
        this.taskManager = null;
    }

    // Проверка создания задач
    @Test
    void testCreateTask() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        taskManager.createTask(task);
        assertNotNull(taskManager.getTask(task.getId()));
    }

    // Проверка создания эпиков
    @Test
    void testCreateEpic() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);
        assertNotNull(taskManager.getEpic(epic.getId()));
    }

    // Проверка создания подзадач
    @Test
    void testCreateSubTask() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);
        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        SubTask createdSubTask = taskManager.createSubTask(subtask);
        assertNotNull(taskManager.getSubtask(createdSubTask.getId()));
    }

    // Проверка обновления подзадачи
    @Test
    void testUpdateSubTask() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);
        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        SubTask createdSubTask = taskManager.createSubTask(subtask);

        createdSubTask.setStatus(TaskStatus.DONE);
        taskManager.updateSubTask(createdSubTask);

        assertEquals(TaskStatus.DONE, taskManager.getSubtask(createdSubTask.getId()).getStatus());
    }

    // Проверка удаления задачи
    @Test
    void testDeleteTask() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        taskManager.createTask(task);
        taskManager.deleteTask(task.getId());
        assertNull(taskManager.getTask(task.getId()));
    }

    // Проверка удаления эпика
    @Test
    void testDeleteEpic() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);
        taskManager.deleteEpic(epic.getId());
        assertNull(taskManager.getEpic(epic.getId()));
    }

    // Проверка удаления подзадачи
    @Test
    void testDeleteSubtask() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);
        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(subtask);
        taskManager.deleteSubtask(subtask.getId());
        assertNull(taskManager.getSubtask(subtask.getId()));
    }

    // Проверка сортировки задач по статусу
    @Test
    void testGetTasksSortedByStatus() {
        Task task1 = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        Task task2 = new Task(2, "Task 2", "Description", TaskStatus.DONE);
        taskManager.createTask(task1);
        taskManager.createTask(task2);

        List<Task> sortedTasks = taskManager.getTasksSortedByStatus();
        assertEquals(task1, sortedTasks.get(0));
        assertEquals(task2, sortedTasks.get(1));
    }

    // Проверка сортировки подзадач по статусу
    @Test
    void testGetSubtasksSortedByStatus() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask1 = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        SubTask subtask2 = new SubTask(2, "Subtask 2", "Description", TaskStatus.DONE, epic.getId());

        SubTask createSubTask1 = taskManager.createSubTask(subtask1);
        SubTask createSubTask2 = taskManager.createSubTask(subtask2);

        List<SubTask> sortedSubtasks = taskManager.getSubtasksSortedByStatus(epic.getId());
        assertEquals(createSubTask1, sortedSubtasks.get(0));
        assertEquals(createSubTask2, sortedSubtasks.get(1));
    }

    // Проверка истории задач
    @Test
    void testGetHistory() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        taskManager.createTask(task);
        taskManager.getTask(1);

        List<Task> history = taskManager.getHistory();
        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals(task.getId(), history.get(0).getId());
    }

    // Проверка, что задачи добавляются в историю
    @Test
    void testHistoryManagerAddsTask() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        taskManager.createTask(task);
        taskManager.getTask(1);

        List<Task> history = taskManager.getHistory();
        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals(task.getId(), history.get(0).getId());
    }

    // Проверка, что подзадачи добавляются в историю
    @Test
    void testHistoryManagerAddsSubtask() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);
        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        SubTask createdSubTask = taskManager.createSubTask(subtask);
        taskManager.getSubtask(createdSubTask.getId());

        List<Task> history = taskManager.getHistory();
        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals(createdSubTask.getId(), history.getFirst().getId());
    }
    //Проверка, что задачи с одинаковым id не могут быть добавлены в менеджер
    @Test
    void testTaskIdConflict() {
        Task task1 = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        Task task2 = new Task(1, "Task 2", "Description", TaskStatus.NEW);

        taskManager.createTask(task1);
        assertThrows(IllegalArgumentException.class, () -> taskManager.createTask(task2));
    }

    // Проверка на неизменность задачи после добавления в менеджер
    @Test
    void testTaskImmutability() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        taskManager.createTask(task);

        Task fetchedTask = taskManager.getTask(1);
        fetchedTask.setStatus(TaskStatus.DONE);

        Task unchangedTask = taskManager.getTask(1);
        assertNotEquals(fetchedTask.getStatus(), unchangedTask.getStatus());
    }

    // Проверка инициализации менеджеров
    @Test
    void testHistoryManagerInitialization() {
        assertNotNull(Managers.getDefaultHistory(), "HistoryManager должен быть инициализирован");
    }

    @Test
    void testTaskManagerInitialization() {
        assertNotNull(new InMemoryTaskManager(), "TaskManager должен быть инициализирован");
    }

}
