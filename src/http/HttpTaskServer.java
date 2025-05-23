package http;

import com.sun.net.httpserver.HttpServer;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private final HttpServer server;
    private final TaskManager manager;

    public HttpTaskServer(TaskManager manager) throws IOException {
        this.manager = manager;
        server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/tasks", new TasksHandler(manager));
        server.createContext("/epics",
                new EpicsHandler(manager));
        server.createContext("/subtasks", new SubtasksHandler(manager));
        server.createContext("/history", new HistoryHandler(manager));
        server.createContext("/prioritized", new PrioritizedHandler(manager));
    }

    public void start() {
        System.out.println("HTTP-сервер запущен на порту 8080");
        server.start();
    }

    public void stop() {
        server.stop(0);
        System.out.println("HTTP-сервер остановлен");
    }

    public static void main(String[] args) throws IOException {
        TaskManager manager = Managers.getDefault(); // InMemoryTaskManager
        HttpTaskServer server = new HttpTaskServer(manager);
        server.start();
    }
}