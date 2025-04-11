package manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {
    private Path testFilePath;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setup() throws IOException {
        File tempFile = File.createTempFile("task_manager_test", ".csv");
        testFilePath = tempFile.toPath();
        manager = new FileBackedTaskManager(testFilePath);
    }

    @AfterEach
    void cleanup() throws IOException {
        Files.deleteIfExists(testFilePath);
    }

    @Test
    void shouldSaveAndLoadTasks() {
        Task task = new Task("Task1", "Description1");
        manager.createTask(task);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFilePath);

        assertEquals(1, loadedManager.getAllTasks().size());
        assertEquals("Task1", loadedManager.getAllTasks().get(0).getTitle());
        assertEquals(TaskStatus.NEW, loadedManager.getAllTasks().get(0).getStatus());
    }

    @Test
    void shouldSaveAndLoadEpicsAndSubtasks() {
        Epic epic = new Epic("Epic1", "Epic Description");
        manager.createEpic(epic);

        SubTask subTask = new SubTask("SubTask1", "Sub Description", TaskStatus.IN_PROGRESS, epic.getId());
        manager.createSubTask(subTask);

        manager.getEpicById(epic.getId());
        manager.getSubtaskById(subTask.getId());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFilePath);

        List<Epic> epics = loadedManager.getAllEpics();
        assertEquals(1, epics.size());

        Epic loadedEpic = epics.get(0);
        assertEquals("Epic1", loadedEpic.getTitle());

        List<SubTask> subtasks = loadedManager.getAllSubtasks();
        assertEquals(1, subtasks.size());
        assertEquals("SubTask1", subtasks.get(0).getTitle());
        assertEquals(loadedEpic.getId(), subtasks.get(0).getEpicId());
    }

    @Test
    void shouldHandleEmptyFile() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFilePath);
        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldThrowExceptionForCorruptedFile() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(testFilePath)) {
            writer.write("corrupted,data\n");
        }

        assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(testFilePath));
    }

    @Test
    void shouldSaveAfterTaskDeletion() {
        Task task = new Task("Task1", "Description1");
        manager.createTask(task);
        manager.deleteTask(task.getId());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFilePath);
        assertTrue(loadedManager.getAllTasks().isEmpty());
    }

    @Test
    void shouldSaveAfterTaskUpdate() {
        Task task = new Task("Task1", "Description1");
        manager.createTask(task);

        task.setTitle("Updated Task1");
        task.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(task);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFilePath);

        Task updatedTask = loadedManager.getAllTasks().get(0);
        assertEquals("Updated Task1", updatedTask.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.getStatus());
    }

    @Test
    void shouldSaveAndRestoreAllTaskTypes() {
        Task task = new Task("Task", "Description");
        Epic epic = new Epic("Epic", "EpicDesc");
        manager.createTask(task);
        manager.createEpic(epic);
        manager.createSubTask(new SubTask("Subtask", "Desc", TaskStatus.NEW, epic.getId()));

        FileBackedTaskManager restored = FileBackedTaskManager.loadFromFile(testFilePath);

        assertEquals(1, restored.getAllTasks().size());
        assertEquals(1, restored.getAllEpics().size());
        assertEquals(1, restored.getAllSubtasks().size());
    }
}