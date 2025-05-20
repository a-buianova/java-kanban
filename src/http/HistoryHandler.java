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

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson = GsonFactory.createGson();

    public HistoryHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();

            if ("GET".equals(method)) {
                List<Task> history = manager.getHistory();
                String json = gson.toJson(history);
                sendText(exchange, json, 200);
            } else {
                sendMethodNotAllowed(exchange, "Only GET is allowed for /history");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendServerError(exchange);
        }
    }
}