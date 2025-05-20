package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import config.BaseHttpHandler;
import config.GsonFactory;
import manager.TaskManager;
import task.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson = GsonFactory.createGson();

    public PrioritizedHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                List<Task> prioritized = manager.getPrioritizedTasks();
                String json = gson.toJson(prioritized);
                sendText(exchange, json, 200);
            } else {
                sendMethodNotAllowed(exchange, "Only GET is supported for /tasks/prioritized");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendServerError(exchange);
        }
    }
}