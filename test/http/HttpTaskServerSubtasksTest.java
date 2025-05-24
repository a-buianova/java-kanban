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
import task.SubTask;
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

public class HttpTaskServerSubtasksTest {

    private HttpTaskServer server;
    private TaskManager manager;

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    private Epic epic;

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        epic = manager.createEpic(new Epic("Epic for Subtasks", "Test"));

        server = new HttpTaskServer(manager);
        server.start();
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    @Test
    public void shouldCreateSubtask() throws IOException, InterruptedException {
        SubTask sub = new SubTask("SubTitle", "SubDesc", Duration.ofMinutes(20), LocalDateTime.now(), epic.getId());
        sub.setStatus(TaskStatus.NEW);
        String json = gson.toJson(sub);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + manager.getAllSubTasks().get(0).getId()))
                .GET()
                .build();

        HttpResponse<String> getResponse = HttpClient.newHttpClient()
                .send(getRequest, HttpResponse.BodyHandlers.ofString());

        SubTask created = gson.fromJson(getResponse.body(), SubTask.class);
        assertEquals("SubTitle", created.getName());
    }

    @Test
    public void shouldReturnAllSubtasksViaGet() throws IOException, InterruptedException {
        SubTask sub = new SubTask("ViewSub", "Desc", Duration.ofMinutes(10), LocalDateTime.now(), epic.getId());
        manager.createSubTask(sub);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<SubTask>>() {}.getType();
        List<SubTask> subtasks = gson.fromJson(response.body(), listType);
        assertEquals(1, subtasks.size());
        assertEquals("ViewSub", subtasks.get(0).getName());
    }

    @Test
    public void shouldReturnSubtaskById() throws IOException, InterruptedException {
        SubTask sub = new SubTask("FindMe", "Desc", Duration.ofMinutes(15), LocalDateTime.now(), epic.getId());
        manager.createSubTask(sub);
        int id = sub.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + id))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        SubTask found = gson.fromJson(response.body(), SubTask.class);
        assertEquals("FindMe", found.getName());
    }

    @Test
    public void shouldUpdateSubtask() throws IOException, InterruptedException {
        SubTask sub = new SubTask("OldTitle", "Desc", Duration.ofMinutes(15), LocalDateTime.now(), epic.getId());
        manager.createSubTask(sub);
        sub.setTitle("UpdatedTitle");
        String json = gson.toJson(sub);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("UpdatedTitle", manager.getSubtask(sub.getId()).orElseThrow().getName());
    }

    @Test
    public void shouldDeleteAllSubtasks() throws IOException, InterruptedException {
        SubTask sub = new SubTask("DeleteMe", "Desc", Duration.ofMinutes(10), LocalDateTime.now(), epic.getId());
        manager.createSubTask(sub);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .DELETE()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(0, manager.getAllSubTasks().size());
    }

    @Test
    public void shouldGetSubtasksOfEpic() throws IOException, InterruptedException {
        Epic epic = manager.createEpic(new Epic("Epic", "desc"));

        SubTask sub1 = new SubTask("Sub1", "desc", Duration.ofMinutes(15), LocalDateTime.now(), epic.getId());
        SubTask sub2 = new SubTask("Sub2", "desc", Duration.ofMinutes(20), LocalDateTime.now().plusMinutes(30), epic.getId());
        manager.createSubTask(sub1);
        manager.createSubTask(sub2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + epic.getId() + "/subtasks")) // исправлено
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<SubTask>>() {}.getType();
        List<SubTask> subtasks = gson.fromJson(response.body(), listType);
        assertEquals(2, subtasks.size());
        assertTrue(subtasks.stream().anyMatch(s -> s.getName().equals("Sub1")));
        assertTrue(subtasks.stream().anyMatch(s -> s.getName().equals("Sub2")));
    }

    @Test
    public void shouldDeleteSubtaskById() throws IOException, InterruptedException {
        SubTask sub = new SubTask("ToDelete", "Desc", Duration.ofMinutes(10), LocalDateTime.now(), epic.getId());
        manager.createSubTask(sub);
        int id = sub.getId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + id))
                .DELETE()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertFalse(manager.getSubtask(id).isPresent(), "Subtask should be deleted");
    }

    @Test
    public void shouldReturn404ForNonExistentSubtask() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/9999"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void shouldReturn409ForInvalidSubtask() throws IOException, InterruptedException {
        String badJson = "{\"title\":null,\"description\":\"Missing fields\",\"epicId\":" + epic.getId() + "}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(badJson))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(409, response.statusCode(), "Should return 409 for invalid input");
    }

    @Test
    public void shouldReturn400ForInvalidEpicIdInQuery() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/epic?id=abc"))
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Should return 400 for invalid epic ID");
    }

    @Test
    public void shouldReturnEmptyListForEpicWithNoSubtasks() throws IOException, InterruptedException {
        Epic emptyEpic = manager.createEpic(new Epic("NoSubtasks", "Empty"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/" + emptyEpic.getId() + "/subtasks")) // исправлено
                .GET()
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("[]"), "Should return empty list for epic with no subtasks");
    }

    @Test
    public void shouldReturn406OnIntersection() throws IOException, InterruptedException {
        SubTask sub1 = new SubTask("S1", "desc", Duration.ofMinutes(30), LocalDateTime.now(), epic.getId());
        manager.createSubTask(sub1);

        SubTask sub2 = new SubTask("S2", "desc", Duration.ofMinutes(30), sub1.getStartTime().plusMinutes(15), epic.getId());
        sub2.setStatus(TaskStatus.NEW);
        String json = gson.toJson(sub2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(), "Should return 406 for intersecting subtask times");
    }
}