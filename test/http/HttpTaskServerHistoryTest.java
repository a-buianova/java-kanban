package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import config.DurationAdapter;
import config.LocalDateTimeAdapter;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.*;
import task.Epic;
import task.SubTask;
import task.Task;
import task.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerHistoryTest {

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
    public void shouldReturnEmptyHistoryInitially() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().equals("[]") || response.body().isBlank(), "Initial history should be empty");
    }

    @Test
    public void shouldReturnHistoryAfterTaskViews() throws IOException, InterruptedException {
        Task task1 = new Task("Task1", "Desc1", Duration.ofMinutes(10), LocalDateTime.now());
        Task task2 = new Task("Task2", "Desc2", Duration.ofMinutes(15), LocalDateTime.now().plusMinutes(20));
        manager.createTask(task1);
        manager.createTask(task2);

        // Access tasks to add to history
        manager.getTask(task1.getId());
        manager.getTask(task2.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Task1"));
        assertTrue(response.body().contains("Task2"));
    }

    @Test
    public void shouldPreserveHistoryOrder() throws IOException, InterruptedException {
        Task t1 = new Task("First", "1", Duration.ofMinutes(10), LocalDateTime.now());
        Task t2 = new Task("Second", "2", Duration.ofMinutes(10), LocalDateTime.now().plusMinutes(15));
        Task t3 = new Task("Third", "3", Duration.ofMinutes(10), LocalDateTime.now().plusMinutes(30));

        manager.createTask(t1);
        manager.createTask(t2);
        manager.createTask(t3);

        manager.getTask(t2.getId());
        manager.getTask(t1.getId());
        manager.getTask(t3.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        String json = response.body();
        int i1 = json.indexOf("Second");
        int i2 = json.indexOf("First");
        int i3 = json.indexOf("Third");

        assertTrue(i1 < i2 && i2 < i3, "Tasks should be ordered as viewed");
    }

    @Test
    public void shouldNotDuplicateInHistory() throws IOException, InterruptedException {
        Task task = new Task("Unique", "NoDup", Duration.ofMinutes(5), LocalDateTime.now());
        manager.createTask(task);

        manager.getTask(task.getId());
        manager.getTask(task.getId());
        manager.getTask(task.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        String json = response.body();
        int first = json.indexOf("Unique");
        int last = json.lastIndexOf("Unique");

        assertEquals(first, last, "History should not contain duplicates");
    }

    @Test
    public void shouldReturnEmptyArrayNotNull() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNotNull(response.body());
        assertTrue(response.body().trim().startsWith("[") && response.body().trim().endsWith("]"), "Should return JSON array");
    }

    @Test
    public void shouldIncludeAllTaskTypesInHistory() throws IOException, InterruptedException {
        // Epic и SubTask требуют связи
        Epic epic = new Epic("EpicHistory", "desc");
        manager.createEpic(epic);

        SubTask sub = new SubTask("SubHistory", "desc", Duration.ofMinutes(10), LocalDateTime.now().plusHours(1), epic.getId());
        manager.createSubTask(sub);

        Task task = new Task("TaskHistory", "desc", Duration.ofMinutes(10), LocalDateTime.now());
        manager.createTask(task);

        // Просмотры
        manager.getTask(task.getId());
        manager.getSubtask(sub.getId());
        manager.getEpic(epic.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String body = response.body();

        assertTrue(body.contains("TaskHistory"), "History should contain Task");
        assertTrue(body.contains("SubHistory"), "History should contain SubTask");
        assertTrue(body.contains("EpicHistory"), "History should contain Epic");
    }

    @Test
    public void shouldRemoveDeletedItemsFromHistory() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("EpicToDelete", "desc"));
        SubTask sub = manager.createSubTask(new SubTask("SubToDelete", "desc", Duration.ofMinutes(15), LocalDateTime.now().plusMinutes(30), epic.getId()));
        Task task = manager.createTask(new Task("TaskToDelete", "desc", Duration.ofMinutes(10), LocalDateTime.now()));

        manager.getTask(task.getId());
        manager.getSubtask(sub.getId());
        manager.getEpic(epic.getId());

        manager.deleteTask(task.getId());
        manager.deleteSubtask(sub.getId());
        manager.deleteEpic(epic.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String body = response.body();

        assertFalse(body.contains("TaskToDelete"), "Task should be removed from history after deletion");
        assertFalse(body.contains("SubToDelete"), "SubTask should be removed from history after deletion");
        assertFalse(body.contains("EpicToDelete"), "Epic should be removed from history after deletion");
    }
}