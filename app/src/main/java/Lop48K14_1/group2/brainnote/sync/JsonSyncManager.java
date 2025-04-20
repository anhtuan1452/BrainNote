package Lop48K14_1.group2.brainnote.sync;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Lop48K14_1.group2.brainnote.ui.models.Note;
import Lop48K14_1.group2.brainnote.ui.models.Notebook;
import Lop48K14_1.group2.brainnote.ui.models.Task;
import Lop48K14_1.group2.brainnote.ui.utils.DataProvider;

public class JsonSyncManager {
    private static final String TAG = "JsonSyncManager";
    private static final String FILE_NAME = "data_backup.json";

    public interface OnDataImported {
        void onSuccess();
        void onFailure(Exception e);
    }

    public static String exportDataAsJson() {
        try {
            JSONObject rootObj = new JSONObject();

            // Export notebooks
            JSONArray notebooksArray = new JSONArray();
            for (Notebook notebook : DataProvider.getNotebooks()) {
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

            // Export tasks
            JSONArray tasksArray = new JSONArray();
            for (Task task : DataProvider.getTasks()) {
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
            Log.e(TAG, "JSON export error", e);
            return "{}";
        }
    }

    public static void uploadDataToFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "Cannot upload to Firebase: User not logged in");
            return;
        }

        String userId = user.getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users/" + userId);

        // Upload notebooks directly to Firebase (not as JSON string)
        DatabaseReference notebooksRef = userRef.child("notebooks");
        for (Notebook notebook : DataProvider.getNotebooks()) {
            DatabaseReference notebookRef = notebooksRef.child(notebook.getId());
            Map<String, Object> notebookData = new HashMap<>();
            notebookData.put("id", notebook.getId());
            notebookData.put("name", notebook.getName());
            notebookRef.setValue(notebookData);

            // Upload notes for this notebook
            DatabaseReference notesRef = notebookRef.child("notes");
            for (Note note : notebook.getNotes()) {
                DatabaseReference noteRef = notesRef.child(note.getId());
                Map<String, Object> noteData = new HashMap<>();
                noteData.put("id", note.getId());
                noteData.put("title", note.getTitle());
                noteData.put("content", note.getContent());
                noteData.put("date", note.getDate());
                noteRef.setValue(noteData);
            }
        }

        // Upload tasks directly to Firebase (not as JSON string)
        DatabaseReference tasksRef = userRef.child("tasks");
        for (Task task : DataProvider.getTasks()) {
            DatabaseReference taskRef = tasksRef.child(task.getId());
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("id", task.getId());
            taskData.put("title", task.getTitle());
            taskData.put("description", task.getDescription());
            taskData.put("date", task.getDate());
            taskData.put("completed", task.isCompleted());
            taskData.put("priority", task.getPriority());
            taskData.put("dueDate", task.getDueDate());
            taskRef.setValue(taskData);
        }

        // Also keep the backup JSON for backward compatibility
        userRef.child("backup_json").setValue(exportDataAsJson())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Upload to Firebase successful for user: " + userId))
                .addOnFailureListener(e -> Log.e(TAG, "Upload to Firebase failed for user: " + userId + ": " + e.getMessage(), e));
    }

    public static void saveDataToFile(Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            fos.write(exportDataAsJson().getBytes());
            fos.close();
            Log.d(TAG, "File saved successfully");
        } catch (IOException e) {
            Log.e(TAG, "Error saving file: " + e.getMessage(), e);
        }
    }

    public static void importFromFile(Context context, OnDataImported callback) {
        try {
            FileInputStream fis = context.openFileInput(FILE_NAME);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            fis.close();
            parseJsonData(builder.toString());
            callback.onSuccess();
        } catch (IOException e) {
            Log.e(TAG, "Import from file failed: " + e.getMessage(), e);
            callback.onFailure(e);
        }
    }

    public static void importFromFirebase(OnDataImported callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "Cannot import from Firebase: User not logged in");
            callback.onFailure(new Exception("User not logged in"));
            return;
        }

        String userId = user.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users/" + userId);

        // First try to import directly from the structured data
        importStructuredDataFromFirebase(userRef, new OnDataImported() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Data imported from Firebase structured data");
                callback.onSuccess();
            }

            @Override
            public void onFailure(Exception e) {
                Log.w(TAG, "Structured data import failed: " + e.getMessage() + ", trying backup JSON...");

                // Fall back to the JSON backup
                userRef.child("backup_json").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String jsonData = snapshot.getValue(String.class);
                        if (jsonData != null && !jsonData.isEmpty()) {
                            parseJsonData(jsonData);
                            callback.onSuccess();
                        } else {
                            Log.w(TAG, "No data found on Firebase for user: " + userId);
                            callback.onFailure(new Exception("No data available on Firebase"));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e(TAG, "Firebase import cancelled for user: " + userId + ": " + error.getMessage(), error.toException());
                        callback.onFailure(new Exception("Permission denied or Firebase error: " + error.getMessage()));
                    }
                });
            }
        });
    }

    private static void importStructuredDataFromFirebase(DatabaseReference userRef, OnDataImported callback) {
        // Import notebooks and tasks in parallel
        final boolean[] notebooksLoaded = {false};
        final boolean[] tasksLoaded = {false};
        final Exception[] error = {null};

        // Import notebooks
        userRef.child("notebooks").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    List<Notebook> notebookList = new ArrayList<>();

                    for (DataSnapshot notebookSnapshot : snapshot.getChildren()) {
                        String id = notebookSnapshot.child("id").getValue(String.class);
                        String name = notebookSnapshot.child("name").getValue(String.class);

                        List<Note> notes = new ArrayList<>();
                        DataSnapshot notesSnapshot = notebookSnapshot.child("notes");
                        for (DataSnapshot noteSnapshot : notesSnapshot.getChildren()) {
                            String noteId = noteSnapshot.child("id").getValue(String.class);
                            String title = noteSnapshot.child("title").getValue(String.class);
                            String content = noteSnapshot.child("content").getValue(String.class);
                            String date = noteSnapshot.child("date").getValue(String.class);

                            notes.add(new Note(noteId, title, content, date));
                        }

                        notebookList.add(new Notebook(id, name, notes));
                    }

                    DataProvider.updateNotebooks(notebookList);
                    Log.d(TAG, "Notebooks loaded from Firebase: " + notebookList.size());

                    notebooksLoaded[0] = true;
                    checkAllLoaded(notebooksLoaded[0], tasksLoaded[0], error[0], callback);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading notebooks from Firebase", e);
                    error[0] = e;
                    checkAllLoaded(notebooksLoaded[0], tasksLoaded[0], error[0], callback);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Firebase notebooks import cancelled: " + error.getMessage());
                notebooksLoaded[0] = true;
                checkAllLoaded(notebooksLoaded[0], tasksLoaded[0], new Exception(error.getMessage()), callback);
            }
        });

        // Import tasks
        userRef.child("tasks").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    List<Task> taskList = new ArrayList<>();

                    for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                        String id = taskSnapshot.child("id").getValue(String.class);
                        String title = taskSnapshot.child("title").getValue(String.class);
                        String description = taskSnapshot.child("description").getValue(String.class);
                        String date = taskSnapshot.child("date").getValue(String.class);
                        Boolean completed = taskSnapshot.child("completed").getValue(Boolean.class);
                        Integer priority = taskSnapshot.child("priority").getValue(Integer.class);
                        String dueDate = taskSnapshot.child("dueDate").getValue(String.class);

                        // Handle null values
                        if (completed == null) completed = false;
                        if (priority == null) priority = 0;

                        taskList.add(new Task(id, title, description, date, completed, priority, dueDate));
                    }

                    DataProvider.updateTasks(taskList);
                    Log.d(TAG, "Tasks loaded from Firebase: " + taskList.size());

                    tasksLoaded[0] = true;
                    checkAllLoaded(notebooksLoaded[0], tasksLoaded[0], error[0], callback);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading tasks from Firebase", e);
                    error[0] = e;
                    checkAllLoaded(notebooksLoaded[0], tasksLoaded[0], error[0], callback);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Firebase tasks import cancelled: " + error.getMessage());
                tasksLoaded[0] = true;
                checkAllLoaded(notebooksLoaded[0], tasksLoaded[0], new Exception(error.getMessage()), callback);
            }
        });
    }

    private static void checkAllLoaded(boolean notebooksLoaded, boolean tasksLoaded, Exception error, OnDataImported callback) {
        if (notebooksLoaded && tasksLoaded) {
            if (error != null) {
                callback.onFailure(error);
            } else {
                callback.onSuccess();
            }
        }
    }

    public static void importDataWithFallback(Context context, OnDataImported callback) {
        importFromFirebase(new OnDataImported() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Data imported from Firebase");
                callback.onSuccess();
            }

            @Override
            public void onFailure(Exception e) {
                Log.w(TAG, "Firebase import failed: " + e.getMessage() + ", trying local file...");
                importFromFile(context, new OnDataImported() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Data imported from local file");
                        callback.onSuccess();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Import failed from both Firebase and local file: " + e.getMessage());
                        callback.onFailure(e);
                    }
                });
            }
        });
    }

    public static void parseJsonData(String json) {
        List<Notebook> notebookList = new ArrayList<>();
        List<Task> taskList = new ArrayList<>();

        try {
            if (json == null || json.trim().isEmpty()) {
                Log.w(TAG, "Empty or null JSON data");
                return;
            }

            JSONObject rootObj = new JSONObject(json);

            // Parse notebooks
            if (rootObj.has("notebooks")) {
                JSONArray notebooksArray = rootObj.getJSONArray("notebooks");
                for (int i = 0; i < notebooksArray.length(); i++) {
                    JSONObject notebookObj = notebooksArray.getJSONObject(i);

                    String id = notebookObj.getString("id");
                    String name = notebookObj.getString("name");

                    List<Note> notes = new ArrayList<>();
                    JSONArray notesArray = notebookObj.getJSONArray("notes");

                    for (int j = 0; j < notesArray.length(); j++) {
                        JSONObject noteObj = notesArray.getJSONObject(j);

                        String noteId = noteObj.getString("id");
                        String title = noteObj.getString("title");
                        String content = noteObj.getString("content");
                        String date = noteObj.getString("date");

                        notes.add(new Note(noteId, title, content, date));
                    }

                    notebookList.add(new Notebook(id, name, notes));
                }

                // Update notebooks in DataProvider
                DataProvider.updateNotebooks(notebookList);
                Log.d(TAG, "DataProvider updated with " + notebookList.size() + " notebooks");
            }

            // Parse tasks
            if (rootObj.has("tasks")) {
                JSONArray tasksArray = rootObj.getJSONArray("tasks");
                for (int i = 0; i < tasksArray.length(); i++) {
                    JSONObject taskObj = tasksArray.getJSONObject(i);

                    String id = taskObj.getString("id");
                    String title = taskObj.getString("title");
                    String description = taskObj.optString("description", "");
                    String date = taskObj.getString("date");
                    boolean completed = taskObj.optBoolean("completed", false);
                    int priority = taskObj.optInt("priority", 0);
                    String dueDate = taskObj.optString("dueDate", "");

                    taskList.add(new Task(id, title, description, date, completed, priority, dueDate));
                }

                // Update tasks in DataProvider
                DataProvider.updateTasks(taskList);
                Log.d(TAG, "DataProvider updated with " + taskList.size() + " tasks");
            }
        } catch (JSONException e) {
            Log.e(TAG, "JSON parsing error: " + e.getMessage(), e);
        }
    }

    // Methods for direct task operations with Firebase
    public static void addTaskToFirebase(Task task) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "Cannot add task to Firebase: User not logged in");
            return;
        }

        String userId = user.getUid();
        DatabaseReference taskRef = FirebaseDatabase.getInstance()
                .getReference("users/" + userId + "/tasks/" + task.getId());

        Map<String, Object> taskData = new HashMap<>();
        taskData.put("id", task.getId());
        taskData.put("title", task.getTitle());
        taskData.put("description", task.getDescription());
        taskData.put("date", task.getDate());
        taskData.put("completed", task.isCompleted());
        taskData.put("priority", task.getPriority());
        taskData.put("dueDate", task.getDueDate());

        taskRef.setValue(taskData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Task added to Firebase: " + task.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to add task to Firebase: " + e.getMessage(), e));
    }

    public static void updateTaskInFirebase(Task task) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "Cannot update task in Firebase: User not logged in");
            return;
        }

        String userId = user.getUid();
        DatabaseReference taskRef = FirebaseDatabase.getInstance()
                .getReference("users/" + userId + "/tasks/" + task.getId());

        Map<String, Object> taskData = new HashMap<>();
        taskData.put("id", task.getId());
        taskData.put("title", task.getTitle());
        taskData.put("description", task.getDescription());
        taskData.put("date", task.getDate());
        taskData.put("completed", task.isCompleted());
        taskData.put("priority", task.getPriority());
        taskData.put("dueDate", task.getDueDate());

        taskRef.updateChildren(taskData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Task updated in Firebase: " + task.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update task in Firebase: " + e.getMessage(), e));
    }

    public static void deleteTaskFromFirebase(String taskId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e(TAG, "Cannot delete task from Firebase: User not logged in");
            return;
        }

        String userId = user.getUid();
        DatabaseReference taskRef = FirebaseDatabase.getInstance()
                .getReference("users/" + userId + "/tasks/" + taskId);

        taskRef.removeValue()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Task deleted from Firebase: " + taskId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to delete task from Firebase: " + e.getMessage(), e));
    }
}
