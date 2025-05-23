package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import config.BaseHttpHandler;
import config.GsonFactory;
import manager.TaskManager;
import task.Epic;
import task.SubTask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler {
    private final Gson gson = GsonFactory.createGson();

    public EpicsHandler(TaskManager manager) { // ✅ без указания пакета
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] segments = path.split("/");

            if ("GET".equals(method)) {
                if (segments.length == 3) {
                    // GET /epics/{id}
                    try {
                        int id = Integer.parseInt(segments[2]);
                        manager.getEpic(id)
                                .map(epic -> {
                                    try {
                                        sendText(exchange, gson.toJson(epic), 200);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    return null;
                                })
                                .orElseGet(() -> {
                                    try {
                                        sendNotFound(exchange);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    return null;
                                });
                    } catch (NumberFormatException e) {
                        sendBadRequest(exchange, "Invalid epic ID");
                    }
                    return;
                } else if (segments.length == 4 && "subtasks".equals(segments[3])) {
                    // GET /epics/{id}/subtasks
                    try {
                        int epicId = Integer.parseInt(segments[2]);
                        List<SubTask> subtasks = manager.getSubtasksForEpic(epicId);
                        sendText(exchange, gson.toJson(subtasks), 200);
                    } catch (NumberFormatException e) {
                        sendBadRequest(exchange, "Invalid epic ID");
                    }
                    return;
                }

                // GET /epics
                List<Epic> epics = manager.getAllEpics();
                sendText(exchange, gson.toJson(epics), 200);
                return;
            }

            if ("POST".equals(method)) {
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                Epic epic = gson.fromJson(body, Epic.class);

                if (epic.getName() == null || epic.getName().isBlank() ||
                        epic.getDescription() == null || epic.getDescription().isBlank()) {
                    sendConflict(exchange, "Invalid epic data");
                    return;
                }

                try {
                    if (epic.getId() != 0) {
                        manager.updateEpic(epic);
                        sendText(exchange, "Epic updated", 200);
                    } else {
                        manager.createEpic(epic);
                        sendText(exchange, "Epic created", 201);
                    }
                } catch (IllegalArgumentException e) {
                    sendNotFound(exchange);
                }
                return;
            }

            if ("DELETE".equals(method)) {
                if (segments.length == 3) {
                    // DELETE /epics/{id}
                    try {
                        int id = Integer.parseInt(segments[2]);
                        manager.deleteEpic(id);
                        sendText(exchange, "Epic deleted", 200);
                    } catch (NumberFormatException e) {
                        sendBadRequest(exchange, "Invalid epic ID");
                    }
                } else {
                    // DELETE /epics
                    manager.deleteAllEpics();
                    sendText(exchange, "All epics deleted", 200);
                }
                return;
            }

            sendMethodNotAllowed(exchange, "Only GET, POST, DELETE are supported for /epics");
        } catch (Exception e) {
            e.printStackTrace();
            sendServerError(exchange);
        }
    }
}