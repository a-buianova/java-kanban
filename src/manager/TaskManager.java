package manager;

import task.*;

import java.util.List;

public interface TaskManager {

    Task createTask(Task task);

    Epic createEpic(Epic epic);

    SubTask createSubTask(SubTask subtask);

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubTask(SubTask subtask);

    void updateEpicStatus(int epicId);

    Task getTask(int id);

    Epic getEpic(int id);

    SubTask getSubtask(int id);

    void deleteTask(int id);

    void deleteEpic(int id);

    void deleteSubtask(int id);

    List<SubTask> getSubtasksForEpic(int epicId);

    List<Task> getTasksSortedByStatus();

    List<SubTask> getSubtasksSortedByStatus(int epicId);

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<SubTask> getAllSubtasks();

    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubtasks();

    List<Task> getHistory();
}
