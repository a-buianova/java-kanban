package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;
import task.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Универсальные тесты для всех реализаций TaskManager.
 */
public abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    protected Task task1;
    protected Task task2;
    protected Epic epic;
    protected SubTask sub1;
    protected SubTask sub2;

    protected abstract T createManager();

    @BeforeEach
    void setUp() {
        manager = createManager();
        assertNotNull(manager, "TaskManager должен быть инициализирован в createManager()");

        task1 = manager.createTask(new Task("Task 1", "Description",
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 5, 2, 10, 0)));
        task2 = manager.createTask(new Task("Task 2", "Description",
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 5, 2, 11, 0)));

        epic = manager.createEpic(new Epic("Epic 1", "Epic desc"));
        sub1 = manager.createSubTask(new SubTask("Sub 1", "desc", Duration.ofMinutes(30),
                LocalDateTime.of(2025, 5, 2, 12, 0), epic.getId()));
        sub2 = manager.createSubTask(new SubTask("Sub 2", "desc", Duration.ofMinutes(45),
                LocalDateTime.of(2025, 5, 2, 13, 0), epic.getId()));
    }

    @Test
    void shouldAddAndRetrieveTask() {
        Task taskNew = new Task("Test Task", "Description",
                Duration.ofMinutes(45),
                LocalDateTime.of(2025, 5, 2, 14, 0)); // вне диапазона
        Task created = manager.createTask(taskNew);
        Task fetched = manager.getTask(created.getId()).orElseThrow();
        assertEquals(created, fetched);
    }

    @Test
    void shouldReturnTasksInStartTimeOrder() {
        List<Task> sorted = manager.getPrioritizedTasks();
        for (int i = 0; i < sorted.size() - 1; i++) {
            LocalDateTime currentStart = sorted.get(i).getStartTime();
            LocalDateTime nextStart = sorted.get(i + 1).getStartTime();
            assertNotNull(currentStart, "У задачи должно быть время начала");
            assertNotNull(nextStart, "У задачи должно быть время начала");
            assertFalse(currentStart.isAfter(nextStart), "Список должен быть отсортирован по времени начала задач");
        }
    }

    @Test
    void shouldThrowExceptionOnOverlappingTasks() {
        T isolatedManager = createManager();  // <== чистый менеджер без задач

        Task baseTask = new Task("Base", "Desc", Duration.ofMinutes(60),
                LocalDateTime.of(2025, 5, 2, 10, 0)); // 10:00 – 11:00
        isolatedManager.createTask(baseTask);

        Task overlapping = new Task("Overlap", "Desc", Duration.ofMinutes(30),
                LocalDateTime.of(2025, 5, 2, 10, 30)); // пересекается с baseTask

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> isolatedManager.createTask(overlapping));

        assertTrue(ex.getMessage().contains("пересекается"));
    }

    @Test
    void shouldReturnEmptyPrioritizedListWhenNoTasks() {
        T emptyManager = createManager();
        assertTrue(emptyManager.getPrioritizedTasks().isEmpty());
    }

    @Test
    void taskWithoutStartTimeShouldBeInPrioritizedList() {
        Task task = new Task("No time", "No time", null, null);
        task.setId(1);
        task.setStatus(TaskStatus.NEW);
        manager.createTask(task);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertTrue(prioritized.contains(task), "Задача без времени должна быть в приоритезированном списке.");
    }

    @Test
    void epicStatusShouldBeNewIfNoSubtasks() {
        Epic newEpic = manager.createEpic(new Epic("New Epic", "Empty"));
        assertEquals(TaskStatus.NEW, newEpic.getStatus());
    }

    @Test
    void epicStatusShouldBeDoneIfAllSubtasksDone() {
        sub1.setStatus(TaskStatus.DONE);
        sub2.setStatus(TaskStatus.DONE);
        manager.updateSubTask(sub1);
        manager.updateSubTask(sub2);
        assertEquals(TaskStatus.DONE, manager.getEpic(epic.getId()).orElseThrow().getStatus());
    }

    @Test
    void epicStatusShouldBeInProgressIfMixedSubtasks() {
        sub1.setStatus(TaskStatus.NEW);
        sub2.setStatus(TaskStatus.DONE);
        manager.updateSubTask(sub1);
        manager.updateSubTask(sub2);
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpic(epic.getId()).orElseThrow().getStatus());
    }

    @Test
    void shouldCalculateEpicTimeFieldsCorrectly() {
        Epic updated = manager.getEpic(epic.getId()).orElseThrow();
        assertEquals(Duration.ofMinutes(105), updated.getDuration());
        assertEquals(LocalDateTime.of(2025, 5, 2, 12, 0), updated.getStartTime());
        assertEquals(LocalDateTime.of(2025, 5, 2, 13, 45), updated.getEndTime());
    }

    @Test
    void epicWithoutSubtasksShouldHaveNoTime() {
        Epic newEpic = manager.createEpic(new Epic("Empty Epic", "No subs"));
        Epic fetched = manager.getEpic(newEpic.getId()).orElseThrow();
        assertEquals(Duration.ZERO, fetched.getDuration());
        assertNull(fetched.getStartTime());
        assertNull(fetched.getEndTime());
    }

    @Test
    void shouldAddTaskToHistory() {
        Task task = manager.createTask(new Task("Task 1", "Description",
                Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 2, 14, 0)));

        manager.getTask(task.getId());

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task.getId(), history.get(0).getId());
    }

    @Test
    void shouldNotAddDuplicateHistoryEntries() {
        Task task = manager.createTask(new Task("Task 1", "Description",
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 5, 2, 15, 0))); // время вне пересечений

        manager.getTask(task.getId());
        manager.getTask(task.getId());

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size(), "История должна содержать только одну запись, без дубликатов.");
    }

    @Test
    void shouldRemoveTaskFromHistory() {
        Task task1 = manager.createTask(new Task("Task 1", "Description",
                Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 2, 14, 0)));
        Task task2 = manager.createTask(new Task("Task 2", "Description",
                Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 2, 15, 0)));

        manager.getTask(task1.getId());
        manager.getTask(task2.getId());

        manager.deleteTask(task1.getId());

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task2.getId(), history.get(0).getId());
    }

    @Test
    void shouldNotCrashOnRemovingNonExistentTaskFromHistory() {
        manager.deleteTask(999);
        List<Task> history = manager.getHistory();
        assertNotNull(history);
    }

    @Test
    void shouldReturnEmptyHistoryInitially() {
        T emptyManager = createManager();
        assertTrue(emptyManager.getHistory().isEmpty());
    }

    @Test
    void shouldPreserveHistoryOrder() {
        Task task1 = manager.createTask(new Task("Task 1", "Description 1",
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 5, 2, 15, 0)));  // безопасное время

        Task task2 = manager.createTask(new Task("Task 2", "Description 2",
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 5, 2, 16, 0)));  // безопасное время

        manager.getTask(task1.getId());
        manager.getTask(task2.getId());

        List<Task> history = manager.getHistory();
        assertEquals(task1.getId(), history.get(0).getId());
        assertEquals(task2.getId(), history.get(1).getId());
    }

    @Test
    void shouldClearSubtasksFromHistoryWhenEpicDeleted() {
        manager.getEpic(epic.getId());
        manager.getSubtask(sub1.getId());
        manager.getSubtask(sub2.getId());
        manager.deleteEpic(epic.getId());
        assertTrue(manager.getHistory().isEmpty());
    }
}