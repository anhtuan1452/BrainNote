package Lop48K14_1.group2.brainnote.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.ui.models.Task;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public interface OnTaskCompletionToggleListener {
        void onTaskCompletionToggle(String taskId);
    }

    private List<Task> tasks;
    private OnTaskClickListener clickListener;
    private OnTaskCompletionToggleListener toggleListener;

    public TaskAdapter(List<Task> tasks, OnTaskClickListener clickListener, OnTaskCompletionToggleListener toggleListener) {
        this.tasks = tasks;
        this.clickListener = clickListener;
        this.toggleListener = toggleListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.radioButton.setText(task.getTitle());
        holder.radioButton.setChecked(task.isCompleted());

        // Set click listener for the whole item
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onTaskClick(task);
            }
        });

        // Set click listener for the radio button
        holder.radioButton.setOnClickListener(v -> {
            if (toggleListener != null) {
                toggleListener.onTaskCompletionToggle(task.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        RadioButton radioButton;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            radioButton = itemView.findViewById(R.id.radio_task);
        }
    }
}
