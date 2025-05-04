package task;

import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EpicTest {
    private TaskManager taskManager;
    private Epic epic;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
        epic = taskManager.createEpic(new Epic("Epic Test", "Test description"));
    }

    // Проверка, что объект Epic нельзя добавить в самого себя в виде подзадачи
    @Test
    void testEpicCannotAddItselfAsSubtask() {
        // Тест на такой кейс написать не возможно, т.к. метод taskManager.createSubTask() принимает только
        // объекты типа SubTask, таким образом тест просто не скомпилируется.
    }

    // Проверка добавления подзадач в эпик
    @Test
    void testAddSubtaskToEpic() {
        Epic epic = new Epic("Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask = new SubTask("Subtask 1", "Description",
                Duration.ofMinutes(15), LocalDateTime.of(2025, 5, 2, 10, 0), epic.getId());
        taskManager.createSubTask(subtask);
    }

    // Проверка, что эпик без подзадач имеет статус NEW
    @Test
    void testEpicStatusWhenNoSubtasks() {
        Epic epic = new Epic("Epic 1", "Description");
        taskManager.createEpic(epic);

        assertEquals(TaskStatus.NEW, epic.getStatus()); // Статус эпика должен быть NEW, так как у него нет подзадач
    }

    // Проверка, что статус эпика обновляется при добавлении подзадач
    @Test
    void testEpicStatusChangedBasedOnSubtaskStatus() {
        Epic epic = new Epic("Epic 1", "Description");
        taskManager.createEpic(epic);
        assertEquals(TaskStatus.NEW, epic.getStatus());

        SubTask subtask = new SubTask("Subtask 1", "Description",
                Duration.ofMinutes(15), LocalDateTime.of(2025, 5, 2, 10, 0), epic.getId());
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        taskManager.createSubTask(subtask);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    // Проверка, что статус эпика обновляется на DONE, если все подзадачи выполнены
    @Test
    void testEpicStatusWhenAllSubtasksDone() {
        Epic epic = new Epic("Epic 1", "Description");
        taskManager.createEpic(epic);

        SubTask subtask1 = new SubTask("Subtask 1", "Description",
                Duration.ofMinutes(15), LocalDateTime.of(2025, 5, 2, 10, 0), epic.getId());
        SubTask subtask2 = new SubTask("Subtask 2", "Description",
                Duration.ofMinutes(15), LocalDateTime.of(2025, 5, 2, 11, 0), epic.getId());

        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);
        taskManager.createSubTask(subtask1);
        taskManager.createSubTask(subtask2);

        assertEquals(TaskStatus.DONE, epic.getStatus());
    }

    // Новый тест: все подзадачи NEW => Epic должен быть NEW
    @Test
    void epicStatusShouldBeNewIfAllSubtasksAreNew() {
        SubTask sub1 = new SubTask("Sub1", "desc", Duration.ofMinutes(10), LocalDateTime.now(), epic.getId());
        SubTask sub2 = new SubTask("Sub2", "desc", Duration.ofMinutes(10), LocalDateTime.now().plusMinutes(15), epic.getId());

        taskManager.createSubTask(sub1);
        taskManager.createSubTask(sub2);

        Epic updatedEpic = taskManager.getEpic(epic.getId()).orElseThrow();
        assertEquals(TaskStatus.NEW, updatedEpic.getStatus(), "Все подзадачи NEW => Epic должен быть NEW");
    }

    // Новый тест: смешанные NEW и DONE => Epic должен быть IN_PROGRESS
    @Test
    void epicStatusShouldBeInProgressIfMixedNewAndDone() {
        SubTask sub1 = new SubTask("Sub1", "desc", Duration.ofMinutes(10), LocalDateTime.now(), epic.getId());
        SubTask sub2 = new SubTask("Sub2", "desc", Duration.ofMinutes(10), LocalDateTime.now().plusMinutes(15), epic.getId());
        sub1.setStatus(TaskStatus.NEW);
        sub2.setStatus(TaskStatus.DONE);

        taskManager.createSubTask(sub1);
        taskManager.createSubTask(sub2);

        Epic updatedEpic = taskManager.getEpic(epic.getId()).orElseThrow();
        assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getStatus(), "Смешанные NEW и DONE => Epic должен быть IN_PROGRESS");
    }

    // Новый тест: все IN_PROGRESS => Epic должен быть IN_PROGRESS
    @Test
    void epicStatusShouldBeInProgressIfAllSubtasksInProgress() {
        SubTask sub1 = new SubTask("Sub1", "desc", Duration.ofMinutes(10), LocalDateTime.now(), epic.getId());
        SubTask sub2 = new SubTask("Sub2", "desc", Duration.ofMinutes(10), LocalDateTime.now().plusMinutes(15), epic.getId());
        sub1.setStatus(TaskStatus.IN_PROGRESS);
        sub2.setStatus(TaskStatus.IN_PROGRESS);

        taskManager.createSubTask(sub1);
        taskManager.createSubTask(sub2);

        Epic updatedEpic = taskManager.getEpic(epic.getId()).orElseThrow();
        assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getStatus(), "Все IN_PROGRESS => Epic должен быть IN_PROGRESS");
    }

    // Проверка расчёта времени старта, окончания и продолжительности эпика
    @Test
    void shouldCalculateTimeFieldsForEpicCorrectly() {
        SubTask sub1 = new SubTask("Sub 1", "desc", Duration.ofMinutes(10),
                LocalDateTime.of(2025, 5, 2, 10, 0), epic.getId());
        SubTask sub2 = new SubTask("Sub 2", "desc", Duration.ofMinutes(20),
                LocalDateTime.of(2025, 5, 2, 12, 0), epic.getId());

        taskManager.createSubTask(sub1);
        taskManager.createSubTask(sub2);

        Epic updatedEpic = taskManager.getEpic(epic.getId()).orElseThrow();

        assertEquals(Duration.ofMinutes(30), updatedEpic.getDuration(), "Общая продолжительность эпика должна быть 30 минут.");
        assertEquals(LocalDateTime.of(2025, 5, 2, 10, 0), updatedEpic.getStartTime(), "startTime должен быть самым ранним");
        assertEquals(LocalDateTime.of(2025, 5, 2, 12, 20), updatedEpic.getEndTime(), "endTime должен быть самым поздним");
    }

    // Epic без подзадач не должен иметь времени
    @Test
    void epicWithoutSubtasksShouldHaveNoTiming() {
        Epic updatedEpic = taskManager.getEpic(epic.getId()).orElseThrow();
        assertEquals(Duration.ZERO, updatedEpic.getDuration());
        assertNull(updatedEpic.getStartTime());
        assertNull(updatedEpic.getEndTime());
    }

    @Test
    void epicShouldIgnoreSubtaskWithoutStartTimeInTimingCalculation() {
        SubTask sub1 = new SubTask("Sub with time", "desc",
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 5, 2, 10, 0),
                epic.getId());

        SubTask sub2 = new SubTask("Sub without time", "desc",
                null, null, epic.getId()); // нет времени

        taskManager.createSubTask(sub1);
        taskManager.createSubTask(sub2);

        Epic updatedEpic = taskManager.getEpic(epic.getId()).orElseThrow();

        assertEquals(Duration.ofMinutes(30), updatedEpic.getDuration(), "Эпик должен учитывать только подзадачи с временем");
        assertEquals(LocalDateTime.of(2025, 5, 2, 10, 0), updatedEpic.getStartTime(), "startTime должен быть только по sub1");
        assertEquals(LocalDateTime.of(2025, 5, 2, 10, 30), updatedEpic.getEndTime(), "endTime должен быть только по sub1");
    }
}