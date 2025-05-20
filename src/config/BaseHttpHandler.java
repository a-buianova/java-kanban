package config;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {

    // Отправка успешного текста с указанным статусом
    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    // Отправка 404 — не найдено
    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendText(exchange, "{\"error\": \"Not Found\"}", 404);
    }

    // Отправка 500 — ошибка сервера
    protected void sendServerError(HttpExchange exchange) throws IOException {
        sendText(exchange, "{\"error\": \"Internal Server Error\"}", 500);
    }

    // Отправка 406 — конфликт (например, пересечение задач)
    protected void sendConflict(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, message, 409); // 409 — правильный код конфликта
    }

    protected void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, message, 400);
    }

    protected void sendHasIntersections(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, message != null ? message : "Task intersects with another", 406);
    }

    protected void sendMethodNotAllowed(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, message != null ? message : "Method Not Allowed", 405);
    }
}