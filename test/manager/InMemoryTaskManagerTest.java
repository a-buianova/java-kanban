package manager;

import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager();
    }

    // Проверка добавления подзадач в эпик
    @Test
    void testAddSubtaskToEpic() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);

        SubTask subtask = new SubTask("Subtask 1", "Description",
                Duration.ofMinutes(15), LocalDateTime.of(2025, 5, 2, 16, 0), epic.getId()); // <-- безопасное время
        manager.createSubTask(subtask);
    }

    // Проверка, что эпик без подзадач имеет статус NEW
    @Test
    void testEpicStatusWhenNoSubtasks() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);

        assertEquals(TaskStatus.NEW, epic.getStatus()); // Статус эпика должен быть NEW, так как у него нет подзадач
    }

    // Проверка, что статус эпика обновляется при добавлении подзадач
    @Test
    void testEpicStatusChangedBasedOnSubtaskStatus() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);
        assertEquals(TaskStatus.NEW, epic.getStatus());

        SubTask subtask = new SubTask("Subtask 1", "Description",
                Duration.ofMinutes(15), LocalDateTime.of(2025, 5, 3, 10, 0), epic.getId());
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        manager.createSubTask(subtask);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    // Проверка, что статус эпика обновляется на DONE, если все подзадачи выполнены
    @Test
    void testEpicStatusWhenAllSubtasksDone() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);

        SubTask subtask1 = new SubTask("Subtask 1", "Description",
                Duration.ofMinutes(15), LocalDateTime.of(2025, 5, 3, 10, 0), epic.getId());
        SubTask subtask2 = new SubTask("Subtask 2", "Description",
                Duration.ofMinutes(15), LocalDateTime.of(2025, 5, 3, 11, 0), epic.getId());

        subtask1.setStatus(TaskStatus.DONE);
        subtask2.setStatus(TaskStatus.DONE);
        manager.createSubTask(subtask1);
        manager.createSubTask(subtask2);

        assertEquals(TaskStatus.DONE, epic.getStatus());
    }

    // Новый тест: все подзадачи NEW => Epic должен быть NEW
    @Test
    void epicStatusShouldBeNewIfAllSubtasksAreNew() {
        SubTask sub1 = new SubTask("Sub1", "desc", Duration.ofMinutes(10), LocalDateTime.now(), epic.getId());
        SubTask sub2 = new SubTask("Sub2", "desc", Duration.ofMinutes(10), LocalDateTime.now().plusMinutes(15), epic.getId());

        manager.createSubTask(sub1);
        manager.createSubTask(sub2);

        Epic updatedEpic = manager.getEpic(epic.getId()).orElseThrow();
        assertEquals(TaskStatus.NEW, updatedEpic.getStatus(), "Все подзадачи NEW => Epic должен быть NEW");
    }

    // Новый тест: смешанные NEW и DONE => Epic должен быть IN_PROGRESS
    @Test
    void epicStatusShouldBeInProgressIfMixedNewAndDone() {
        SubTask sub1 = new SubTask("Sub1", "desc", Duration.ofMinutes(10), LocalDateTime.now(), epic.getId());
        SubTask sub2 = new SubTask("Sub2", "desc", Duration.ofMinutes(10), LocalDateTime.now().plusMinutes(15), epic.getId());
        sub1.setStatus(TaskStatus.NEW);
        sub2.setStatus(TaskStatus.DONE);

        manager.createSubTask(sub1);
        manager.createSubTask(sub2);

        Epic updatedEpic = manager.getEpic(epic.getId()).orElseThrow();
        assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getStatus(), "Смешанные NEW и DONE => Epic должен быть IN_PROGRESS");
    }

    // Новый тест: все IN_PROGRESS => Epic должен быть IN_PROGRESS
    @Test
    void epicStatusShouldBeInProgressIfAllSubtasksInProgress() {
        SubTask sub1 = new SubTask("Sub1", "desc", Duration.ofMinutes(10), LocalDateTime.now(), epic.getId());
        SubTask sub2 = new SubTask("Sub2", "desc", Duration.ofMinutes(10), LocalDateTime.now().plusMinutes(15), epic.getId());
        sub1.setStatus(TaskStatus.IN_PROGRESS);
        sub2.setStatus(TaskStatus.IN_PROGRESS);

        manager.createSubTask(sub1);
        manager.createSubTask(sub2);

        Epic updatedEpic = manager.getEpic(epic.getId()).orElseThrow();
        assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getStatus(), "Все IN_PROGRESS => Epic должен быть IN_PROGRESS");
    }

    // Проверка расчёта времени старта, окончания и продолжительности эпика
    @Test
    void shouldCalculateTimeFieldsForEpicCorrectly() {
        manager.deleteAllTasks();
        manager.deleteAllEpics();
        manager.deleteAllSubtasks();
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        SubTask sub1 = new SubTask("Sub 1", "desc", Duration.ofMinutes(10),
                LocalDateTime.of(2025, 5, 3, 10, 0), epic.getId());
        SubTask sub2 = new SubTask("Sub 2", "desc", Duration.ofMinutes(20),
                LocalDateTime.of(2025, 5, 3, 12, 0), epic.getId());

        manager.createSubTask(sub1);
        manager.createSubTask(sub2);

        Epic updatedEpic = manager.getEpic(epic.getId()).orElseThrow();

        assertEquals(Duration.ofMinutes(30), updatedEpic.getDuration(), "Общая продолжительность эпика должна быть 30 минут.");
        assertEquals(LocalDateTime.of(2025, 5, 3, 10, 0), updatedEpic.getStartTime(), "startTime должен быть самым ранним");
        assertEquals(LocalDateTime.of(2025, 5, 3, 12, 20), updatedEpic.getEndTime(), "endTime должен быть самым поздним");
    }

    // Epic без подзадач не должен иметь времени
    @Test
    void epicWithoutSubtasksShouldHaveNoTiming() {
        manager.deleteAllTasks();
        manager.deleteAllEpics();
        manager.deleteAllSubtasks();

        Epic epic = manager.createEpic(new Epic("Empty Epic", "No subs"));
        Epic updatedEpic = manager.getEpic(epic.getId()).orElseThrow();

        assertEquals(Duration.ZERO, updatedEpic.getDuration());
        assertNull(updatedEpic.getStartTime());
        assertNull(updatedEpic.getEndTime());
    }

    @Test
    void epicShouldIgnoreSubtaskWithoutStartTimeInTimingCalculation() {
        manager.deleteAllTasks();
        manager.deleteAllEpics();
        manager.deleteAllSubtasks();

        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));

        SubTask sub1 = new SubTask("Sub with time", "desc",
                Duration.ofMinutes(30),
                LocalDateTime.of(2025, 5, 3, 10, 0),
                epic.getId());

        SubTask sub2 = new SubTask("Sub without time", "desc", epic.getId()); // нет времени

        manager.createSubTask(sub1);
        manager.createSubTask(sub2);

        Epic updatedEpic = manager.getEpic(epic.getId()).orElseThrow();
        assertEquals(Duration.ofMinutes(30), updatedEpic.getDuration(), "Эпик должен учитывать только подзадачи с временем");
        assertEquals(LocalDateTime.of(2025, 5, 3, 10, 0), updatedEpic.getStartTime(), "startTime должен быть только по sub1");
        assertEquals(LocalDateTime.of(2025, 5, 3, 10, 30), updatedEpic.getEndTime(), "endTime должен быть только по sub1");
    }

    @Test
    void shouldAddSubtaskToEpic() {
        Epic epic = manager.createEpic(new Epic("Epic 1", "Description"));
        SubTask subtask = new SubTask("Subtask 1", "Description", Duration.ofMinutes(15),
                LocalDateTime.of(2025, 5, 3, 10, 0), epic.getId());
        SubTask created = manager.createSubTask(subtask);

        Epic updatedEpic = manager.getEpic(epic.getId()).orElseThrow();
        assertTrue(updatedEpic.getSubtaskIds().contains(created.getId()));
    }

    // Проверка, что подзадача не может быть своим собственным эпиком
    @Test
    void testSubtaskCannotBeItsOwnEpic() {

        SubTask subtask = new SubTask("Subtask 1", "Description", Duration.ofMinutes(15), LocalDateTime.of(2025, 5, 2, 10, 0), 1);

        assertThrows(IllegalArgumentException.class, () -> manager.createSubTask(subtask));
    }

    //Проверка, что можно создать подзадачу с правильным статусом
    @Test
    void testCreateSubtaskWithCorrectStatus() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);
        SubTask subtask = new SubTask("Subtask 1", "Description", Duration.ofMinutes(15), LocalDateTime.of(2025, 5, 3, 10, 0), epic.getId());

        SubTask createdSubtask = manager.createSubTask(subtask);
        assertTrue(manager.getSubtask(createdSubtask.getId()).isPresent()); // Подзадача должна быть добавлена в менеджер
        assertEquals(TaskStatus.NEW, manager.getSubtask(createdSubtask.getId()).orElseThrow().getStatus()); // Статус должен быть NEW
    }

    //Проверка, что можно обновить статус подзадачи
    @Test
    void testUpdateSubtaskStatus() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);

        SubTask subtask = new SubTask("Subtask 1", "Description", Duration.ofMinutes(15), LocalDateTime.of(2025, 5, 3, 10, 0), epic.getId());
        SubTask createdSubTask = manager.createSubTask(subtask);

        createdSubTask.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubTask(createdSubTask);

        assertEquals(TaskStatus.IN_PROGRESS, manager.getSubtask(createdSubTask.getId()).orElseThrow().getStatus()); // Статус подзадачи должен быть обновлен на IN_PROGRESS
    }

    //Проверка, что подзадача имеет правильный статус после удаления
    @Test
    void testDeleteSubtask() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);

        SubTask subtask = new SubTask("Subtask 1", "Description", Duration.ofMinutes(15), LocalDateTime.of(2025, 5, 3, 10, 0), epic.getId());
        manager.createSubTask(subtask);

        manager.deleteSubtask(subtask.getId());
        assertTrue(manager.getSubtask(subtask.getId()).isEmpty()); // Подзадача должна быть удалена
    }

    //Проверка, что не может быть создана подзадача с несуществующим эпиком
    @Test
    void testCreateSubtaskWithNonExistentEpic() {
        SubTask subtask = new SubTask("Subtask 1", "Description", Duration.ofMinutes(15), LocalDateTime.of(2025, 5, 2, 10, 0), 99); // Не существует эпика с таким ID

        assertThrows(IllegalArgumentException.class, () -> manager.createSubTask(subtask)); // Должна быть ошибка при попытке создать подзадачу
    }

    //Проверка, что при удалении эпика, все его подзадачи удаляются
    @Test
    void testDeleteEpicRemovesSubtasks() {
        Epic epic = new Epic("Epic 1", "Description");
        manager.createEpic(epic);

        SubTask subtask = new SubTask("Subtask 1", "Description", Duration.ofMinutes(15), LocalDateTime.of(2025, 5, 3, 10, 0), epic.getId());
        manager.createSubTask(subtask);

        manager.deleteEpic(epic.getId());
        assertTrue(manager.getSubtask(subtask.getId()).isEmpty()); // Подзадача должна быть удалена вместе с эпиком
    }

    @Test
    void subtaskWithNullTimingShouldBeHandledGracefully() {
        Epic epic = new Epic("Epic with null-time subtask", "desc");
        manager.createEpic(epic);

        // Подзадача без времени
        SubTask subtask = new SubTask("Subtask without time", "desc", epic.getId());
        manager.createSubTask(subtask);

        // Проверка, что она не влияет на поля времени эпика
        Epic updatedEpic = manager.getEpic(epic.getId()).orElseThrow();

        assertEquals(Duration.ZERO, updatedEpic.getDuration(), "Продолжительность эпика должна быть 0 при отсутствии времени у подзадач");
        assertNull(updatedEpic.getStartTime(), "startTime эпика должен быть null");
        assertNull(updatedEpic.getEndTime(), "endTime эпика должен быть null");

        assertFalse(manager.getPrioritizedTasks().contains(subtask), "Подзадача без времени не должна попадать в приоритезированный список");
    }

}