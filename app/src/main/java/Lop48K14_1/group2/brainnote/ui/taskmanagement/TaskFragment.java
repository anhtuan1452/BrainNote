package Lop48K14_1.group2.brainnote.ui.taskmanagement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.ui.adapters.TaskAdapter;
import Lop48K14_1.group2.brainnote.ui.models.Task;
import Lop48K14_1.group2.brainnote.ui.taskmanagement.FilterTaskFragment;
import Lop48K14_1.group2.brainnote.ui.utils.TaskManager;

public class TaskFragment extends Fragment {
    private RecyclerView recyclerViewInProgress;
    private RecyclerView recyclerViewCompleted;
    private TaskAdapter inProgressAdapter;
    private TaskAdapter completedAdapter;
    private TextView inProgressTitle;
    private TextView completedTitle;
    private EditText searchEditText;
    private ImageButton filterButton;
    private FloatingActionButton fabAddTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task, container, false);

        // Initialize views
        recyclerViewInProgress = view.findViewById(R.id.recycler_in_progress);
        recyclerViewCompleted = view.findViewById(R.id.recycler_completed);
        inProgressTitle = view.findViewById(R.id.title_in_progress);
        completedTitle = view.findViewById(R.id.title_completed);
        searchEditText = view.findViewById(R.id.edit_search);
        filterButton = view.findViewById(R.id.btn_filter);
        fabAddTask = view.findViewById(R.id.fab_add_task);

        // Set up recycler views
        setupRecyclerViews();

        // Set up search
        setupSearch();

        // Set up filter button
        filterButton.setOnClickListener(v -> {
            // Navigate to filter fragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment_content_main, new FilterTaskFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Set up FAB
        fabAddTask.setOnClickListener(v -> {
            // Navigate to edit task fragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment_content_main, new EditTaskFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to this fragment
        refreshTaskLists();
    }

    private void setupRecyclerViews() {
        // Set up in-progress tasks recycler view
        recyclerViewInProgress.setLayoutManager(new LinearLayoutManager(getContext()));
        inProgressAdapter = new TaskAdapter(TaskManager.getIncompleteTasks(), task -> {
            // Handle task click - navigate to edit
            EditTaskFragment editFragment = new EditTaskFragment();
            Bundle args = new Bundle();
            args.putString("task_id", task.getId());
            editFragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment_content_main, editFragment)
                    .addToBackStack(null)
                    .commit();
        }, taskId -> {
            // Handle task completion toggle
            TaskManager.toggleTaskCompletion(taskId);
            refreshTaskLists();
        });
        recyclerViewInProgress.setAdapter(inProgressAdapter);

        // Set up completed tasks recycler view
        recyclerViewCompleted.setLayoutManager(new LinearLayoutManager(getContext()));
        completedAdapter = new TaskAdapter(TaskManager.getCompletedTasks(), task -> {
            // Handle task click - navigate to edit
            EditTaskFragment editFragment = new EditTaskFragment();
            Bundle args = new Bundle();
            args.putString("task_id", task.getId());
            editFragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment_content_main, editFragment)
                    .addToBackStack(null)
                    .commit();
        }, taskId -> {
            // Handle task completion toggle
            TaskManager.toggleTaskCompletion(taskId);
            refreshTaskLists();
        });
        recyclerViewCompleted.setAdapter(completedAdapter);

        // Update section titles
        updateSectionTitles();
    }

    private void setupSearch() {
        // Implement search functionality
        searchEditText.setHint("Tìm nhiệm vụ");
        // Add text change listener for search
    }

    private void refreshTaskLists() {
        // Refresh in-progress tasks
        inProgressAdapter.updateTasks(TaskManager.getIncompleteTasks());

        // Refresh completed tasks
        completedAdapter.updateTasks(TaskManager.getCompletedTasks());

        // Update section titles
        updateSectionTitles();
    }

    private void updateSectionTitles() {
        int inProgressCount = TaskManager.getIncompleteTasks().size();
        int completedCount = TaskManager.getCompletedTasks().size();

        inProgressTitle.setText("Công việc của bạn (" + inProgressCount + ")");
        completedTitle.setText("Hoàn thành (" + completedCount + ")");
    }
}
