package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import config.BaseHttpHandler;
import config.GsonFactory;
import exception.TaskIntersectionException;
import manager.TaskManager;
import task.Task;
import task.TaskType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TasksHandler extends BaseHttpHandler {
    private final Gson gson = GsonFactory.createGson();

    public TasksHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] segments = path.split("/");

            if ("GET".equals(method)) {
                if (segments.length == 3 && "prioritized".equals(segments[2])) {
                    sendText(exchange, gson.toJson(manager.getPrioritizedTasks()), 200);
                    return;
                } else if (segments.length == 3) {
                    try {
                        int id = Integer.parseInt(segments[2]);
                        manager.getTask(id)
                                .map(task -> {
                                    sendSafely(exchange, gson.toJson(task), 200);
                                    return null;
                                })
                                .orElseGet(() -> {
                                    sendSafelyNotFound(exchange);
                                    return null;
                                });
                    } catch (NumberFormatException e) {
                        sendBadRequest(exchange, "Invalid task ID format");
                    }
                    return;
                }

                sendText(exchange, gson.toJson(manager.getAllTasks()), 200);
                return;
            }

            if ("POST".equals(method)) {
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                Task task = gson.fromJson(body, Task.class);

                if (task.getType() != TaskType.TASK) {
                    sendConflict(exchange, "Invalid task type: expected TASK");
                    return;
                }

                try {
                    if (task.getId() != 0) {
                        manager.updateTask(task);
                        sendText(exchange, "Task updated", 200);
                    } else {
                        manager.createTask(task);
                        sendText(exchange, "Task created", 201);
                    }
                } catch (TaskIntersectionException e) {
                    sendHasIntersections(exchange, e.getMessage());
                } catch (IllegalArgumentException e) {
                    sendConflict(exchange, e.getMessage());
                }
                return;
            }

            if ("DELETE".equals(method)) {
                if (segments.length == 3) {
                    try {
                        int id = Integer.parseInt(segments[2]);
                        manager.deleteTask(id);
                        sendText(exchange, "Task deleted", 200);
                    } catch (NumberFormatException e) {
                        sendBadRequest(exchange, "Invalid task ID");
                    }
                } else {
                    manager.deleteAllTasks();
                    sendText(exchange, "All tasks deleted", 200);
                }
                return;
            }

            sendMethodNotAllowed(exchange, "Only GET, POST, DELETE are supported for /tasks");
        } catch (Exception e) {
            e.printStackTrace();
            sendServerError(exchange);
        }
    }

    private void sendSafely(HttpExchange exchange, String text, int code) {
        try {
            sendText(exchange, text, code);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendSafelyNotFound(HttpExchange exchange) {
        try {
            sendNotFound(exchange);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}