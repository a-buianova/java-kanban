package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.BaseHttpHandler;
import config.GsonFactory;
import manager.TaskManager;
import task.Epic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson = GsonFactory.createGson();

    public EpicsHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath(); // e.g., /epics or /epics/123
            String[] segments = path.split("/");

            if ("GET".equals(method)) {
                if (segments.length == 3) {
                    // GET /epics/{id}
                    try {
                        int id = Integer.parseInt(segments[2]);
                        var epicOpt = manager.getEpic(id);
                        if (epicOpt.isPresent()) {
                            sendText(exchange, gson.toJson(epicOpt.get()), 200);
                        } else {
                            sendNotFound(exchange);
                        }
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

                if (epic.getId() != 0) {
                    if (manager.getEpic(epic.getId()).isPresent()) {
                        manager.updateEpic(epic);
                        sendText(exchange, "Epic updated", 200);
                    } else {
                        sendNotFound(exchange);
                    }
                } else {
                    manager.createEpic(epic);
                    sendText(exchange, "Epic created", 201);
                }
                return;
            }

            if ("DELETE".equals(method)) {
                manager.deleteAllEpics();
                sendText(exchange, "All epics deleted", 200);
                return;
            }

            sendMethodNotAllowed(exchange, "Only GET, POST, DELETE are supported for /epics");
        } catch (Exception e) {
            e.printStackTrace();
            sendServerError(exchange);
        }
    }
}