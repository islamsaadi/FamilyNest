package com.islam.familynest.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import com.islam.familynest.adapters.BudgetAdapter;
import com.islam.familynest.configs.Config;
import com.islam.familynest.models.BudgetItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BudgetFragment extends Fragment implements BudgetAdapter.BudgetItemListener {

    private TextView totalTextView;
    private Spinner monthSpinner;
    private Spinner yearSpinner;
    private Button filterButton;
    private RecyclerView budgetRecyclerView;
    private BudgetAdapter budgetAdapter;
    private List<BudgetItem> budgetItemList;
    private DatabaseReference budgetRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        totalTextView = view.findViewById(R.id.totalTextView);
        monthSpinner = view.findViewById(R.id.monthSpinner);
        yearSpinner = view.findViewById(R.id.yearSpinner);
        filterButton = view.findViewById(R.id.filterButton);
        budgetRecyclerView = view.findViewById(R.id.budgetRecyclerView);

        budgetItemList = new ArrayList<>();
        budgetAdapter = new BudgetAdapter(budgetItemList, this);
        budgetRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        budgetRecyclerView.setAdapter(budgetAdapter);

        budgetRef = FirebaseDatabase.getInstance(Config.DB_URL).getReference("BudgetItems")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        populateSpinners();
        loadBudgetItems(getCurrentMonth(), getCurrentYear());

        filterButton.setOnClickListener(v -> filterBudgetItems());

        view.findViewById(R.id.addItemButton).setOnClickListener(v -> showAddEditItemDialog(null));
    }

    private void populateSpinners() {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        String currentMonth = getCurrentMonth();
        String currentYear = getCurrentYear();

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

        int monthPosition = monthAdapter.getPosition(currentMonth);
        monthSpinner.setSelection(monthPosition);

        String[] years = getLastTenYears();
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        int yearPosition = yearAdapter.getPosition(currentYear);
        yearSpinner.setSelection(yearPosition);
    }

    private String getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        return new SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.getTime());
    }

    private String getCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        return String.valueOf(calendar.get(Calendar.YEAR));
    }

    private String[] getLastTenYears() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        String[] years = new String[11];
        for (int i = 0; i < 11; i++) {
            years[i] = String.valueOf(currentYear - (10 - i));
        }

        return years;
    }

    private void filterBudgetItems() {
        String selectedMonth = (String) monthSpinner.getSelectedItem();
        String selectedYear = (String) yearSpinner.getSelectedItem();
        loadBudgetItems(selectedMonth, selectedYear);
    }

    private void loadBudgetItems(String month, String year) {
        budgetRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isAdded()) return;

                budgetItemList.clear();
                double total = 0.0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    BudgetItem item = snapshot.getValue(BudgetItem.class);

                    if (item != null && matchesFilter(item, month, year)) {
                        budgetItemList.add(item);
                        total += item.getType().equals("Income") ? item.getAmount() : -item.getAmount();
                    }
                }

                Collections.sort(budgetItemList, new Comparator<BudgetItem>() {
                    @Override
                    public int compare(BudgetItem item1, BudgetItem item2) {
                        return item2.getDate().compareTo(item1.getDate());
                    }
                });

                totalTextView.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));
                budgetAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Failed to load Budget items", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean matchesFilter(BudgetItem item, String month, String year) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(item.getDate());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.getDefault());
        int itemYear = cal.get(Calendar.YEAR);

        return monthFormat.format(cal.getTime()).equals(month) && String.valueOf(itemYear).equals(year);
    }

    private void showAddEditItemDialog(@Nullable BudgetItem budgetItem) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_edit_budget_item, null);
        dialogBuilder.setView(dialogView);
        AlertDialog alertDialog = dialogBuilder.create();

        EditText itemNameEditText = dialogView.findViewById(R.id.itemNameEditText);
        EditText amountEditText = dialogView.findViewById(R.id.amountEditText);
        Spinner typeSpinner = dialogView.findViewById(R.id.typeSpinner);
        TextView dateTextView = dialogView.findViewById(R.id.dateTextView);
        Button saveButton = dialogView.findViewById(R.id.saveButton);

        Calendar selectedDate = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        String[] types = {"Income", "Expense"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        if (budgetItem != null) {
            itemNameEditText.setText(budgetItem.getItemName());
            amountEditText.setText(String.valueOf(budgetItem.getAmount()));
            dateTextView.setText(dateFormat.format(budgetItem.getDate()));
            selectedDate.setTime(budgetItem.getDate());
            typeSpinner.setSelection(budgetItem.getType().equals("Income") ? 0 : 1);
        } else {
            dateTextView.setText(dateFormat.format(selectedDate.getTime()));
        }

        dateTextView.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        selectedDate.set(year, month, dayOfMonth);
                        dateTextView.setText(dateFormat.format(selectedDate.getTime()));
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        saveButton.setOnClickListener(v -> {
            String itemName = itemNameEditText.getText().toString().trim();
            String amountText = amountEditText.getText().toString().trim();

            if (itemName.isEmpty()) {
                Toast.makeText(requireContext(), "Item name cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (amountText.isEmpty() || Double.parseDouble(amountText) <= 0) {
                Toast.makeText(requireContext(), "Please enter a valid amount!", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountText);
            String type = typeSpinner.getSelectedItem().toString();
            Date date = selectedDate.getTime();
            BudgetItem newItem = new BudgetItem(itemName, amount, type, date);

            if (budgetItem != null) {
                newItem.setId(budgetItem.getId());
                budgetRef.child(budgetItem.getId()).setValue(newItem);
            } else {
                String id = budgetRef.push().getKey();
                newItem.setId(id);
                budgetRef.child(id).setValue(newItem);
            }

            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    private void deleteBudgetItem(BudgetItem budgetItem) {
        budgetRef.child(budgetItem.getId()).removeValue();
    }

    @Override
    public void onEditItem(BudgetItem budgetItem) {
        showAddEditItemDialog(budgetItem);
    }

    @Override
    public void onDeleteItem(BudgetItem budgetItem) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Yes", (dialog, which) -> deleteBudgetItem(budgetItem))
                .setNegativeButton("No", null)
                .show();
    }
}
