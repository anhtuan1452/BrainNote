package Lop48K14_1.group2.brainnote.ui.taskmanagement;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.ui.models.Task;
import Lop48K14_1.group2.brainnote.ui.utils.DataProvider;
import Lop48K14_1.group2.brainnote.ui.utils.TaskManager;

public class EditTaskFragment extends Fragment {
    private ImageButton closeButton;
    private TextView titleTextView;
    private Button doneButton;
    private RadioButton taskRadioButton;
    private EditText taskTitleEditText;
    private EditText taskDescriptionEditText;
    private ChipGroup chipGroupPriority;
    private Button dueDateButton;

    private String taskId;
    private Task currentTask;
    private String selectedDueDate = "";
    private int selectedPriority = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_task, container, false);

        // Initialize views
        closeButton = view.findViewById(R.id.btn_close);
        titleTextView = view.findViewById(R.id.text_title);
        doneButton = view.findViewById(R.id.btn_done);
        taskRadioButton = view.findViewById(R.id.radio_task);
        taskTitleEditText = view.findViewById(R.id.edit_task_title);
        taskDescriptionEditText = view.findViewById(R.id.edit_task_description);
        chipGroupPriority = view.findViewById(R.id.chip_group_priority);
        dueDateButton = view.findViewById(R.id.btn_due_date);

        // Set up title
        titleTextView.setText("Nhiệm vụ");
        doneButton.setText("Xong");

        // Check if editing existing task
        Bundle args = getArguments();
        if (args != null && args.containsKey("task_id")) {
            taskId = args.getString("task_id");
            currentTask = DataProvider.getTaskById(taskId);
            if (currentTask != null) {
                // Populate fields with task data
                taskTitleEditText.setText(currentTask.getTitle());
                taskDescriptionEditText.setText(currentTask.getDescription());
                taskRadioButton.setChecked(currentTask.isCompleted());
                selectedPriority = currentTask.getPriority();
                selectedDueDate = currentTask.getDueDate();
            }
        }

        // Set up priority chips
        setupPriorityChips();

        // Set up due date button
        setupDueDateButton();

        // Set up close button
        closeButton.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        // Set up done button
        doneButton.setOnClickListener(v -> {
            saveTask();
        });

        return view;
    }

    private void setupPriorityChips() {
        String[] priorityLabels = {"Thấp", "Trung bình", "Cao"};

        for (int i = 0; i < priorityLabels.length; i++) {
            final int priority = i;
            Chip chip = new Chip(getContext());
            chip.setText(priorityLabels[i]);
            chip.setCheckable(true);
            chip.setChecked(selectedPriority == priority);

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedPriority = priority;
                    // Uncheck other chips
                    for (int j = 0; j < chipGroupPriority.getChildCount(); j++) {
                        Chip otherChip = (Chip) chipGroupPriority.getChildAt(j);
                        if (otherChip != buttonView) {
                            otherChip.setChecked(false);
                        }
                    }
                }
            });

            chipGroupPriority.addView(chip);
        }
    }

    private void setupDueDateButton() {
        // Set initial text
        if (selectedDueDate != null && !selectedDueDate.isEmpty()) {
            dueDateButton.setText("Ngày hết hạn: " + selectedDueDate);
        } else {
            dueDateButton.setText("Đặt ngày hết hạn");
        }

        // Set click listener
        dueDateButton.setOnClickListener(v -> {
            showDatePicker();
        });
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();

        // If we have a due date already, use it
        if (selectedDueDate != null && !selectedDueDate.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = sdf.parse(selectedDueDate);
                if (date != null) {
                    calendar.setTime(date);
                }
            } catch (Exception e) {
                // Use current date if parsing fails
            }
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    selectedDueDate = sdf.format(calendar.getTime());
                    dueDateButton.setText("Ngày hết hạn: " + selectedDueDate);
                },
                year, month, day);

        datePickerDialog.show();
    }

    private void saveTask() {
        String title = taskTitleEditText.getText().toString().trim();
        String description = taskDescriptionEditText.getText().toString().trim();
        boolean completed = taskRadioButton.isChecked();

        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập tiêu đề nhiệm vụ", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        if (currentTask != null) {
            // Update existing task
            currentTask.setTitle(title);
            currentTask.setDescription(description);
            currentTask.setCompleted(completed);
            currentTask.setPriority(selectedPriority);
            currentTask.setDueDate(selectedDueDate);

            TaskManager.updateTask(currentTask);
        } else {
            // Create new task
            Task newTask = new Task(title, description, currentDate, completed, selectedPriority, selectedDueDate);
            TaskManager.addTask(title, description, selectedPriority, selectedDueDate);
        }

        // Go back to task list
        getParentFragmentManager().popBackStack();
    }
}
