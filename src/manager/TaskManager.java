package manager;

import task.Epic;
import task.SubTask;
import task.Task;

import java.util.List;
import java.util.Optional;

public interface TaskManager {

    Task createTask(Task task);

    Epic createEpic(Epic epic);

    SubTask createSubTask(SubTask subtask);

    SubTask updateSubTask(SubTask subtask);

    Task updateTask(Task task);

    Epic updateEpic(Epic epic);

    Optional<Task> getTask(int id);

    Optional<Epic> getEpic(int id);

    Optional<SubTask> getSubtask(int id);

    void deleteTask(int id);

    void deleteEpic(int id);

    void deleteSubtask(int id);

    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubtasks();

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<SubTask> getAllSubTasks();

    List<SubTask> getSubtasksForEpic(int epicId);

    List<Task> getTasksSortedByStatus();

    List<SubTask> getSubtasksSortedByStatus(int epicId);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();

}