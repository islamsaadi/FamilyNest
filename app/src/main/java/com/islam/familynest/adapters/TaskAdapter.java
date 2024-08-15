package com.islam.familynest.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.islam.familynest.R;
import com.islam.familynest.models.Task;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private TaskItemListener listener;

    public TaskAdapter(List<Task> taskList, TaskItemListener listener) {
        setTaskList(taskList);
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setTaskList(List<Task> tasks) {
        // Sort and set the task list
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                // First, compare by done status (undone first)
                if (t1.isDone() != t2.isDone()) {
                    return t1.isDone() ? 1 : -1;
                }

                // If both tasks are undone, sort by priority (higher priority first)
                if (!t1.isDone()) {
                    return Integer.compare(t2.getPriority(), t1.getPriority());
                }

                // If both are done, keep their order (no additional sorting needed)
                return 0;
            }
        });

        this.taskList = tasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.taskTitleTextView.setText(task.getTitle());
        holder.taskDateTextView.setText(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(task.getDate()));
        holder.taskRewardTextView.setText(String.format(Locale.getDefault(), "+%d", task.getReward()));

        if (task.isDone()) {
            holder.doneButton.setEnabled(false);
            holder.doneButton.setAlpha(0.5f);
        } else {
            holder.doneButton.setEnabled(true);
            holder.doneButton.setAlpha(1.0f);
            listener.onMarkTaskUndone(task);
        }

        // Set the priority text and color
        switch (task.getPriority()) {
            case 1: // High
                holder.taskPriorityTextView.setText("High");
                holder.taskPriorityTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.priority_high));
                break;
            case 2: // Medium
                holder.taskPriorityTextView.setText("Medium");
                holder.taskPriorityTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.priority_medium));
                break;
            case 3: // Low
                holder.taskPriorityTextView.setText("Low");
                holder.taskPriorityTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.priority_low));
                break;
        }

        // Set the visibility and color of the done button and watermark
        if (task.isDone()) {
            holder.doneButton.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark), android.graphics.PorterDuff.Mode.SRC_IN);
            holder.doneWatermark.setVisibility(View.VISIBLE);
        } else {
            holder.doneButton.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
            holder.doneWatermark.setVisibility(View.GONE);
        }

        // Mark done with confirmation
        holder.doneButton.setOnClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Mark Task as Done")
                    .setMessage("Are you sure you want to mark this task as done?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        listener.onMarkTaskDone(task);
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        });

        holder.editTaskButton.setOnClickListener(v -> listener.onEditTask(task));
        holder.deleteTaskButton.setOnClickListener(v -> listener.onDeleteTask(task));
    }


    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView doneWatermark;
        TextView taskTitleTextView;
        TextView taskDateTextView;
        TextView taskRewardTextView;
        TextView taskPriorityTextView;
        ImageButton doneButton;
        ImageButton editTaskButton;
        ImageButton deleteTaskButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            doneWatermark = itemView.findViewById(R.id.doneWatermark);
            taskTitleTextView = itemView.findViewById(R.id.taskTitleTextView);
            taskDateTextView = itemView.findViewById(R.id.taskDateTextView);
            taskRewardTextView = itemView.findViewById(R.id.taskRewardTextView);
            taskPriorityTextView = itemView.findViewById(R.id.taskPriorityTextView);
            doneButton = itemView.findViewById(R.id.doneButton);
            editTaskButton = itemView.findViewById(R.id.editTaskButton);
            deleteTaskButton = itemView.findViewById(R.id.deleteTaskButton);
        }
    }

    public interface TaskItemListener {
        void onMarkTaskDone(Task task);
        void onMarkTaskUndone(Task task);
        void onEditTask(Task task);
        void onDeleteTask(Task task);
    }
}
