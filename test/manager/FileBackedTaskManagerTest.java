package manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;
import task.TaskStatus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private Path testFilePath;

    @BeforeEach
    @Override
    void setUp() {
        try {
            File tempFile = File.createTempFile("task_manager_test", ".csv");
            testFilePath = tempFile.toPath();
            manager = new FileBackedTaskManager(testFilePath);

            // Очищаем файл перед каждым тестом
            Files.writeString(testFilePath, "id,type,name,status,description,epic,startTime,duration\n");

            // Только epic и сабы, чтобы не конфликтовать с отдельными тестами на Task
            epic = manager.createEpic(new Epic("Epic 1", "Epic desc"));
            sub1 = new SubTask("Sub 1", "desc", Duration.ofMinutes(30),
                    LocalDateTime.of(2025, 5, 2, 12, 0), epic.getId());
            sub2 = new SubTask("Sub 2", "desc", Duration.ofMinutes(45),
                    LocalDateTime.of(2025, 5, 2, 13, 0), epic.getId());

            manager.createSubTask(sub1);
            manager.createSubTask(sub2);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при создании временного файла", e);
        }
    }

    @AfterEach
    void cleanup() throws IOException {
        Files.deleteIfExists(testFilePath);
    }

    @Override
    protected FileBackedTaskManager createManager() {
        return new FileBackedTaskManager(testFilePath);
    }

    @Test
    void shouldSaveAndLoadSingleTask() {
        manager.deleteAllTasks();  // чтобы тест работал изолированно

        Task task = new Task("Task1", "Description1", Duration.ofHours(1),
                LocalDateTime.of(2025, 5, 2, 14, 0));
        manager.createTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(testFilePath);
        Task loadedTask = loaded.getTask(task.getId()).orElseThrow();

        assertEquals("Task1", loadedTask.getTitle());
        assertEquals(TaskStatus.NEW, loadedTask.getStatus());
        assertEquals(Duration.ofHours(1), loadedTask.getDuration());
        assertEquals(LocalDateTime.of(2025, 5, 2, 14, 0), loadedTask.getStartTime());
    }

    @Test
    void shouldSaveAndLoadEpicAndSubtask() {
        manager.deleteAllEpics(); // очищаем начальные данные

        Epic epic = new Epic("Epic1", "Epic Desc");
        manager.createEpic(epic);

        SubTask sub = new SubTask("Sub1", "SubDesc", Duration.ofMinutes(30),
                LocalDateTime.of(2025, 5, 2, 12, 0), epic.getId());
        manager.createSubTask(sub);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(testFilePath);

        List<Epic> epics = loaded.getAllEpics();
        List<SubTask> subs = loaded.getAllSubTasks();

        assertEquals(1, epics.size());
        assertEquals("Epic1", epics.get(0).getTitle());
        assertEquals(1, subs.size());
        assertEquals("Sub1", subs.get(0).getTitle());
        assertEquals(epics.get(0).getId(), subs.get(0).getEpicId());
        assertTrue(epics.get(0).getSubtaskIds().contains(subs.get(0).getId()));
    }

    @Test
    void shouldHandleEmptyFileCorrectly() {
        try {
            Files.writeString(testFilePath, "id,type,name,status,description,epic,startTime,duration\n");
        } catch (IOException e) {
            fail("Не удалось перезаписать файл: " + e.getMessage());
        }
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(testFilePath);
        assertTrue(loaded.getAllTasks().isEmpty());
        assertTrue(loaded.getAllEpics().isEmpty());
        assertTrue(loaded.getAllSubTasks().isEmpty());
    }

    @Test
    void shouldThrowExceptionOnCorruptedFile() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(testFilePath)) {
            writer.write("id,type,name,status,description,epic,startTime,duration");
            writer.newLine();
            writer.write("1,WRONGTYPE,Name,NEW,Desc,,2025-05-02T10:00,30"); // некорректный тип
        }

        assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(testFilePath));
    }

    @Test
    void shouldReflectTaskDeletionInFile() {
        Task task = new Task("ToDelete", "D", Duration.ofMinutes(10), LocalDateTime.of(2025, 5, 5, 15, 0));
        manager.createTask(task);
        manager.deleteTask(task.getId());

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(testFilePath);
        assertTrue(loaded.getAllTasks().isEmpty());
    }

    @Test
    void shouldReflectTaskUpdateInFile() {
        Task task = new Task("Original", "D", Duration.ofMinutes(10), LocalDateTime.of(2025, 5, 5, 15, 0));
        manager.createTask(task);

        task.setTitle("Updated");
        task.setStatus(TaskStatus.DONE);
        manager.updateTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(testFilePath);
        Task updated = loaded.getTask(task.getId()).orElseThrow();
        assertEquals("Updated", updated.getTitle());
        assertEquals(TaskStatus.DONE, updated.getStatus());
    }

    @Test
    void shouldSaveAllTypes() {
        manager.deleteAllTasks();
        manager.deleteAllEpics();
        manager.deleteAllSubtasks();

        manager.createTask(new Task("T", "D", Duration.ofMinutes(15), LocalDateTime.of(2025, 5, 1, 12, 0)));
        Epic epic = manager.createEpic(new Epic("E", "E-desc"));
        manager.createSubTask(new SubTask("S", "S-desc", Duration.ofMinutes(30),
                LocalDateTime.of(2025, 5, 2, 9, 0), epic.getId()));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(testFilePath);

        assertEquals(1, loaded.getAllTasks().size());
        assertEquals(1, loaded.getAllEpics().size());
        assertEquals(1, loaded.getAllSubTasks().size());
    }

    @Test
    void shouldSaveAndLoadHistory() {
        manager.deleteAllTasks();
        Task task = manager.createTask(new Task("T", "D", Duration.ofMinutes(15), LocalDateTime.of(2025, 5, 1, 12, 0)));
        manager.getTask(task.getId());

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(testFilePath);
        List<Task> history = loaded.getHistory();
        assertEquals(1, history.size());
        assertEquals(task.getId(), history.get(0).getId());
    }

    @Test
    void shouldCalculateEndTimeForTaskCorrectly() {
        Task task = new Task("Task", "Desc", Duration.ofMinutes(90), LocalDateTime.of(2025, 5, 1, 10, 0));
        manager.createTask(task);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(testFilePath);
        Task loadedTask = loaded.getTask(task.getId()).orElseThrow();
        assertEquals(LocalDateTime.of(2025, 5, 1, 11, 30), loadedTask.getEndTime());
    }

    @Test
    void shouldCalculateEndTimeForSubTaskCorrectly() {
        Epic epic = manager.createEpic(new Epic("Epic", "EpicDesc"));
        SubTask sub = new SubTask("Sub", "Desc", Duration.ofMinutes(60),
                LocalDateTime.of(2025, 5, 2, 9, 0), epic.getId());
        manager.createSubTask(sub);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(testFilePath);
        SubTask loadedSub = loaded.getSubtask (sub.getId()).orElseThrow();
        assertEquals(LocalDateTime.of(2025, 5, 2, 10, 0), loadedSub.getEndTime());
    }

    @Test
    void shouldCalculateEndTimeForEpicFromSubtasks() {
        manager.deleteAllTasks();
        manager.deleteAllEpics();
        manager.deleteAllSubtasks();

        Epic epic = manager.createEpic(new Epic("Epic", "EpicDesc"));
        SubTask sub1 = new SubTask("Sub1", "D1", Duration.ofMinutes(30),
                LocalDateTime.of(2025, 5, 2, 10, 0), epic.getId());
        SubTask sub2 = new SubTask("Sub2", "D2", Duration.ofMinutes(45),
                LocalDateTime.of(2025, 5, 2, 12, 0), epic.getId());
        manager.createSubTask(sub1);
        manager.createSubTask(sub2);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(testFilePath);
        Epic loadedEpic = loaded.getEpic(epic.getId()).orElseThrow();
        assertEquals(LocalDateTime.of(2025, 5, 2, 12, 45), loadedEpic.getEndTime());
    }
}