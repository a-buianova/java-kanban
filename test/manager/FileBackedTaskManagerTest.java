package manager;

import org.junit.jupiter.api.*;
import task.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private static final String TEST_FILE_PATH = "test_tasks.csv";
    private FileBackedTaskManager manager;
    private Path testFilePath;

    @BeforeEach
    void setUp() throws IOException {
        testFilePath = Paths.get(TEST_FILE_PATH);
        Files.deleteIfExists(testFilePath); // Очищаем файл перед каждым тестом
        Files.createFile(testFilePath); // Создаем новый пустой файл
        manager = new FileBackedTaskManager(testFilePath);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(testFilePath); // Удаляем файл после каждого теста
    }

    @Test
    void shouldSaveAndLoadTasks() throws IOException {
        Task task = new Task(1, "Task1", "Description1", TaskStatus.NEW);
        manager.createTask(task);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFilePath);

        assertEquals(1, loadedManager.getAllTasks().size());
        assertEquals("Task1", loadedManager.getAllTasks().get(0).getTitle());
        assertEquals(TaskStatus.NEW, loadedManager.getAllTasks().get(0).getStatus());
    }

    @Test
    void shouldSaveAndLoadEpicsAndSubtasks() throws IOException {
        Epic epic = new Epic(2, "Epic1", "EpicDescription1");
        manager.createEpic(epic);

        SubTask subTask = new SubTask(3, "SubTask1", "SubTaskDescription1", TaskStatus.IN_PROGRESS, epic.getId());
        manager.createSubTask(subTask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFilePath);

        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(1, loadedManager.getSubtasksForEpic(epic.getId()).size());

        Epic loadedEpic = loadedManager.getAllEpics().get(0);
        SubTask loadedSubTask = loadedManager.getSubtasksForEpic(loadedEpic.getId()).get(0);

        assertEquals("Epic1", loadedEpic.getTitle());
        assertEquals("SubTask1", loadedSubTask.getTitle());
    }

    @Test
    void shouldHandleEmptyFile() throws IOException {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFilePath);

        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
    }

    @Test
    void shouldThrowExceptionForCorruptedFile() throws IOException {
        // Записываем поврежденные данные в testFile
        try (BufferedWriter writer = Files.newBufferedWriter(testFilePath)) {
            writer.write("corrupted,data\n");
        }

        assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(testFilePath));
    }

    @Test
    void shouldSaveAfterTaskDeletion() throws IOException {
        Task task = new Task(1, "Task1", "Description1", TaskStatus.NEW);
        manager.createTask(task);
        manager.deleteTask(task.getId());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFilePath);

        assertTrue(loadedManager.getAllTasks().isEmpty());
    }

    @Test
    void shouldSaveAfterTaskUpdate() throws IOException {
        Task task = new Task(1, "Task1", "Description1", TaskStatus.NEW);
        manager.createTask(task);

        task.setTitle("Updated Task1");
        task.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(task);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFilePath);

        assertEquals(1, loadedManager.getAllTasks().size());
        Task updatedTask = loadedManager.getAllTasks().get(0);
        assertEquals("Updated Task1", updatedTask.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.getStatus());
    }
}
