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
        this.historyManager = Managers.getDefaultHistory(); // Получаем экземпляр HistoryManager через утилиту
        this.taskManager = new InMemoryTaskManager(); // Инициализация TaskManager
    }

    @AfterEach
    void tearDown() {
        this.historyManager = null; // Очищаем менеджер истории
        this.taskManager = null; // Очищаем TaskManager
    }

    // 1. Проверка, что задача добавляется в историю
    @Test
    void testAddTaskToHistory() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        taskManager.createTask(task);
        taskManager.getTask(1); // Задача должна попасть в историю

        List<Task> history = taskManager.getHistory();
        assertNotNull(history);
        assertEquals(1, history.size()); // Должна быть одна задача в истории
        assertEquals(task.getId(), history.get(0).getId()); // Задача в истории должна быть такой же
    }

    // 2. Проверка, что задача в истории не удаляется при новом добавлении
    @Test
    void testHistoryKeepsTaskAfterAccess() {
        Task task1 = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        Task task2 = new Task(2, "Task 2", "Description", TaskStatus.NEW);

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        taskManager.getTask(1); // Задача 1 должна попасть в историю
        taskManager.getTask(2); // Задача 2 должна попасть в историю

        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size()); // В истории должно быть две задачи
        assertTrue(history.contains(task1)); // Задача 1 должна быть в истории
        assertTrue(history.contains(task2)); // Задача 2 должна быть в истории
    }

    // 3. Проверка на то, что задачи в истории отсортированы по порядку
    @Test
    void testHistoryTaskOrder() {
        Task task1 = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        Task task2 = new Task(2, "Task 2", "Description", TaskStatus.NEW);

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        taskManager.getTask(1);
        taskManager.getTask(2);

        List<Task> history = taskManager.getHistory();
        assertEquals(task1, history.get(0)); // Задача 1 должна быть первой в истории
        assertEquals(task2, history.get(1)); // Задача 2 должна быть второй в истории
    }

    // 4. Проверка на хранение не более 10 задач в истории
    @Test
    void testHistorySizeLimit() {
        for (int i = 1; i <= 15; i++) {
            Task task = new Task(i, "Task " + i, "Description", TaskStatus.NEW);
            taskManager.createTask(task);
            taskManager.getTask(i); // Все задачи должны попасть в историю
        }

        List<Task> history = taskManager.getHistory();
        assertEquals(10, history.size()); // История должна содержать не более 10 задач
        assertEquals(6, history.get(0).getId()); // Задача с id 6 должна быть первой в истории
        assertEquals(15, history.get(9).getId()); // Задача с id 15 должна быть последней в истории
    }

    // 5. Проверка, что изменения задачи сохраняются в истории
    @Test
    void testTaskUpdateHistory() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        taskManager.createTask(task);
        taskManager.getTask(1); // Задача 1 добавлена в историю

        task.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubTask((SubTask) task);  // Теперь мы обновляем задачу, а не приводим её к SubTask

        taskManager.getTask(1); // Повторный доступ к задаче

        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size()); // В истории две версии задачи
        assertEquals(TaskStatus.NEW, history.get(0).getStatus()); // Первая версия задачи имеет статус NEW
        assertEquals(TaskStatus.IN_PROGRESS, history.get(1).getStatus()); // Вторая версия задачи имеет статус IN_PROGRESS
    }

    // 6. Проверка, что задачи не добавляются в историю, если они не были получены
    @Test
    void testNoTaskInHistoryIfNotAccessed() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        taskManager.createTask(task);

        // Задача не была получена, поэтому она не должна быть в истории
        List<Task> history = taskManager.getHistory();
        assertTrue(history.isEmpty());
    }
}