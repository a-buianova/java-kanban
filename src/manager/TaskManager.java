package manager;

import task.Task;
import task.SubTask;
import task.Epic;
import java.util.List;

public interface TaskManager {

    Task createTask(Task task);
    Epic createEpic(Epic epic);
    SubTask createSubTask(SubTask subtask);

    SubTask updateSubTask(SubTask subtask);

    Task getTask(int id);
    Epic getEpic(int id);
    SubTask getSubtask(int id);

    void deleteTask(int id);
    void deleteEpic(int id);
    void deleteSubtask(int id);
    void removeEpic(int id);

    List<SubTask> getSubtasksForEpic(int epicId);
    List<Task> getTasksSortedByStatus();
    List<SubTask> getSubtasksSortedByStatus(int epicId);
    List<Task> getAllTasks();
    List<Epic> getAllEpics();

    void deleteAllTasks();
    void deleteAllEpics();
    void deleteAllSubtasks();

    List<Task> getHistory();
}

