package manager;

import org.junit.jupiter.api.Test;
import task.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {

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
}