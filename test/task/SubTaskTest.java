package task;

import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubTaskTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    // Проверка, что подзадача не может быть своим собственным эпиком
    @Test
    void testSubtaskCannotBeItsOwnEpic() {

        SubTask subtask = new SubTask("Subtask 1", "Description", Duration.ofMinutes(15), LocalDateTime.of(2025, 5, 2, 10, 0), 1);

        assertThrows(IllegalArgumentException.class, () -> taskManager.createSubTask(subtask));
    }

    //Проверка, что можно создать подзадачу с правильным статусом
    @Test
    void testCreateSubtaskWithCorrectStatus() {
        Epic epic = new Epic("Epic 1", "Description");
        taskManager.createEpic(epic);
        SubTask subtask = new SubTask("Subtask 1", "Description", Duration.ofMinutes(15), LocalDateTime.of(2025, 5, 2, 10, 0), epic.getId());

        SubTask createdSubtask = taskManager.createSubTask(subtask);
        assertTrue(taskManager.getSubtask(createdSubtask.getId()).isPresent()); // Подзадача должна быть добавлена в менеджер
        assertEquals(TaskStatus.NEW, taskManager.getSubtask(createdSubtask.getId()).orElseThrow().getStatus()); // Статус должен быть NEW
    }

    //Проверка, что можно обновить статус подзадачи
    @Test
    void testUpdateSubtaskStatus() {
        Epic epic = new Epic("Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask = new SubTask("Subtask 1", "Description", Duration.ofMinutes(15), LocalDateTime.of(2025, 5, 2, 10, 0), epic.getId());
        SubTask createdSubTask = taskManager.createSubTask(subtask);

        createdSubTask.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.updateSubTask(createdSubTask);

        assertEquals(TaskStatus.IN_PROGRESS, taskManager.getSubtask(createdSubTask.getId()).orElseThrow().getStatus()); // Статус подзадачи должен быть обновлен на IN_PROGRESS
    }

    //Проверка, что подзадача имеет правильный статус после удаления
    @Test
    void testDeleteSubtask() {
        Epic epic = new Epic("Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask = new SubTask("Subtask 1", "Description", Duration.ofMinutes(15), LocalDateTime.of(2025, 5, 2, 10, 0), epic.getId());
        taskManager.createSubTask(subtask);

        taskManager.deleteSubtask(subtask.getId());
        assertTrue(taskManager.getSubtask(subtask.getId()).isEmpty()); // Подзадача должна быть удалена
    }

    //Проверка, что не может быть создана подзадача с несуществующим эпиком
    @Test
    void testCreateSubtaskWithNonExistentEpic() {
        SubTask subtask = new SubTask("Subtask 1", "Description", Duration.ofMinutes(15), LocalDateTime.of(2025, 5, 2, 10, 0), 99); // Не существует эпика с таким ID

        assertThrows(IllegalArgumentException.class, () -> taskManager.createSubTask(subtask)); // Должна быть ошибка при попытке создать подзадачу
    }

    //Проверка, что при удалении эпика, все его подзадачи удаляются
    @Test
    void testDeleteEpicRemovesSubtasks() {
        Epic epic = new Epic("Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask = new SubTask("Subtask 1", "Description", Duration.ofMinutes(15), LocalDateTime.of(2025, 5, 2, 10, 0), epic.getId());
        taskManager.createSubTask(subtask);

        taskManager.deleteEpic(epic.getId());
        assertTrue(taskManager.getSubtask(subtask.getId()).isEmpty()); // Подзадача должна быть удалена вместе с эпиком
    }

    @Test
    void subtaskWithNullTimingShouldBeHandledGracefully() {
        Epic epic = new Epic("Epic with null-time subtask", "desc");
        taskManager.createEpic(epic);

        // Подзадача без времени
        SubTask subtask = new SubTask("Subtask without time", "desc", epic.getId());
        taskManager.createSubTask(subtask);

        // Проверка, что она не влияет на поля времени эпика
        Epic updatedEpic = taskManager.getEpic(epic.getId()).orElseThrow();

        assertEquals(Duration.ZERO, updatedEpic.getDuration(), "Продолжительность эпика должна быть 0 при отсутствии времени у подзадач");
        assertNull(updatedEpic.getStartTime(), "startTime эпика должен быть null");
        assertNull(updatedEpic.getEndTime(), "endTime эпика должен быть null");

        assertFalse(taskManager.getPrioritizedTasks().contains(subtask), "Подзадача без времени не должна попадать в приоритезированный список");
    }
}