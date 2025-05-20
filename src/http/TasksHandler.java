package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.BaseHttpHandler;
import config.GsonFactory;
import manager.TaskManager;
import task.Task;
import task.TaskType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson = GsonFactory.createGson();

    public TasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] segments = path.split("/");

            if ("GET".equals(method)) {
                if (segments.length == 3 && "prioritized".equals(segments[2])) {
                    // GET /tasks/prioritized
                    String json = gson.toJson(manager.getPrioritizedTasks());
                    sendText(exchange, json, 200);
                    return;
                } else if (segments.length == 3) {
                    // GET /tasks/{id}
                    try {
                        int id = Integer.parseInt(segments[2]);
                        var optional = manager.getTask(id);
                        if (optional.isPresent()) {
                            String json = gson.toJson(optional.get());
                            sendText(exchange, json, 200);
                        } else {
                            sendNotFound(exchange);
                        }
                    } catch (NumberFormatException e) {
                        sendBadRequest(exchange, "Invalid task ID format");
                    }
                    return;
                }

                // GET /tasks
                List<Task> tasks = manager.getAllTasks();
                String json = gson.toJson(tasks);
                sendText(exchange, json, 200);
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

                if (task.getId() != 0) {
                    if (manager.getTask(task.getId()).isPresent()) {
                        try {
                            manager.updateTask(task);
                            sendText(exchange, "Task updated", 200);
                        } catch (IllegalArgumentException e) {
                            sendHasIntersections(exchange, e.getMessage()); // 406
                        }
                    } else {
                        sendNotFound(exchange);
                    }
                } else {
                    if (task.getName() == null || task.getName().isBlank()
                            || task.getDescription() == null || task.getDescription().isBlank()
                            || task.getStartTime() == null || task.getDuration().toMinutes() < 0) {
                        sendConflict(exchange, "Invalid task data");
                    } else {
                        try {
                            manager.createTask(task);
                            sendText(exchange, "Task created", 201);
                        } catch (IllegalArgumentException e) {
                            sendHasIntersections(exchange, e.getMessage()); // 406
                        }
                    }
                }
                return;
            }

            if ("DELETE".equals(method)) {
                manager.deleteAllTasks();
                sendText(exchange, "All tasks deleted", 200);
                return;
            }

            sendNotFound(exchange);
        } catch (Exception e) {
            e.printStackTrace();
            sendServerError(exchange);
        }
    }
}