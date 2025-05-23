package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import config.DurationAdapter;
import config.LocalDateTimeAdapter;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Task;
import task.TaskStatus;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpTaskServerPrioritizedTest {

    private HttpTaskServer server;
    private TaskManager manager;

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    @BeforeEach
    public void startServer() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        server.start();
    }

    @AfterEach
    public void stopServer() {
        server.stop();
    }

    @Test
    public void shouldReturnTasksInPriorityOrder() throws IOException, InterruptedException {
        Task early = new Task("Early", "desc", Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 21, 9, 0));
        Task middle = new Task("Middle", "desc", Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 21, 11, 0));
        Task late = new Task("Late", "desc", Duration.ofMinutes(30), LocalDateTime.of(2025, 5, 21, 13, 0));

        manager.createTask(middle);
        manager.createTask(late);
        manager.createTask(early);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Type taskListType = new com.google.gson.reflect.TypeToken<List<Task>>() {}.getType();
        List<Task> tasks = gson.fromJson(response.body(), taskListType);

        assertEquals(3, tasks.size());
        assertEquals("Early", tasks.get(0).getName());
        assertEquals("Middle", tasks.get(1).getName());
        assertEquals("Late", tasks.get(2).getName());
    }

    @Test
    public void shouldReturnEmptyListWhenNoTasks() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("[]"), "Empty list expected");
    }

    @Test
    public void shouldHandleTasksWithNullStartTime() throws IOException, InterruptedException {
        Task t1 = new Task("T1", "Has time", Duration.ofMinutes(20), LocalDateTime.now().plusHours(1));
        Task t2 = new Task("T2", "No time", Duration.ofMinutes(10), null); // ✔ duration есть, startTime = null
        t1.setStatus(TaskStatus.NEW);
        t2.setStatus(TaskStatus.NEW);

        manager.createTask(t1);
        manager.createTask(t2);

        List<Task> prioritized = manager.getPrioritizedTasks();

        assertEquals(2, prioritized.size(), "Should contain both tasks");
        assertTrue(prioritized.contains(t1));
        assertTrue(prioritized.contains(t2));

        // задача без времени — в конце списка
        assertEquals(t2.getId(), prioritized.get(prioritized.size() - 1).getId());
    }
}