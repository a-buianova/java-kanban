package config;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {

    protected final TaskManager manager;
    protected final Gson gson;

    public BaseHttpHandler(TaskManager manager) {
        this.manager = manager;
        this.gson = GsonFactory.createGson();
    }

    // Отправка успешного ответа с текстом и статусом
    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    // Ошибки и статусы
    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendText(exchange, "{\"error\": \"Not Found\"}", 404);
    }

    protected void sendServerError(HttpExchange exchange) throws IOException {
        sendText(exchange, "{\"error\": \"Internal Server Error\"}", 500);
    }

    protected void sendConflict(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, message != null ? message : "Conflict", 409);
    }

    protected void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, message != null ? message : "Bad Request", 400);
    }

    protected void sendHasIntersections(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, message != null ? message : "Task intersects with another", 406);
    }

    protected void sendMethodNotAllowed(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, message != null ? message : "Method Not Allowed", 405);
    }
}