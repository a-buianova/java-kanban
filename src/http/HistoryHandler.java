package http;

import com.sun.net.httpserver.HttpExchange;
import config.BaseHttpHandler;
import manager.TaskManager;
import task.Task;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler {
    public HistoryHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                List<Task> history = manager.getHistory();
                sendText(exchange, gson.toJson(history), 200);
            } else {
                sendMethodNotAllowed(exchange, "Only GET is allowed for /history");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendServerError(exchange);
        }
    }
}