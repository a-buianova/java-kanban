import java.util.*;
import java.util.stream.Collectors;

public class TaskManager {
    private Map<Integer, Task> tasks = new HashMap<>();
    private Map<Integer, Epic> epics = new HashMap<>();
    private Map<Integer, SubTask> subtasks = new HashMap<>();
    private int idCounter = 0;

    public Task createTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            throw new IllegalArgumentException("Задача с таким ID уже существует.");
        }
        task.setId(++idCounter);
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic createEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            throw new IllegalArgumentException("Эпик с таким ID уже существует.");
        }
        epic.setId(++idCounter);
        epics.put(epic.getId(), epic);
        return epic;
    }

    public SubTask createSubTask(SubTask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            throw new IllegalArgumentException("Подзадача с таким ID уже существует.");
        }
        if (!epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Эпик с ID " + subtask.getEpicId() + " не найден.");
        }
        subtask.setId(++idCounter);
        subtasks.put(subtask.getId(), subtask);
        epics.get(subtask.getEpicId()).addSubtask(subtask);
        return subtask;
    }

    public Task getTask(int id) {
        return tasks.get(id);
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public SubTask getSubtask(int id) {
        return subtasks.get(id);
    }

    public void deleteTask(int id) {
        tasks.remove(id);
    }

    public void deleteEpic(int id) {
        epics.remove(id);
    }

    public void deleteSubtask(int id) {
        subtasks.remove(id);
    }

    public List<SubTask> getSubtasksForEpic(int epicId) {
        return new ArrayList<>(epics.get(epicId).getSubtasks());
    }

    public List<Task> getTasksSortedByStatus() {
        return tasks.values().stream()
                .sorted(Comparator.comparing(Task::getStatus))
                .collect(Collectors.toList());
    }

    public List<SubTask> getSubtasksSortedByStatus(int epicId) {
        return epics.get(epicId).getSubtasks().stream()
                .sorted(Comparator.comparing(SubTask::getStatus))
                .collect(Collectors.toList());
    }
}