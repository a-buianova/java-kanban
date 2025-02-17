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
    private File testFile;

    @BeforeEach
    void setUp() throws IOException {
        testFile = new File(TEST_FILE_PATH);
        if (!testFile.exists()) {
            testFile.createNewFile(); // Создать файл, если он отсутствует
        }
        manager = new FileBackedTaskManager(testFile);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(testFile.toPath());
    }

    @Test
    void shouldSaveAndLoadTasks() throws IOException {
        // Проверка, что задачи корректно сохраняются и загружаются из файла
        Task task = new Task(1, "Task1", "Description1", TaskStatus.NEW);
        manager.createTask(task);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        assertEquals(1, loadedManager.getAllTasks().size());
        assertEquals("Task1", loadedManager.getAllTasks().get(0).getTitle());
        assertEquals(TaskStatus.NEW, loadedManager.getAllTasks().get(0).getStatus());
    }

    @Test
    void shouldSaveAndLoadEpicsAndSubtasks() throws IOException {
        // Проверка, что эпики и связанные с ними подзадачи корректно сохраняются и загружаются
        Epic epic = new Epic(2, "Epic1", "EpicDescription1");
        manager.createEpic(epic);

        SubTask subTask = new SubTask(3, "SubTask1", "SubTaskDescription1", TaskStatus.IN_PROGRESS, epic.getId());
        manager.createSubTask(subTask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(1, loadedManager.getSubtasksForEpic(epic.getId()).size());

        Epic loadedEpic = loadedManager.getAllEpics().get(0);
        SubTask loadedSubTask = loadedManager.getSubtasksForEpic(loadedEpic.getId()).get(0);

        assertEquals("Epic1", loadedEpic.getTitle());
        assertEquals("SubTask1", loadedSubTask.getTitle());
    }

    @Test
    void shouldHandleEmptyFile() throws IOException {
        // Проверка, что менеджер корректно работает с пустым файлом
        if (!testFile.exists()) {
            testFile.createNewFile(); // Создать пустой файл
        }
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
    }

    @Test
    void shouldThrowExceptionForCorruptedFile() {
        // Проверка, что выбрасывается исключение при загрузке поврежденного файла
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(testFile))) {
            writer.write("corrupted,data\n");
        } catch (IOException e) {
            fail("Setup failed: could not write to test file.");
        }

        assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(testFile));
    }

    @Test
    void shouldSaveAfterTaskDeletion() throws IOException {
        // Проверка, что удаление задачи корректно сохраняется в файл
        Task task = new Task(1, "Task1", "Description1", TaskStatus.NEW);
        manager.createTask(task);
        manager.deleteTask(task.getId());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        assertTrue(loadedManager.getAllTasks().isEmpty());
    }
}
