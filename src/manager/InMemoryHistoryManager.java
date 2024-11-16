package manager;
import task.Task;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
public class InMemoryHistoryManager implements HistoryManager {
    // Хранение истории (максимум 10 последних задач)
    private final List<Task> history = new LinkedList<>();

    @Override
    public void add(Task task) {
        if (!history.contains(task)) {
            history.addLast(task.clone());
            if (history.size() > 10) {
                history.removeFirst();
            }
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}