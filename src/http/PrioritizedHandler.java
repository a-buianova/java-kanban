package http;

import com.sun.net.httpserver.HttpExchange;
import config.BaseHttpHandler;
import task.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler {
    public PrioritizedHandler(manager.TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                List<Task> prioritized = manager.getPrioritizedTasks();
                sendText(exchange, gson.toJson(prioritized), 200);
            } else {
                sendMethodNotAllowed(exchange, "Only GET is supported for /tasks/prioritized");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendServerError(exchange);
        }
    }
}