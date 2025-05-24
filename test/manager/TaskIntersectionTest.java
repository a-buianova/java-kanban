package manager;

import exception.TaskIntersectionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskIntersectionTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void shouldThrowExceptionWhenTasksOverlap() {
        // Создаём первую задачу с 10:00 до 11:00
        Task task1 = new Task("Task 1", "Description",
                Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 4, 10, 0));
        taskManager.createTask(task1);

        // Попытка создать вторую задачу, пересекающуюся с первой (10:30–11:30)
        Task task2 = new Task("Task 2", "Description",
                Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 4, 10, 30));

        TaskIntersectionException exception = assertThrows(
                TaskIntersectionException.class,
                () -> taskManager.createTask(task2)
        );

        assertEquals("Задача пересекается по времени с другой задачей.", exception.getMessage());
    }
    @Test
    void shouldAllowNonOverlappingTasks() {
        Task task1 = new Task("Morning task", "Desc",
                Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 4, 8, 0));
        Task task2 = new Task("Evening task", "Desc",
                Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 4, 18, 0));

        assertDoesNotThrow(() -> {
            taskManager.createTask(task1);
            taskManager.createTask(task2);
        });
    }

    @Test
    void shouldThrowExceptionWhenUpdatingTaskToOverlap() {
        Task task1 = new Task("Task 1", "Desc",
                Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 4, 10, 0));
        taskManager.createTask(task1);

        Task task2 = new Task("Task 2", "Desc",
                Duration.ofMinutes(60), LocalDateTime.of(2025, 5, 4, 12, 0));
        taskManager.createTask(task2);

        // Попробуем обновить task2 так, чтобы он пересекался с task1
        task2.setStartTime(LocalDateTime.of(2025, 5, 4, 10, 30));

        TaskIntersectionException exception = assertThrows(
                TaskIntersectionException.class,
                () -> taskManager.updateTask(task2)
        );

        assertEquals("Задача пересекается по времени с другой задачей.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenSubTasksOverlap() {
        Epic epic = taskManager.createEpic(new Epic("Epic", "Desc"));

        SubTask sub1 = new SubTask("Sub1", "Desc",
                Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 4, 9, 0), epic.getId());
        taskManager.createSubTask(sub1);

        SubTask sub2 = new SubTask("Sub2", "Desc",
                Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 4, 9, 15), epic.getId());

        TaskIntersectionException exception = assertThrows(
                TaskIntersectionException.class,
                () -> taskManager.createSubTask(sub2)
        );

        assertEquals("Подзадача пересекается по времени с другой задачей.", exception.getMessage());
    }
}