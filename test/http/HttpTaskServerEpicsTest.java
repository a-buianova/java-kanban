package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import config.DurationAdapter;
import config.LocalDateTimeAdapter;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpTaskServerEpicsTest {

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
    public void shouldCreateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("EpicTitle", "EpicDescription");
        String json = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(1, manager.getAllEpics().size());
        assertEquals("EpicTitle", manager.getAllEpics().get(0).getName());
    }

    @Test
    public void shouldReturnAllEpicsViaGet() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("GetEpic", "Desc"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        List<Epic> epics = gson.fromJson(response.body(), new TypeToken<List<Epic>>() {}.getType());
        assertEquals(1, epics.size());
        assertEquals("GetEpic", epics.get(0).getName());
    }

    @Test
    public void shouldReturnEpicById() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("ById", "Desc"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epic.getId()))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Epic result = gson.fromJson(response.body(), Epic.class);
        assertEquals(epic.getName(), result.getName());
    }

    @Test
    public void shouldUpdateEpic() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("OldTitle", "OldDesc"));
        epic.setTitle("NewTitle");

        String json = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("NewTitle", manager.getEpic(epic.getId()).orElseThrow().getName());
    }

    @Test
    public void shouldDeleteAllEpics() throws IOException, InterruptedException {
        manager.createEpic(new Epic("ToDelete", "Desc"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .DELETE()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(manager.getAllEpics().isEmpty());
    }

    @Test
    public void shouldReturn409ForInvalidEpicData() throws IOException, InterruptedException {
        String badJson = "{\"title\":\"\",\"description\":\"\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(badJson))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(409, response.statusCode());
    }

    @Test
    public void shouldReturn404WhenUpdatingNonexistentEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Nonexistent", "Desc");
        epic.setId(9999);
        String json = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void shouldReturnSubtasksForEpicById() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Epic with subtasks", "desc"));

        SubTask sub1 = new SubTask("Sub1", "desc1", Duration.ofMinutes(15),
                LocalDateTime.of(2025, 5, 1, 10, 0), epic.getId());
        SubTask sub2 = new SubTask("Sub2", "desc2", Duration.ofMinutes(30),
                LocalDateTime.of(2025, 5, 1, 11, 0), epic.getId());

        manager.createSubTask(sub1);
        manager.createSubTask(sub2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epic.getId() + "/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<SubTask> subtasks = gson.fromJson(response.body(), new TypeToken<List<SubTask>>() {}.getType());
        assertEquals(2, subtasks.size());
    }
}