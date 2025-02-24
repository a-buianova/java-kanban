package manager;

public class Managers {
    private static final TaskManager TASK_MANAGER = new InMemoryTaskManager();
    private static final HistoryManager HISTORY_MANAGER = new InMemoryHistoryManager();

    private Managers() {
        // Приватный конструктор предотвращает создание экземпляров
    }

    public static TaskManager getDefault() {
        return TASK_MANAGER;
    }

    public static HistoryManager getDefaultHistory() {
        return HISTORY_MANAGER;
    }
}
