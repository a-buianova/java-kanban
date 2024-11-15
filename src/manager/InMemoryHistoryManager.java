package manager;

import task.Task;
import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    // Хранение истории (максимум 10 последних задач)
    private List<Task> history = new ArrayList<>();


    @Override
    public void add(Task task) {
        if (!history.contains(task)) {
            history.add(task);
            if (history.size() > 10) {
                history.remove(0);
            }
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}