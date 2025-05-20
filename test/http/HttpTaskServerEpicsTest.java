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
import task.Epic;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

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

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "POST /epics should return 201");
        assertEquals(1, manager.getAllEpics().size(), "There should be 1 epic");
        assertEquals("EpicTitle", manager.getAllEpics().get(0).getName(), "Epic name should match");
    }

    @Test
    public void shouldReturnAllEpicsViaGet() throws IOException, InterruptedException {
        Epic epic = new Epic("GetEpic", "Desc");
        manager.createEpic(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("GetEpic"));
    }

    @Test
    public void shouldDeleteAllEpics() throws IOException, InterruptedException {
        Epic epic = new Epic("ToDelete", "Desc");
        manager.createEpic(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .DELETE()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(0, manager.getAllEpics().size());
    }

    @Test
    public void shouldReturnEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("ById", "Desc");
        manager.createEpic(epic);
        int id = epic.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + id))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("ById"));
    }

    @Test
    public void shouldUpdateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("OldTitle", "OldDesc");
        manager.createEpic(epic);
        int id = epic.getId();

        epic.setTitle("NewTitle");
        String json = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("NewTitle", manager.getEpic(id).orElseThrow().getName());
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
        Epic epic = new Epic("Nonexistent", "Not exists");
        epic.setId(9999); // несуществующий ID
        String json = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }
}