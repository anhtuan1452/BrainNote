package Lop48K14_1.group2.brainnote.ui.taskmanagement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import Lop48K14_1.group2.brainnote.R;

public class FilterTaskFragment extends Fragment {
    private ImageButton closeButton;
    private TextView titleTextView;
    private Button doneButton;
    private Switch switchPriority, switchDueDate, switchCompleted;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_task, container, false);

        // Initialize views
        closeButton = view.findViewById(R.id.btn_close);
        titleTextView = view.findViewById(R.id.text_title);
        doneButton = view.findViewById(R.id.btn_done);
        switchPriority = view.findViewById(R.id.switch_priority);
        switchDueDate = view.findViewById(R.id.switch_due_date);
        switchCompleted = view.findViewById(R.id.switch_completed);

        // Set up title
        titleTextView.setText("Lọc nhiệm vụ");
        doneButton.setText("Xong");

        // Set up filter options
        TextView priorityText = view.findViewById(R.id.text_priority);
        priorityText.setText("Được đánh dấu");

        TextView dueDateText = view.findViewById(R.id.text_due_date);
        dueDateText.setText("Ngày hết hạn");

        TextView completedText = view.findViewById(R.id.text_completed);
        completedText.setText("Đã hoàn tất");

        // Set up close button click listener
        closeButton.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        // Set up done button click listener
        doneButton.setOnClickListener(v -> {
            // Apply filters and navigate back
            applyFilters();
            getParentFragmentManager().popBackStack();
        });

        return view;
    }

    private void applyFilters() {
        // Create a bundle with filter settings
        Bundle filterBundle = new Bundle();
        filterBundle.putBoolean("filter_priority", switchPriority.isChecked());
        filterBundle.putBoolean("filter_due_date", switchDueDate.isChecked());
        filterBundle.putBoolean("filter_completed", switchCompleted.isChecked());

        // Pass the filters back to the TaskFragment
        getParentFragmentManager().setFragmentResult("task_filters", filterBundle);
    }
}
