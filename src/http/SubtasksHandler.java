package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.BaseHttpHandler;
import config.GsonFactory;
import manager.TaskManager;
import task.SubTask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson = GsonFactory.createGson();

    public SubtasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath(); // e.g., /subtasks, /subtasks/{id}, /subtasks/epic

            if ("GET".equals(method)) {
                if ("/subtasks/epic".equals(path)) {
                    // GET /subtasks/epic?id=1
                    String query = exchange.getRequestURI().getQuery(); // id=1
                    if (query != null && query.startsWith("id=")) {
                        try {
                            int epicId = Integer.parseInt(query.substring(3));
                            if (manager.getEpic(epicId).isPresent()) {
                                List<SubTask> subtasks = manager.getSubtasksForEpic(epicId);
                                sendText(exchange, gson.toJson(subtasks), 200);
                            } else {
                                sendNotFound(exchange);
                            }
                        } catch (NumberFormatException e) {
                            sendBadRequest(exchange, "Invalid epic ID");
                        }
                    } else {
                        sendBadRequest(exchange, "Missing epic ID");
                    }
                    return;
                }

                String[] segments = path.split("/");
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
                } else {
                    // GET /subtasks
                    List<SubTask> subtasks = manager.getAllSubTasks();
                    sendText(exchange, gson.toJson(subtasks), 200);
                }
                return;
            }

            if ("POST".equals(method)) {
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                SubTask subtask = gson.fromJson(body, SubTask.class);

                if (subtask.getId() != 0) {
                    if (manager.getSubtask(subtask.getId()).isPresent()) {
                        try {
                            manager.updateSubTask(subtask);
                            sendText(exchange, "Subtask updated", 200);
                        } catch (IllegalArgumentException e) {
                            sendHasIntersections(exchange, e.getMessage()); // 406
                        }
                    } else {
                        sendNotFound(exchange);
                    }
                } else {
                    if (subtask.getName() == null || subtask.getName().isBlank() ||
                            subtask.getDescription() == null || subtask.getDescription().isBlank() ||
                            subtask.getStartTime() == null || subtask.getDuration().toMinutes() < 0 ||
                            subtask.getEpicId() <= 0 || manager.getEpic(subtask.getEpicId()).isEmpty()) {
                        sendConflict(exchange, "Invalid subtask data");
                    } else {
                        try {
                            manager.createSubTask(subtask);
                            sendText(exchange, "Subtask created", 201);
                        } catch (IllegalArgumentException e) {
                            sendHasIntersections(exchange, e.getMessage()); // 406
                        }
                    }
                }
                return;
            }

            if ("DELETE".equals(method)) {
                manager.deleteAllSubtasks();
                sendText(exchange, "All subtasks deleted", 200);
                return;
            }

            sendNotFound(exchange);
        } catch (Exception e) {
            e.printStackTrace();
            sendServerError(exchange);
        }
    }
}