package task;

import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

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

    @Test
    void epicsWithSameFieldsShouldBeEqual() {
        Epic epic1 = new Epic("Epic Title", "Epic Description");
        Epic epic2 = new Epic("Epic Title", "Epic Description");

        epic1.setId(1);
        epic2.setId(1);

        assertEquals(epic1, epic2, "Epics with same ID and fields should be equal");
    }

    @Test
    void epicStartTimeShouldBeNullByDefault() {
        Epic epic = new Epic("Epic", "desc");
        assertNull(epic.getStartTime());
    }

    @Test
    void epicDurationShouldBeZeroByDefault() {
        Epic epic = new Epic("Epic", "desc");
        assertEquals(Duration.ZERO, epic.getDuration());
    }

    @Test
    void epicTypeShouldBeEpic() {
        Epic epic = new Epic("Epic", "desc");
        assertEquals(TaskType.EPIC, epic.getType());
    }
}