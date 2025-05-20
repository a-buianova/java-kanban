package task;

import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubTaskTest {
    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void subTasksWithSameFieldsShouldBeEqual() {
        SubTask sub1 = new SubTask("name", "desc", 1);
        SubTask sub2 = new SubTask("name", "desc", 1);
        sub1.setId(42);
        sub2.setId(42);

        assertEquals(sub1, sub2, "SubTask with same ID and fields should be equal");
    }
}