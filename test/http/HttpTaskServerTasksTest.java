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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpTaskServerTasksTest {

    private HttpTaskServer server;
    private TaskManager manager;

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        server.start();
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    @Test
    public void shouldCreateTaskViaPost() throws IOException, InterruptedException {
        Task task = new Task("Test task", "Via POST", Duration.ofMinutes(30), LocalDateTime.now());
        task.setStatus(TaskStatus.NEW);

        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getAllTasks().size());
    }

    @Test
    public void shouldUpdateTaskViaPost() throws IOException, InterruptedException {
        Task task = new Task("Old", "Desc", Duration.ofMinutes(10), LocalDateTime.now());
        task.setStatus(TaskStatus.NEW);
        manager.createTask(task);

        task.setTitle("Updated");
        String json = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("Updated", manager.getTask(task.getId()).orElseThrow().getName());
    }

    @Test
    public void shouldReturnTaskById() throws IOException, InterruptedException {
        Task task = new Task("FindMe", "Desc", Duration.ofMinutes(20), LocalDateTime.now());
        manager.createTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + task.getId()))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task returnedTask = gson.fromJson(response.body(), Task.class);
        assertEquals(task.getTitle(), returnedTask.getTitle());
        assertEquals(task.getDescription(), returnedTask.getDescription());
        assertEquals(task.getStartTime(), returnedTask.getStartTime());
    }

    @Test
    public void shouldReturnAllTasksViaGet() throws IOException, InterruptedException {
        Task task = new Task("Read", "Book", Duration.ofMinutes(45), LocalDateTime.now());
        manager.createTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(1, tasks.length);
        assertEquals("Read", tasks[0].getTitle());
    }

    @Test
    public void shouldDeleteAllTasks() throws IOException, InterruptedException {
        Task task = new Task("ToDelete", "Desc", Duration.ofMinutes(15), LocalDateTime.now());
        manager.createTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .DELETE()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(0, manager.getAllTasks().size());
    }

    @Test
    public void shouldReturnPrioritizedTasks() throws IOException, InterruptedException {
        Task t1 = new Task("One", "1", Duration.ofMinutes(30), LocalDateTime.now());
        Task t2 = new Task("Two", "2", Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));
        manager.createTask(t1);
        manager.createTask(t2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] prioritized = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, prioritized.length);
        assertEquals("One", prioritized[0].getTitle());
        assertEquals("Two", prioritized[1].getTitle());
    }
    @Test
    public void shouldReturnHistory() throws IOException, InterruptedException {
        Task task = new Task("History", "Track", Duration.ofMinutes(30), LocalDateTime.now());
        manager.createTask(task);
        manager.getTask(task.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertEquals(1, history.length);
        assertEquals("History", history[0].getTitle());
    }

    @Test
    public void shouldReturn404ForMissingTask() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void shouldReturn400ForInvalidIdFormat() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/invalid_id"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Should return 400 for malformed ID");
    }

    @Test
    public void shouldReturn409ForInvalidTaskData() throws IOException, InterruptedException {
        String badJson = "{\"title\":null,\"description\":\"Bad\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(badJson))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(409, response.statusCode(), "Should return 409 for invalid input");
    }

    @Test
    public void shouldReturn406ForOverlappingTasks() throws IOException, InterruptedException {
        Task t1 = new Task("T1", "desc", Duration.ofMinutes(30), LocalDateTime.now());
        Task t2 = new Task("T2", "desc", Duration.ofMinutes(30), t1.getStartTime().plusMinutes(15));

        manager.createTask(t1); // первая — OK

        String json = gson.toJson(t2);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(), "Should return 406 for intersecting tasks");
    }

    @Test
    public void shouldReturnEmptyArrayWhenNoTasks() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("[]"), "Should return empty JSON array");
    }
}