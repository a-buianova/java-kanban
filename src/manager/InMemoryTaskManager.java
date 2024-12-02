package manager;

import task.*;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, SubTask> subtasks = new HashMap<>();
    private int idCounter = 0;

    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private void validateUniqueId(int id) {
        if (tasks.containsKey(id) || epics.containsKey(id) || subtasks.containsKey(id)) {
            throw new IllegalArgumentException("Задача с ID " + id + " уже существует.");
        }
    }

    @Override
    public Task createTask(Task task) {
        validateUniqueId(task.getId());
        task.setId(++idCounter);
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        validateUniqueId(epic.getId());
        epic.setId(++idCounter);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public SubTask createSubTask(SubTask subtask) {
        validateUniqueId(subtask.getId());

        if (subtask.getStatus() == null) {
            throw new IllegalArgumentException("Статус подзадачи не может быть null.");
        }

        if (!epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Эпик с ID " + subtask.getEpicId() + " не найден.");
        }

        subtask.setId(++idCounter);
        subtasks.put(subtask.getId(), subtask);
        epics.get(subtask.getEpicId()).addSubtask(subtask);

        updateEpicStatus(subtask.getEpicId());
        return subtask;
    }

    @Override
    public SubTask updateSubTask(SubTask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            throw new IllegalArgumentException("Подзадача с ID " + subtask.getId() + " не найдена.");
        }

        subtasks.put(subtask.getId(), subtask);
        epics.get(subtask.getEpicId()).addSubtask(subtask);
        updateEpicStatus(subtask.getEpicId());
        return subtask;
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public SubTask getSubtask(int id) {
        SubTask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public void deleteTask(int id) {
        if (tasks.containsKey(id)) {
            historyManager.remove(id);
            tasks.remove(id);
        }
    }

    @Override
    public void deleteEpic(int id) {
        if (epics.containsKey(id)) {
            Epic epic = epics.get(id);
            for (SubTask subtask : epic.getSubtasks()) {
                historyManager.remove(subtask.getId());
                subtasks.remove(subtask.getId());
            }
            historyManager.remove(id);
            epics.remove(id);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        if (subtasks.containsKey(id)) {
            SubTask subtask = subtasks.get(id);
            subtasks.remove(id);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtasks().remove(subtask);
                updateEpicStatus(epic.getId());
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void removeEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            for (SubTask subtask : epic.getSubtasks()) {
                historyManager.remove(subtask.getId());
                subtasks.remove(subtask.getId());
            }
            historyManager.remove(id);
            epics.remove(id);
        }
    }

    @Override
    public List<SubTask> getSubtasksForEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            return new ArrayList<>(epic.getSubtasks());
        }
        return List.of();
    }

    @Override
    public List<Task> getTasksSortedByStatus() {
        return List.of();
    }

    @Override
    public List<SubTask> getSubtasksSortedByStatus(int epicId) {
        return List.of();
    }

    @Override
    public List<Task> getAllTasks() {
        return List.of();
    }

    @Override
    public List<Epic> getAllEpics() {
        return List.of();
    }

    @Override
    public void deleteAllTasks() {
        for (Task task : tasks.values()) {
            historyManager.remove(task.getId());
        }
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            for (SubTask subtask : epic.getSubtasks()) {
                historyManager.remove(subtask.getId());
                subtasks.remove(subtask.getId());
            }
            historyManager.remove(epic.getId());
        }
        epics.clear(); //
    }

    @Override
    public void deleteAllSubtasks() {
        for (SubTask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
        }
        subtasks.clear();
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);

        if (epic == null) {
            throw new IllegalArgumentException("Эпик с ID " + epicId + " не найден.");
        }

        List<SubTask> subtasks = epic.getSubtasks();

        if (subtasks.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        boolean allDone = true;
        boolean allNew = true;

        for (SubTask subtask : subtasks) {
            if (subtask.getStatus() == null) {
                throw new IllegalStateException("У подзадачи ID " + subtask.getId() + " отсутствует статус.");
            }

            if (subtask.getStatus() != TaskStatus.DONE) {
                allDone = false;
            }
            if (subtask.getStatus() != TaskStatus.NEW) {
                allNew = false;
            }
        }

        if (allDone) {
            epic.setStatus(TaskStatus.DONE);
        } else if (allNew) {
            epic.setStatus(TaskStatus.NEW);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}