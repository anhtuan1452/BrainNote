package Lop48K14_1.group2.brainnote.ui.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Lop48K14_1.group2.brainnote.sync.JsonSyncManager;
import Lop48K14_1.group2.brainnote.ui.models.Task;

public class TaskManager {
    private static final String TAG = "TaskManager";

    // Get all tasks
    public static List<Task> getAllTasks() {
        return DataProvider.getTasks();
    }

    // Get completed tasks
    public static List<Task> getCompletedTasks() {
        List<Task> completedTasks = new ArrayList<>();
        for (Task task : DataProvider.getTasks()) {
            if (task.isCompleted()) {
                completedTasks.add(task);
            }
        }
        return completedTasks;
    }

    // Get incomplete tasks
    public static List<Task> getIncompleteTasks() {
        List<Task> incompleteTasks = new ArrayList<>();
        for (Task task : DataProvider.getTasks()) {
            if (!task.isCompleted()) {
                incompleteTasks.add(task);
            }
        }
        return incompleteTasks;
    }

    // Add a new task
    public static void addTask(String title, String description, int priority, String dueDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        Task newTask = new Task(title, description, currentDate, false, priority, dueDate);

        // Add to local data
        DataProvider.addTask(newTask);

        // Sync with Firebase
        JsonSyncManager.addTaskToFirebase(newTask);

        Log.d(TAG, "New task added: " + newTask.getId());
    }

    // Update an existing task
    public static void updateTask(Task task) {
        // Update local data
        DataProvider.updateTask(task);

        // Sync with Firebase
        JsonSyncManager.updateTaskInFirebase(task);

        Log.d(TAG, "Task updated: " + task.getId());
    }

    // Delete a task
    public static void deleteTask(String taskId) {
        // Delete from local data
        DataProvider.deleteTask(taskId);

        // Sync with Firebase
        JsonSyncManager.deleteTaskFromFirebase(taskId);

        Log.d(TAG, "Task deleted: " + taskId);
    }

    // Toggle task completion status
    public static void toggleTaskCompletion(String taskId) {
        Task task = DataProvider.getTaskById(taskId);
        if (task != null) {
            task.setCompleted(!task.isCompleted());
            updateTask(task);
        }
    }

    // Get tasks by priority
    public static List<Task> getTasksByPriority(int priority) {
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : DataProvider.getTasks()) {
            if (task.getPriority() == priority) {
                filteredTasks.add(task);
            }
        }
        return filteredTasks;
    }
}
