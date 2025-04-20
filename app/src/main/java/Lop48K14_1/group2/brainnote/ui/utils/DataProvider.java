package Lop48K14_1.group2.brainnote.ui.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import Lop48K14_1.group2.brainnote.ui.models.Note;
import Lop48K14_1.group2.brainnote.ui.models.Notebook;
import Lop48K14_1.group2.brainnote.ui.models.Task;

public class DataProvider {

    private static List<Notebook> notebooks = new ArrayList<>();
    private static List<Task> tasks = new ArrayList<>();

    // Khởi tạo dữ liệu mẫu nếu cần
    public static void initializeSampleData() {
        // Empty implementation
    }

    // Notebook methods
    public static List<Notebook> getNotebooks() {
        return notebooks;
    }

    public static Notebook getNotebookById(String id) {
        for (Notebook notebook : notebooks) {
            if (notebook.getId().equals(id)) {
                return notebook;
            }
        }
        return null;
    }

    public static void addNotebook(Notebook notebook) {
        notebooks.add(notebook);
    }

    public static void addNoteToNotebook(String notebookId, Note note) {
        Notebook notebook = getNotebookById(notebookId);
        if (notebook != null) {
            notebook.addNote(note);
        }
    }

    // Task methods
    public static List<Task> getTasks() {
        return tasks;
    }

    public static Task getTaskById(String id) {
        for (Task task : tasks) {
            if (task.getId().equals(id)) {
                return task;
            }
        }
        return null;
    }

    public static void addTask(Task task) {
        tasks.add(task);
    }

    public static void updateTask(Task updatedTask) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(updatedTask.getId())) {
                tasks.set(i, updatedTask);
                return;
            }
        }
        // If task not found, add it
        tasks.add(updatedTask);
    }

    public static void deleteTask(String taskId) {
        tasks.removeIf(task -> task.getId().equals(taskId));
    }

    // Cập nhật danh sách notebooks từ dữ liệu nhập
    public static void updateNotebooks(List<Notebook> newNotebooks) {
        notebooks.clear();
        notebooks.addAll(newNotebooks);
    }

    // Cập nhật danh sách tasks từ dữ liệu nhập
    public static void updateTasks(List<Task> newTasks) {
        tasks.clear();
        tasks.addAll(newTasks);
    }

    // Làm mới dữ liệu (xóa danh sách hiện tại)
    public static void clearData() {
        notebooks.clear();
        tasks.clear();
    }

    // Chuyển đổi dữ liệu sang JSON
    public static String getDataAsJson() {
        try {
            JSONObject rootObj = new JSONObject();

            // Add notebooks
            JSONArray notebooksArray = new JSONArray();
            for (Notebook notebook : notebooks) {
                JSONObject notebookObj = new JSONObject();
                notebookObj.put("id", notebook.getId());
                notebookObj.put("name", notebook.getName());

                JSONArray notesArray = new JSONArray();
                for (Note note : notebook.getNotes()) {
                    JSONObject noteObj = new JSONObject();
                    noteObj.put("id", note.getId());
                    noteObj.put("title", note.getTitle());
                    noteObj.put("content", note.getContent());
                    noteObj.put("date", note.getDate());
                    notesArray.put(noteObj);
                }

                notebookObj.put("notes", notesArray);
                notebooksArray.put(notebookObj);
            }
            rootObj.put("notebooks", notebooksArray);

            // Add tasks
            JSONArray tasksArray = new JSONArray();
            for (Task task : tasks) {
                JSONObject taskObj = new JSONObject();
                taskObj.put("id", task.getId());
                taskObj.put("title", task.getTitle());
                taskObj.put("description", task.getDescription());
                taskObj.put("date", task.getDate());
                taskObj.put("completed", task.isCompleted());
                taskObj.put("priority", task.getPriority());
                taskObj.put("dueDate", task.getDueDate());
                tasksArray.put(taskObj);
            }
            rootObj.put("tasks", tasksArray);

            return rootObj.toString(2);
        } catch (JSONException e) {
            e.printStackTrace();
            return "{}";
        }
    }
}
