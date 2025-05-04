package manager;

import org.junit.jupiter.api.Test;
import task.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {

    // Проверка удаления эпика и его подзадач из истории
    @Test
    void testEpicDeletionRemovesSubtasksFromHistory() {
        HistoryManager history = new InMemoryHistoryManager();

        Epic epic = new Epic(1, "Epic", "Epic description");
        SubTask sub1 = new SubTask(2, "Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        SubTask sub2 = new SubTask(3, "Subtask 2", "Description", TaskStatus.NEW, epic.getId());

        history.add(epic);
        history.add(sub1);
        history.add(sub2);

        history.remove(sub1.getId());
        history.remove(sub2.getId());
        history.remove(epic.getId());

        List<Task> result = history.getHistory();
        assertTrue(result.isEmpty(), "История должна быть пустой после удаления эпика и подзадач");
    }

    // Добавлено: Проверка порядка добавления задач
    @Test
    void testAddMaintainsCorrectOrder() {
        HistoryManager history = new InMemoryHistoryManager();

        Task t1 = new Task("Task 1", "Desc");
        Task t2 = new Task("Task 2", "Desc");
        Task t3 = new Task("Task 3", "Desc");

        t1.setId(1);
        t2.setId(2);
        t3.setId(3);

        history.add(t1);
        history.add(t2);
        history.add(t3);

        List<Task> result = history.getHistory();
        assertEquals(List.of(t1, t2, t3), result, "Задачи должны добавляться в историю в порядке добавления");
    }

    // Добавлено: Удаление из начала истории
    @Test
    void testRemoveFromBeginning() {
        HistoryManager history = new InMemoryHistoryManager();

        Task t1 = new Task("Task 1", "Desc");
        Task t2 = new Task("Task 2", "Desc");
        Task t3 = new Task("Task 3", "Desc");

        t1.setId(1);
        t2.setId(2);
        t3.setId(3);

        history.add(t1);
        history.add(t2);
        history.add(t3);
        history.remove(t1.getId());

        List<Task> result = history.getHistory();
        assertEquals(List.of(t2, t3), result, "Удаление из начала истории работает некорректно");
    }

    // Добавлено: Удаление из середины истории
    @Test
    void testRemoveFromMiddle() {
        HistoryManager history = new InMemoryHistoryManager();

        Task t1 = new Task("Task 1", "Desc");
        Task t2 = new Task("Task 2", "Desc");
        Task t3 = new Task("Task 3", "Desc");

        t1.setId(1);
        t2.setId(2);
        t3.setId(3);

        history.add(t1);
        history.add(t2);
        history.add(t3);
        history.remove(t2.getId());

        List<Task> result = history.getHistory();
        assertEquals(List.of(t1, t3), result, "Удаление из середины истории работает некорректно");
    }

    // Добавлено: Удаление из конца истории
    @Test
    void testRemoveFromEnd() {
        HistoryManager history = new InMemoryHistoryManager();

        Task t1 = new Task("Task 1", "Desc");
        Task t2 = new Task("Task 2", "Desc");
        Task t3 = new Task("Task 3", "Desc");

        t1.setId(1);
        t2.setId(2);
        t3.setId(3);

        history.add(t1);
        history.add(t2);
        history.add(t3);
        history.remove(t3.getId());

        List<Task> result = history.getHistory();
        assertEquals(List.of(t1, t2), result, "Удаление из конца истории работает некорректно");
    }

    // Добавлено: Повторное добавление одной и той же задачи
    @Test
    void testDuplicateNotAdded() {
        HistoryManager history = new InMemoryHistoryManager();
        Task task = new Task("Task", "Desc");
        task.setId(1);

        history.add(task);
        history.add(task);
        history.add(task);

        List<Task> result = history.getHistory();
        assertEquals(1, result.size(), "Повторное добавление задачи не должно создавать дубликатов");
        assertEquals(task, result.get(0));
    }

    // Добавлено: Работа с пустой историей
    @Test
    void testEmptyHistory() {
        HistoryManager history = new InMemoryHistoryManager();
        List<Task> result = history.getHistory();
        assertTrue(result.isEmpty(), "История должна быть пустой, если задачи не добавлялись");
    }

    // Добавлено: Повторное добавление перемещает задачу в конец
    @Test
    void testReAddMovesTaskToEnd() {
        HistoryManager history = new InMemoryHistoryManager();

        Task t1 = new Task("T1", "Desc");
        Task t2 = new Task("T2", "Desc");
        Task t3 = new Task("T3", "Desc");

        t1.setId(1);
        t2.setId(2);
        t3.setId(3);

        history.add(t1);
        history.add(t2);
        history.add(t3);
        history.add(t2); // повторное добавление

        List<Task> result = history.getHistory();
        assertEquals(List.of(t1, t3, t2), result, "Повторное добавление должно переместить задачу в конец");
    }

    // Добавлено: Добавление null не должно ломать историю
    @Test
    void testAddNullTask() {
        HistoryManager history = new InMemoryHistoryManager();
        Task task = new Task("Task", "Desc");
        task.setId(1);

        history.add(task);
        history.add(null);

        List<Task> result = history.getHistory();
        assertEquals(1, result.size(), "Добавление null не должно влиять на историю");
        assertEquals(task, result.get(0));
    }

    // Добавлено: Удаление несуществующего ID
    @Test
    void testRemoveNonexistentId() {
        HistoryManager history = new InMemoryHistoryManager();
        Task task = new Task("Task", "Desc");
        task.setId(1);

        history.add(task);
        history.remove(999);

        List<Task> result = history.getHistory();
        assertEquals(1, result.size(), "Удаление несуществующего ID не должно влиять на историю");
        assertEquals(task, result.get(0));
    }

    // Добавлено: Иммутабельность возвращаемого списка
    @Test
    void testHistoryImmutability() {
        HistoryManager history = new InMemoryHistoryManager();
        Task task = new Task("Task", "Desc");
        task.setId(1);

        history.add(task);

        List<Task> retrieved = history.getHistory();
        retrieved.clear(); // попытка очистки

        List<Task> result = history.getHistory();
        assertEquals(1, result.size(), "Внутреннее состояние истории не должно меняться при изменении копии");
    }

    // Добавлено: Смешанные операции (добавление, удаление, повторное добавление)
    @Test
    void testMixedOperations() {
        HistoryManager history = new InMemoryHistoryManager();
        Task t1 = new Task("T1", "Desc");
        Task t2 = new Task("T2", "Desc");
        Task t3 = new Task("T3", "Desc");
        Task t4 = new Task("T4", "Desc");

        t1.setId(1);
        t2.setId(2);
        t3.setId(3);
        t4.setId(4);

        history.add(t1);
        history.add(t2);
        history.add(t3);
        history.remove(t2.getId());
        history.add(t4);
        history.add(t1); // переместить в конец

        List<Task> result = history.getHistory();
        assertEquals(List.of(t3, t4, t1), result, "Смешанные операции должны корректно обновлять историю");
    }

    // Добавлено: Удаление всех задач
    @Test
    void testClearHistoryByRemovingAll() {
        HistoryManager history = new InMemoryHistoryManager();
        Task t1 = new Task("T1", "Desc");
        Task t2 = new Task("T2", "Desc");
        Task t3 = new Task("T3", "Desc");

        t1.setId(1);
        t2.setId(2);
        t3.setId(3);

        history.add(t1);
        history.add(t2);
        history.add(t3);

        history.remove(t1.getId());
        history.remove(t2.getId());
        history.remove(t3.getId());

        List<Task> result = history.getHistory();
        assertTrue(result.isEmpty(), "История должна быть пустой после удаления всех задач");
    }
}