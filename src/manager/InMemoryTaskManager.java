package manager;

import exception.TaskIntersectionException;
import task.Epic;
import task.SubTask;
import task.Task;
import task.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {

    private final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator
                    .comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(Task::getId)
    );

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, SubTask> subtasks = new HashMap<>();
    protected int nextId = 1;

    protected final HistoryManager historyManager = Managers.getDefaultHistory();

    private void validateUniqueId(int id) {
        if (tasks.containsKey(id) || epics.containsKey(id) || subtasks.containsKey(id)) {
            throw new IllegalArgumentException("Задача с ID " + id + " уже существует.");
        }
    }

    @Override
    public Task createTask(Task task) {
        validateTaskData(task);
        task.setId(nextId++);
        if (hasIntersections(task)) {
            throw new TaskIntersectionException("Задача пересекается по времени с другой задачей.");
        }
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(nextId++);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public SubTask createSubTask(SubTask subtask) {
        subtask.setId(nextId++);
        if (subtask.getStatus() == null) {
            throw new IllegalArgumentException("Статус подзадачи не может быть null.");
        }
        if (!epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Эпик с ID " + subtask.getEpicId() + " не найден.");
        }
        if (hasIntersections(subtask)) {
            throw new TaskIntersectionException("Подзадача пересекается по времени с другой задачей.");
        }
        subtasks.put(subtask.getId(), subtask);
        prioritizedTasks.add(subtask);
        epics.get(subtask.getEpicId()).getSubtaskIds().add(subtask.getId());
        updateEpicStatus(subtask.getEpicId());
        return subtask;
    }

    @Override
    public SubTask updateSubTask(SubTask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            throw new IllegalArgumentException("Подзадача с ID " + subtask.getId() + " не найдена.");
        }
        if (hasIntersections(subtask)) {
            throw new TaskIntersectionException("Подзадача пересекается по времени с другой задачей.");
        }
        subtasks.put(subtask.getId(), subtask);
        prioritizedTasks.removeIf(t -> t.getId() == subtask.getId());
        prioritizedTasks.add(subtask);
        updateEpicStatus(subtask.getEpicId());
        return subtask;
    }

    @Override
    public Task updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            throw new IllegalArgumentException("Задача с ID " + task.getId() + " не найдена.");
        }
        validateTaskData(task);
        if (hasIntersections(task)) {
            throw new TaskIntersectionException("Задача пересекается по времени с другой задачей.");
        }
        tasks.put(task.getId(), task);
        prioritizedTasks.removeIf(t -> t.getId() == task.getId());
        prioritizedTasks.add(task);
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            throw new IllegalArgumentException("Эпик с ID " + epic.getId() + " не найден.");
        }
        epics.put(epic.getId(), epic);
        updateEpicStatus(epic.getId());
        return epic;
    }

    private void validateTaskData(Task task) {
        if (task == null || task.getName() == null || task.getName().isBlank()
                || task.getDescription() == null || task.getDescription().isBlank()
                || task.getDuration() == null || task.getDuration().isNegative()) {
            throw new IllegalArgumentException("Некорректные данные задачи.");
        }
    }

    private boolean isOverlapping(Task t1, Task t2) {
        LocalDateTime start1 = t1.getStartTime();
        LocalDateTime end1 = t1.getEndTime();
        LocalDateTime start2 = t2.getStartTime();
        LocalDateTime end2 = t2.getEndTime();

        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }

        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private boolean hasIntersections(Task task) {
        return prioritizedTasks.stream()
                .filter(t -> t.getId() != task.getId())
                .anyMatch(t -> isOverlapping(t, task));
    }

    @Override
    public Optional<Task> getTask(int id) {
        Optional<Task> task = Optional.ofNullable(tasks.get(id));
        task.ifPresent(historyManager::add);
        return task;
    }

    @Override
    public Optional<Epic> getEpic(int id) {
        Optional<Epic> epic = Optional.ofNullable(epics.get(id));
        epic.ifPresent(historyManager::add);
        return epic;
    }

    @Override
    public Optional<SubTask> getSubtask(int id) {
        Optional<SubTask> subtask = Optional.ofNullable(subtasks.get(id));
        subtask.ifPresent(historyManager::add);
        return subtask;
    }

    @Override
    public void deleteTask(int id) {
        if (tasks.containsKey(id)) {
            historyManager.remove(id);
            prioritizedTasks.removeIf(t -> t.getId() == id);
            tasks.remove(id);
        }
    }

    @Override
    public void deleteEpic(int id) {
        if (epics.containsKey(id)) {
            getSubtasksForEpic(id).forEach(sub -> {
                historyManager.remove(sub.getId());
                subtasks.remove(sub.getId());
                prioritizedTasks.removeIf(t -> t.getId() == sub.getId());
            });
            historyManager.remove(id);
            epics.remove(id);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        if (subtasks.containsKey(id)) {
            SubTask subtask = subtasks.get(id);
            prioritizedTasks.removeIf(t -> t.getId() == id);
            subtasks.remove(id);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                updateEpicStatus(epic.getId());
            }
            historyManager.remove(id);
        }
    }

    @Override
    public List<SubTask> getSubtasksForEpic(int epicId) {
        return subtasks.values().stream()
                .filter(s -> s.getEpicId() == epicId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<SubTask> getAllSubTasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.values().forEach(task -> historyManager.remove(task.getId()));
        prioritizedTasks.removeIf(t -> !(t instanceof SubTask));
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        epics.values().forEach(epic -> {
            getSubtasksForEpic(epic.getId()).forEach(sub -> {
                historyManager.remove(sub.getId());
                subtasks.remove(sub.getId());
                prioritizedTasks.removeIf(t -> t.getId() == sub.getId());
            });
            historyManager.remove(epic.getId());
        });
        epics.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.values().forEach(sub -> historyManager.remove(sub.getId()));
        prioritizedTasks.removeIf(t -> t instanceof SubTask);
        subtasks.clear();
    }

    protected void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        List<SubTask> epicSubtasks = getSubtasksForEpic(epicId);

        if (epicSubtasks.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            epic.setStartTime(null);
            epic.setEndTime(null);
            return;
        }

        boolean allNew = epicSubtasks.stream().allMatch(s -> s.getStatus() == TaskStatus.NEW);
        boolean allDone = epicSubtasks.stream().allMatch(s -> s.getStatus() == TaskStatus.DONE);

        if (allDone) epic.setStatus(TaskStatus.DONE);
        else if (allNew) epic.setStatus(TaskStatus.NEW);
        else epic.setStatus(TaskStatus.IN_PROGRESS);

        updateEpicTimeFields(epicSubtasks, epic);
    }

    private void updateEpicTimeFields(List<SubTask> epicSubtasks, Epic epic) {
        Optional<LocalDateTime> start = epicSubtasks.stream()
                .map(SubTask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo);

        Optional<LocalDateTime> end = epicSubtasks.stream()
                .map(SubTask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo);

        if (start.isPresent() && end.isPresent()) {
            epic.setStartTime(start.get());
            epic.setEndTime(end.get());
            epic.setDuration(Duration.between(start.get(), end.get()).truncatedTo(ChronoUnit.MINUTES));
        } else {
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDuration(Duration.ZERO);
        }
    }

    protected void recalculateEpicFields(Epic epic) {
        updateEpicStatus(epic.getId());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return List.copyOf(prioritizedTasks);
    }
}