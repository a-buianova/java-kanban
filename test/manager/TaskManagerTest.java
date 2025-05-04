package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;
import task.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TaskManagerTest {
    private TaskManager manager;
    private Task task;
    private Epic epic;
    private SubTask subtask;

    @BeforeEach
    void setup() {
        manager = new InMemoryTaskManager();

        task = new Task("Task1", "Task Description");
        epic = new Epic("Epic1", "Epic Description");
        manager.createTask(task);
        manager.createEpic(epic);

        subtask = new SubTask("Subtask1", "Subtask Description", TaskStatus.NEW, epic.getId());
        manager.createSubTask(subtask);
    }

    @Test
    void shouldCreateAndRetrieveTask() {
        Task retrieved = manager.getTaskById(task.getId());
        assertNotNull(retrieved);
        assertEquals("Task1", retrieved.getTitle());
    }

    @Test
    void shouldUpdateTask() {
        task.setTitle("Updated Task");
        task.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(task);

        Task updated = manager.getTaskById(task.getId());
        assertEquals("Updated Task", updated.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, updated.getStatus());
    }

    @Test
    void shouldDeleteTask() {
        manager.deleteTask(task.getId());
        assertNull(manager.getTaskById(task.getId()));
    }

    @Test
    void shouldDeleteAllTasks() {
        manager.deleteAllTasks();
        assertTrue(manager.getAllTasks().isEmpty());
    }

    @Test
    void shouldCreateAndRetrieveEpic() {
        Epic retrieved = manager.getEpicById(epic.getId());
        assertNotNull(retrieved);
        assertEquals("Epic1", retrieved.getTitle());
    }

    @Test
    void shouldUpdateEpic() {
        epic.setTitle("Updated Epic");
        manager.updateEpic(epic);
        Epic updated = manager.getEpicById(epic.getId());
        assertEquals("Updated Epic", updated.getTitle());
    }

    @Test
    void shouldDeleteEpicAndSubtasks() {
        manager.deleteEpic(epic.getId());
        assertNull(manager.getEpicById(epic.getId()));
        assertTrue(manager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldDeleteAllEpics() {
        manager.deleteAllEpics();
        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldCreateAndRetrieveSubtask() {
        SubTask retrieved = manager.getSubtaskById(subtask.getId());
        assertNotNull(retrieved);
        assertEquals("Subtask1", retrieved.getTitle());
    }

    @Test
    void shouldUpdateSubtask() {
        subtask.setTitle("Updated Subtask");
        subtask.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask);

        SubTask updated = manager.getSubtaskById(subtask.getId());
        assertEquals("Updated Subtask", updated.getTitle());
        assertEquals(TaskStatus.DONE, updated.getStatus());
    }

    @Test
    void shouldDeleteSubtask() {
        manager.deleteSubtask(subtask.getId());
        assertNull(manager.getSubtaskById(subtask.getId()));
    }

    @Test
    void shouldDeleteAllSubtasks() {
        manager.deleteAllSubtasks();
        assertTrue(manager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldGetHistory() {
        manager.getTaskById(task.getId());
        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subtask.getId());

        List<Task> history = manager.getHistory();
        assertEquals(3, history.size());
        assertTrue(history.contains(task));
        assertTrue(history.contains(epic));
        assertTrue(history.contains(subtask));
    }

    @Test
    void shouldRemoveEpicAndItsSubtasksFromHistory() {
        Epic epic = new Epic("Epic Title", "Epic Description");
        manager.createEpic(epic);

        SubTask sub1 = new SubTask("Subtask 1", "Description", TaskStatus.NEW, epic.getId());
        SubTask sub2 = new SubTask("Subtask 2", "Description", TaskStatus.DONE, epic.getId());

        manager.createSubTask(sub1);
        manager.createSubTask(sub2);

        manager.getEpicById(epic.getId());
        manager.getSubtaskById(sub1.getId());
        manager.getSubtaskById(sub2.getId());

        manager.deleteEpic(epic.getId());

        List<Task> history = manager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой после удаления эпика и всех его подзадач.");
    }
}