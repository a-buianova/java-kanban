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

    @Test
    void testAddTaskToHistory() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не должна быть null.");
        assertEquals(1, history.size(), "История должна содержать одну задачу.");
        assertEquals(task, history.get(0), "Задача должна соответствовать добавленной.");
    }

    @Test
    void testNoDuplicateTasksInHistory() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        historyManager.add(task);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать только одну задачу.");
        assertEquals(task, history.get(0), "Задача должна остаться первой.");
    }

    @Test
    void testRemoveTaskFromHistory() {
        Task task1 = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        Task task2 = new Task(2, "Task 2", "Description", TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу.");
        assertEquals(task2, history.get(0), "Должна остаться вторая задача.");
    }

    @Test
    void testRemoveNonExistentTaskDoesNothing() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        historyManager.add(task);

        historyManager.remove(999);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История не должна измениться.");
    }

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
        assertTrue(history.isEmpty(), "История должна быть пустой после удаления эпика.");
    }

    @Test
    void testEmptyHistory() {
        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не должна быть null.");
        assertTrue(history.isEmpty(), "История должна быть пустой.");
    }

    @Test
    void testRemoveFirstTaskFromHistory() {
        Task task1 = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        Task task2 = new Task(2, "Task 2", "Description", TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(task1.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу.");
        assertEquals(task2, history.get(0), "Оставшаяся задача должна быть task2.");
    }

    @Test
    void testRemoveLastTaskFromHistory() {
        Task task1 = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        Task task2 = new Task(2, "Task 2", "Description", TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(task2.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу.");
        assertEquals(task1, history.get(0), "Оставшаяся задача должна быть task1.");
    }

    @Test
    void testOrderOfTasksInHistory() {
        Task task1 = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        Task task2 = new Task(2, "Task 2", "Description", TaskStatus.NEW);
        Task task3 = new Task(3, "Task 3", "Description", TaskStatus.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size(), "История должна содержать три задачи.");
        assertEquals(task1, history.get(0), "Первая задача должна быть task1.");
        assertEquals(task2, history.get(1), "Вторая задача должна быть task2.");
        assertEquals(task3, history.get(2), "Третья задача должна быть task3.");
    }

    @Test
    void testSingleTaskInHistory() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу.");
        assertEquals(task, history.get(0), "Задача должна соответствовать добавленной.");
    }
}
