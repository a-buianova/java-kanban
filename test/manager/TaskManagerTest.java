package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;
import task.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        // Инициализация менеджера задач перед каждым тестом
        taskManager = new InMemoryTaskManager();
    }

    // Проверка: задача успешно создаётся и извлекается
    @Test
    void testCreateAndGetTask() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        taskManager.createTask(task);

        Task fetchedTask = taskManager.getTask(1);
        assertNotNull(fetchedTask);
        assertEquals(task.getId(), fetchedTask.getId());
    }

    // Проверка: задача успешно удаляется
    @Test
    void testDeleteTask() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        taskManager.createTask(task);

        taskManager.deleteTask(1);
        assertNull(taskManager.getTask(1));
    }

    // Проверка: эпик и его подзадачи успешно создаются и извлекаются
    @Test
    void testCreateAndGetEpicWithSubtasks() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask = new SubTask(2, "Subtask 1", "Description", TaskStatus.NEW, 1);
        taskManager.createSubTask(subtask);

        List<SubTask> subtasks = taskManager.getSubtasksForEpic(1);
        assertEquals(1, subtasks.size());
        assertEquals(subtask.getId(), subtasks.get(0).getId());
    }

    // Проверка: при удалении эпика его подзадачи также удаляются
    @Test
    void testDeleteEpicWithSubtasks() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask = new SubTask(2, "Subtask 1", "Description", TaskStatus.NEW, 1);
        taskManager.createSubTask(subtask);

        taskManager.deleteEpic(1);

        assertNull(taskManager.getEpic(1));
        assertNull(taskManager.getSubtask(2));
    }

    // Проверка: обновление полей задачи через сеттеры сохраняется в менеджере
    @Test
    void testUpdateTaskFields() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        taskManager.createTask(task);

        Task fetchedTask = taskManager.getTask(1);
        fetchedTask.setTitle("Updated Task");
        fetchedTask.setStatus(TaskStatus.IN_PROGRESS);

        Task updatedTask = taskManager.getTask(1);
        assertEquals("Updated Task", updatedTask.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.getStatus());
    }

    // Проверка: все задачи, эпики и подзадачи успешно удаляются
    @Test
    void testDeleteAllTasks() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        Epic epic = new Epic(2, "Epic 1", "Description");
        SubTask subtask = new SubTask(3, "Subtask 1", "Description", TaskStatus.NEW, 2);

        taskManager.createTask(task);
        taskManager.createEpic(epic);
        taskManager.createSubTask(subtask);

        taskManager.deleteAllTasks();
        taskManager.deleteAllEpics();
        taskManager.deleteAllSubtasks();

        assertTrue(taskManager.getAllTasks().isEmpty());
        assertTrue(taskManager.getAllEpics().isEmpty());
        assertTrue(taskManager.getSubtasksForEpic(2).isEmpty());
    }

    // Проверка удаления задачи из истории просмотров
    @Test
    void testHistoryAfterTaskDeletion() {
        Task task = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        taskManager.createTask(task);
        taskManager.getTask(1); // Добавляем задачу в историю

        taskManager.deleteTask(1); // Удаляем задачу
        List<Task> history = taskManager.getHistory();
        assertTrue(history.isEmpty()); // История должна быть пустой
    }

    // Проверка отсутствия повторов в истории
    @Test
    void testHistoryWithRepeatedViews() {
        Task task1 = new Task(1, "Task 1", "Description", TaskStatus.NEW);
        Task task2 = new Task(2, "Task 2", "Description", TaskStatus.NEW);

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        taskManager.getTask(1);
        taskManager.getTask(2);
        taskManager.getTask(1); // Повторный просмотр task1

        List<Task> history = taskManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2, history.get(0)); // task2 последний просмотрен
        assertEquals(task1, history.get(1)); // task1 добавлен снова в конец
    }

    // Проверка обновления статуса эпика на DONE, если все подзадачи выполнены
    @Test
    void testEpicStatusWhenAllSubtasksDone() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask1 = new SubTask(2, "Subtask 1", "Description", TaskStatus.DONE, 1);
        SubTask subtask2 = new SubTask(3, "Subtask 2", "Description", TaskStatus.DONE, 1);
        taskManager.createSubTask(subtask1);
        taskManager.createSubTask(subtask2);

        assertEquals(TaskStatus.DONE, epic.getStatus());
    }

    // Проверка обновления статуса эпика при изменении статуса подзадачи
    @Test
    void testUpdateEpicStatusBasedOnSubtasks() {
        Epic epic = new Epic(1, "Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask = new SubTask(2, "Subtask 1", "Description", TaskStatus.IN_PROGRESS, 1);
        taskManager.createSubTask(subtask);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }


    // Проверка: статус эпика должен выдать ошибку, если статус подзадачи некорректен или отсутствует
    @Test
    void testUpdateEpicStatusWithInvalidSubtaskStatus() {
        Epic epic = new Epic(1, "Epic", "Description");
        taskManager.createEpic(epic);

        // Создаем подзадачу с некорректным (null) статусом
        SubTask subtask = new SubTask(2, "Subtask", "Description", null, epic.getId());

        // Проверяем, что создание подзадачи с некорректным статусом вызывает исключение
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskManager.createSubTask(subtask);
        });

        assertEquals("Статус подзадачи не может быть null.", exception.getMessage());
    }

    // Проверка: нельзя добавить одну и ту же подзадачу дважды в эпик
    @Test
    void testAddDuplicateSubtaskToEpic() {
        Epic epic = new Epic(1, "Epic", "Description");
        taskManager.createEpic(epic);

        SubTask subtask = new SubTask(2, "Subtask", "Description", TaskStatus.NEW, epic.getId());
        epic.addSubtask(subtask);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            epic.addSubtask(subtask);
        });

        assertEquals("Подзадача с ID 2 уже добавлена.", exception.getMessage());
    }


    // Проверка: невозможно создать подзадачу с некорректным ID эпика
    @Test
    void testCreateSubTaskWithInvalidEpicId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new SubTask(1, "Subtask", "Description", TaskStatus.NEW, -1);
        });

        assertEquals("ID эпика должен быть положительным числом.", exception.getMessage());
    }


}