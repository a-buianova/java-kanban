package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import config.BaseHttpHandler;
import config.GsonFactory;
import manager.TaskManager;
import task.SubTask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubtasksHandler extends BaseHttpHandler {
    private final Gson gson = GsonFactory.createGson();

    public SubtasksHandler(TaskManager manager) {
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
                    // GET /subtasks/{id}
                    try {
                        int id = Integer.parseInt(segments[2]);
                        var optional = manager.getSubtask(id);
                        if (optional.isPresent()) {
                            sendText(exchange, gson.toJson(optional.get()), 200);
                        } else {
                            sendNotFound(exchange);
                        }
                    } catch (NumberFormatException e) {
                        sendBadRequest(exchange, "Invalid subtask ID");
                    }
                    return;
                }

                // GET /subtasks
                List<SubTask> subtasks = manager.getAllSubTasks();
                sendText(exchange, gson.toJson(subtasks), 200);
                return;
            }

            if ("POST".equals(method)) {
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                SubTask subtask = gson.fromJson(body, SubTask.class);

                if (subtask.getName() == null || subtask.getName().isBlank()
                        || subtask.getDescription() == null || subtask.getDescription().isBlank()
                        || subtask.getStartTime() == null || subtask.getDuration() == null
                        || subtask.getDuration().isNegative() || subtask.getEpicId() <= 0) {
                    sendConflict(exchange, "Invalid subtask data");
                    return;
                }

                try {
                    if (subtask.getId() != 0) {
                        if (manager.containsSubtask(subtask.getId())) {
                            manager.updateSubTask(subtask);
                            sendText(exchange, "Subtask updated", 200);
                        } else {
                            sendNotFound(exchange);
                        }
                    } else {
                        manager.createSubTask(subtask);
                        sendText(exchange, "Subtask created", 201);
                    }
                } catch (IllegalArgumentException e) {
                    String msg = e.getMessage();
                    if (msg != null && msg.contains("пересекается")) {
                        sendHasIntersections(exchange, msg);
                    } else {
                        sendConflict(exchange, msg);
                    }
                }
                return;
            }

            if ("DELETE".equals(method)) {
                if (segments.length == 3) {
                    // DELETE /subtasks/{id}
                    try {
                        int id = Integer.parseInt(segments[2]);
                        if (manager.containsSubtask(id)) {
                            manager.deleteSubtask(id);
                            sendText(exchange, "Subtask deleted", 200);
                        } else {
                            sendNotFound(exchange);
                        }
                    } catch (NumberFormatException e) {
                        sendBadRequest(exchange, "Invalid subtask ID");
                    }
                } else {
                    // DELETE /subtasks
                    manager.deleteAllSubtasks();
                    sendText(exchange, "All subtasks deleted", 200);
                }
                return;
            }

            sendMethodNotAllowed(exchange, "Only GET, POST, DELETE are supported for /subtasks");
        } catch (Exception e) {
            e.printStackTrace();
            sendServerError(exchange);
        }
    }
}