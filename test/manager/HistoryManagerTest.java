package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;
import task.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {

    private HistoryManager historyManager;
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        taskManager = new InMemoryTaskManager();
    }

    // Проверка: задача успешно добавляется в историю
    @Test
    void testAddTaskToHistory() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не должна быть null после добавления задачи.");
        assertEquals(1, history.size(), "История должна содержать одну задачу.");
        assertEquals(task.getId(), history.get(0).getId(), "ID задачи в истории должен совпадать с добавленной задачей.");
    }

    // Проверка: дублирующиеся задачи не добавляются в историю
    @Test
    void testNoDuplicateTasksInHistory() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        historyManager.add(task);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать только одну задачу, несмотря на повторное добавление.");
    }

    // Проверка: задача успешно удаляется из истории
    @Test
    void testRemoveTaskFromHistory() {
        Task task1 = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        Task task2 = new Task(2, "Task 2", "Description", TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(1); // Удаляем первую задачу

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу после удаления.");
        assertEquals(task2.getId(), history.get(0).getId(), "ID оставшейся задачи должен совпадать с второй задачей.");
    }

    // Проверка: удаление несуществующей задачи не влияет на историю
    @Test
    void testRemoveNonExistentTaskDoesNothing() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        historyManager.add(task);

        historyManager.remove(999); // Пытаемся удалить задачу с несуществующим ID

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История не должна измениться при удалении несуществующей задачи.");
    }

    // Проверка: эпик и все его подзадачи удаляются из истории при удалении эпика
    @Test
    void testEpicDeletionRemovesSubtasksFromHistory() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subTask1 = new SubTask(2, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        SubTask subTask2 = new SubTask(3, "Subtask 2", "Description", TaskStatus.NEW, epic.getId());

        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);

        taskManager.getEpic(epic.getId());
        taskManager.getSubtask(subTask1.getId());
        taskManager.getSubtask(subTask2.getId());

        taskManager.deleteEpic(epic.getId());

        List<Task> history = taskManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой после удаления эпика и всех его подзадач.");
    }

    // Проверка: история возвращает пустой список, если задачи не добавлены
    @Test
    void testEmptyHistory() {
        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не должна быть null.");
        assertTrue(history.isEmpty(), "История должна быть пустой, если задачи не добавлены.");
    }
}