package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Специфичные тесты для InMemoryHistoryManager.
 * Универсальные тесты перенесены в TaskManagerTest.
 */
class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;

    private Task t1, t2, t3;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();

        t1 = new Task("Task 1", "Description", Duration.ofMinutes(30),
                LocalDateTime.of(2025, 5, 2, 10, 0));
        t1.setId(1);
        t2 = new Task("Task 2", "Description", Duration.ofMinutes(30),
                LocalDateTime.of(2025, 5, 2, 11, 0));
        t2.setId(2);
        t3 = new Task("Task 3", "Description", Duration.ofMinutes(30),
                LocalDateTime.of(2025, 5, 2, 12, 0));
        t3.setId(3);
    }

    @Test
    void testRemoveFirstTaskFromHistory() {
        historyManager.add(t1);
        historyManager.add(t2);

        historyManager.remove(t1.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу после удаления первой.");
        assertEquals(t2.getId(), history.get(0).getId());
    }

    @Test
    void testRemoveLastTaskFromHistory() {
        historyManager.add(t1);
        historyManager.add(t2);

        historyManager.remove(t2.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу после удаления последней.");
        assertEquals(t1.getId(), history.get(0).getId());
    }

    @Test
    void testRemoveMiddleTaskFromHistory() {
        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t3);

        historyManager.remove(t2.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать две задачи после удаления из середины.");
        assertEquals(t1.getId(), history.get(0).getId());
        assertEquals(t3.getId(), history.get(1).getId());
    }

    @Test
    void testEmptyHistoryInitially() {
        assertTrue(historyManager.getHistory().isEmpty(), "История должна быть пустой сразу после создания менеджера.");
    }

    @Test
    void testAvoidDuplicateHistory() {
        historyManager.add(t1);
        historyManager.add(t1);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История не должна содержать дубликатов задач.");
        assertEquals(t1.getId(), history.get(0).getId());
    }
}