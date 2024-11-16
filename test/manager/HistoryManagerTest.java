package manager;

import task.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class HistoryManagerTest {

    private HistoryManager historyManager;
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        this.historyManager = Managers.getDefaultHistory();
        this.taskManager = new InMemoryTaskManager();
    }

    @AfterEach
    void tearDown() {
        this.historyManager = null;
        this.taskManager = null;
    }

    // Проверка, что задача добавляется в историю
    @Test
    void testAddTaskToHistory() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        taskManager.createTask(task);
        taskManager.getTask(1);

        List<Task> history = taskManager.getHistory();
        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals(task.getId(), history.get(0).getId());
    }

    // Проверка, что задача в истории не удаляется при новом добавлении
    @Test
    void testHistoryKeepsTaskAfterAccess() {
        Task task1 = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        Task task2 = new Task(2, "Task 2", "Description", TaskStatus.NEW);

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        taskManager.getTask(1);
        taskManager.getTask(2);

        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size());
        assertTrue(history.contains(task1));
        assertTrue(history.contains(task2));
    }

    // Проверка на то, что задачи в истории отсортированы по порядку
    @Test
    void testHistoryTaskOrder() {
        Task task1 = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        Task task2 = new Task(2, "Task 2", "Description", TaskStatus.NEW);

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        taskManager.getTask(1);
        taskManager.getTask(2);

        List<Task> history = taskManager.getHistory();
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    // Проверка на хранение не более 10 задач в истории
    @Test
    void testHistorySizeLimit() {
        for (int i = 1; i <= 15; i++) {
            Task task = new Task(i, "Task " + i, "Description", TaskStatus.NEW);
            taskManager.createTask(task);
            taskManager.getTask(i);
        }

        List<Task> history = taskManager.getHistory();
        assertEquals(10, history.size());
        assertEquals(6, history.get(0).getId());
        assertEquals(15, history.get(9).getId());
    }

    // Проверка, что изменения задачи сохраняются в истории
    @Test
    void testTaskUpdateHistory() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);
        SubTask subtask = new SubTask(1, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        taskManager.createSubTask(subtask);
        SubTask subTaskCloned = taskManager.getSubtask(2);

        subTaskCloned.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubTask(subTaskCloned);

        taskManager.getSubtask(2);

        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(TaskStatus.NEW, history.get(0).getStatus());
        assertEquals(TaskStatus.IN_PROGRESS, history.get(1).getStatus());
    }

    // Проверка, что задачи не добавляются в историю, если они не были получены
    @Test
    void testNoTaskInHistoryIfNotAccessed() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        taskManager.createTask(task);

        List<Task> history = taskManager.getHistory();
        assertTrue(history.isEmpty());
    }
}