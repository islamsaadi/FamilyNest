package com.islam.familynest.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.islam.familynest.R;
import com.islam.familynest.adapters.TaskAdapter;
import com.islam.familynest.configs.Config;
import com.islam.familynest.models.FamilyMember;
import com.islam.familynest.models.Task;
import com.islam.familynest.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TaskFragment extends Fragment implements TaskAdapter.TaskItemListener {

    private RecyclerView tasksRecyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private List<Task> filteredTaskList;
    private DatabaseReference tasksRef;
    private EditText dateFilterEditText;
    private Calendar selectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tasks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        dateFilterEditText = view.findViewById(R.id.dateFilterEditText);
        selectedDate = Calendar.getInstance();

        taskList = new ArrayList<>();
        filteredTaskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(filteredTaskList, this);
        tasksRecyclerView.setAdapter(taskAdapter);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tasksRef = FirebaseDatabase.getInstance(Config.DB_URL).getReference("Tasks").child(currentUserId);

        // Load tasks
        loadTasks();

        // Set up date picker for filtering
        setupDatePicker();

        view.findViewById(R.id.addTaskButton).setOnClickListener(v -> showAddEditTaskDialog(null));
    }

    private void setupDatePicker() {
        selectedDate = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        dateFilterEditText.setText(sdf.format(selectedDate.getTime()));

        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            selectedDate.set(Calendar.YEAR, year);
            selectedDate.set(Calendar.MONTH, month);
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            dateFilterEditText.setText(sdf.format(selectedDate.getTime()));
            filterTasksByDate();
        };

        dateFilterEditText.setOnClickListener(v -> {
            new DatePickerDialog(requireContext(), dateSetListener,
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void loadTasks() {
        tasksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isAdded()) return;

                taskList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Task task = snapshot.getValue(Task.class);
                    if (task != null) {
                        taskList.add(task);
                    }
                }
                sortAndFilterTasks();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Failed to load tasks", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sortAndFilterTasks() {
        Collections.sort(taskList, new Comparator<Task>() {
            @Override
            public int compare(Task t1, Task t2) {
                if (t1.isDone() != t2.isDone()) {
                    return t1.isDone() ? 1 : -1;
                }

                if (!t1.isDone()) {
                    return Integer.compare(t1.getPriority(), t2.getPriority());
                }

                return 0;
            }
        });

        filterTasksByDate();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterTasksByDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String selectedDateString = sdf.format(selectedDate.getTime());

        filteredTaskList.clear();
        filteredTaskList.addAll(taskList.stream()
                .filter(task -> sdf.format(task.getDate()).equals(selectedDateString))
                .collect(Collectors.toList()));

        taskAdapter.notifyDataSetChanged();
    }

    private void showAddEditTaskDialog(@Nullable Task task) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_edit_task, null);
        dialogBuilder.setView(dialogView);

        EditText titleEditText = dialogView.findViewById(R.id.titleEditText);
        EditText dateEditText = dialogView.findViewById(R.id.dateEditText);
        EditText rewardEditText = dialogView.findViewById(R.id.rewardEditText);
        CheckBox doneCheckBox = dialogView.findViewById(R.id.doneCheckBox);
        Spinner prioritySpinner = dialogView.findViewById(R.id.prioritySpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.priority_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(adapter);

        Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            dateEditText.setText(sdf.format(calendar.getTime()));
        };

        dateEditText.setOnClickListener(v -> {
            new DatePickerDialog(requireContext(), dateSetListener, calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        if (task != null) {
            titleEditText.setText(task.getTitle());
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            dateEditText.setText(sdf.format(task.getDate()));
            rewardEditText.setText(String.valueOf(task.getReward()));
            doneCheckBox.setChecked(task.isDone());

            prioritySpinner.setSelection(task.getPriority() - 1);
        }

        dialogBuilder.setTitle(task == null ? "Add Task" : "Edit Task");
        dialogBuilder.setPositiveButton(task == null ? "Add" : "Update", (dialog, whichButton) -> {
            String title = titleEditText.getText().toString().trim();
            String dateString = dateEditText.getText().toString().trim();
            String rewardString = rewardEditText.getText().toString().trim();
            boolean done = doneCheckBox.isChecked();
            int priority = prioritySpinner.getSelectedItemPosition() + 1;

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(dateString) || TextUtils.isEmpty(rewardString)) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            int reward = Integer.parseInt(rewardString);
            Date date;
            try {
                date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(dateString);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
                return;
            }

            if (task == null) {
                String id = tasksRef.push().getKey();
                Task newTask = new Task(
                        id,
                        FirebaseAuth.getInstance().getCurrentUser().getUid(),
                        title,
                        date,
                        done,
                        reward,
                        null,
                        priority
                );
                tasksRef.child(id).setValue(newTask);
            } else {
                task.setTitle(title);
                task.setDate(date);
                task.setReward(reward);
                task.setDone(done);
                task.setPriority(priority);
                tasksRef.child(task.getId()).setValue(task);
            }
        });

        dialogBuilder.setNegativeButton("Cancel", (dialog, whichButton) -> dialog.dismiss());

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onMarkTaskDone(Task task) {
        fetchOwnerAsMemberAndShowSelectFamilyMemberDialog(task, true);
    }

    @Override
    public void onMarkTaskUndone(Task task) {
        if (task.getDoneByUserId() != null) {
            updateMemberRewards(task, false);
            task.setDone(false);
            task.setDoneByUserId(null);
            tasksRef.child(task.getId()).setValue(task);
        }
    }

    private void updateMemberRewards(Task task, boolean isAdding) {
        int reward = task.getReward();
        String ownerUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String memberUid = task.getDoneByUserId();
        boolean isUser = false;

        DatabaseReference memberRef;
        if (memberUid.equals(ownerUid)) {
            memberRef = FirebaseDatabase.getInstance(Config.DB_URL)
                    .getReference("Users")
                    .child(ownerUid);
            isUser = true;
        } else {
            memberRef = FirebaseDatabase.getInstance(Config.DB_URL)
                    .getReference("FamilyMembers")
                    .child(ownerUid)
                    .child(memberUid);
        }

        boolean finalIsUser = isUser;
        memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(finalIsUser) {
                    User member = dataSnapshot.getValue(User.class);
                    if (member != null) {
                        int updatedRewards = isAdding ? member.getRewards() + reward : member.getRewards() - reward;
                        if (updatedRewards < 0) updatedRewards = 0;
                        member.setRewards(updatedRewards);
                        memberRef.setValue(member);
                    }
                } else {
                    FamilyMember member = dataSnapshot.getValue(FamilyMember.class);
                    if (member != null) {
                        int updatedRewards = isAdding ? member.getRewards() + reward : member.getRewards() - reward;
                        if (updatedRewards < 0) updatedRewards = 0;
                        member.setRewards(updatedRewards);
                        memberRef.setValue(member);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Failed to update rewards", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchOwnerAsMemberAndShowSelectFamilyMemberDialog(Task task, boolean isDone) {
        String ownerUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ownerRef = FirebaseDatabase.getInstance(Config.DB_URL)
                .getReference("Users")
                .child(ownerUid);

        ownerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                String ownerName = snapshot.exists() ? snapshot.child("name").getValue(String.class) : "Owner";

                fetchFamilyMembersAndShowDialog(task, isDone, ownerUid, ownerName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchFamilyMembersAndShowDialog(Task task, boolean isDone, String ownerUid, String ownerName) {
        DatabaseReference familyRef = FirebaseDatabase.getInstance(Config.DB_URL)
                .getReference("FamilyMembers")
                .child(ownerUid);

        familyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isAdded()) return;

                List<FamilyMember> familyMembers = new ArrayList<>();
                List<String> memberNames = new ArrayList<>();

                FamilyMember ownerMember = new FamilyMember(ownerUid, ownerName, 0);
                familyMembers.add(ownerMember);
                memberNames.add(ownerName);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FamilyMember member = snapshot.getValue(FamilyMember.class);
                    if (member != null && !member.getUid().equals(ownerUid)) {
                        familyMembers.add(member);
                        memberNames.add(member.getName());
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_dropdown_item, memberNames);

                final Spinner spinner = new Spinner(requireContext());
                spinner.setAdapter(adapter);

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
                dialogBuilder.setTitle("Select Family Member");
                dialogBuilder.setView(spinner);
                dialogBuilder.setPositiveButton("Confirm", (dialog, which) -> {
                    int selectedPosition = spinner.getSelectedItemPosition();
                    FamilyMember selectedMember = familyMembers.get(selectedPosition);

                    if (isDone) {
                        task.setDone(true);
                        task.setDoneByUserId(selectedMember.getUid());
                        tasksRef.child(task.getId()).setValue(task);

                        updateMemberRewards(task, true);
                    }
                });

                dialogBuilder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                dialogBuilder.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Failed to load family members", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditTask(Task task) {
        showAddEditTaskDialog(task);
    }

    @Override
    public void onDeleteTask(Task task) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", (dialog, which) -> deleteTask(task))
                .setNegativeButton("No", null)
                .show();
    }

    public void deleteTask(Task task) {
        tasksRef.child(task.getId()).removeValue();
    }
}
