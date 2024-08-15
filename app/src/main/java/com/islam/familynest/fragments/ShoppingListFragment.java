package com.islam.familynest.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.islam.familynest.adapters.ShoppingItemAdapter;
import com.islam.familynest.configs.Config;
import com.islam.familynest.models.ShoppingItem;
import com.islam.familynest.models.ShoppingList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShoppingListFragment extends Fragment implements ShoppingItemAdapter.ShoppingItemListener {

    private RecyclerView shoppingItemsRecyclerView;
    private ShoppingItemAdapter shoppingItemAdapter;
    private List<ShoppingItem> shoppingItemList;
    private DatabaseReference shoppingListRef;
    private Spinner shoppingListSpinner;
    private TextView totalCostTextView;
    private List<ShoppingList> shoppingLists;
    private int selectedListPosition = 0; // Default to the first shopping list in the dropdown
    private Button clearDoneButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shopping_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        shoppingListSpinner = view.findViewById(R.id.shoppingListSpinner);
        totalCostTextView = view.findViewById(R.id.totalCostTextView);
        shoppingItemsRecyclerView = view.findViewById(R.id.shoppingItemsRecyclerView);
        clearDoneButton = view.findViewById(R.id.clearDoneButton);
        clearDoneButton.setOnClickListener(v -> clearDoneItems());

        shoppingItemsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        shoppingItemList = new ArrayList<>();
        shoppingItemAdapter = new ShoppingItemAdapter(shoppingItemList, this);
        shoppingItemsRecyclerView.setAdapter(shoppingItemAdapter);

        // Load shopping lists and items here
        loadShoppingLists();

        view.findViewById(R.id.addListButton).setOnClickListener(v -> showAddListDialog());
        view.findViewById(R.id.addItemButton).setOnClickListener(v -> showAddEditItemDialog(null));
    }

    private void clearDoneItems() {
        String selectedListUid = shoppingLists.get(selectedListPosition).getUid();
        DatabaseReference itemsRef = shoppingListRef.child(selectedListUid).child("shoppingItems");

        itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    ShoppingItem item = itemSnapshot.getValue(ShoppingItem.class);
                    if (item != null && item.isDone()) {
                        item.setDone(false);
                        itemsRef.child(item.getUid()).setValue(item);
                    }
                }

                // Refresh the RecyclerView to reflect the changes
                loadShoppingItems(selectedListUid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to clear done items", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadShoppingLists() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        shoppingListRef = FirebaseDatabase.getInstance(Config.DB_URL).getReference("ShoppingLists").child(currentUserId);

        shoppingListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isAdded()) return;

                shoppingLists = new ArrayList<>();
                List<String> shoppingListNames = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ShoppingList shoppingList = snapshot.getValue(ShoppingList.class);
                    shoppingLists.add(shoppingList);
                    shoppingListNames.add(shoppingList.getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, shoppingListNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                shoppingListSpinner.setAdapter(adapter);

                // Restore the spinner to the last selected position(list)
                shoppingListSpinner.setSelection(selectedListPosition);

                shoppingListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedListPosition = position;
                        loadShoppingItems(shoppingLists.get(position).getUid());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Failed to load shopping lists", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadShoppingItems(String shoppingListUid) {
        DatabaseReference itemsRef = shoppingListRef.child(shoppingListUid).child("shoppingItems");

        itemsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isAdded()) return;

                shoppingItemList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ShoppingItem shoppingItem = snapshot.getValue(ShoppingItem.class);
                    shoppingItemList.add(shoppingItem);
                }
                shoppingItemAdapter.notifyDataSetChanged();
                updateTotalCost();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Failed to load shopping items", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTotalCost() {
        double totalCost = 0;
        for (ShoppingItem item : shoppingItemList) {
            totalCost += item.getCost();
        }
        totalCostTextView.setText("Total: $" + totalCost);
    }

    @Override
    public void onEditItem(ShoppingItem item) {
        showAddEditItemDialog(item);
    }

    @Override
    public void onDeleteItem(ShoppingItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    shoppingListRef.child(item.getShoppingListUid()).child("shoppingItems").child(item.getUid()).removeValue();
                    loadShoppingItems(item.getShoppingListUid());
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onMarkItemDone(ShoppingItem item) {
        item.setDone(!item.isDone());
        shoppingListRef.child(item.getShoppingListUid()).child("shoppingItems").child(item.getUid()).setValue(item);
    }

    private void showAddListDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_list, null);
        dialogBuilder.setView(dialogView);

        EditText listNameEditText = dialogView.findViewById(R.id.listNameEditText);

        dialogBuilder.setTitle("Add Shopping List");
        dialogBuilder.setPositiveButton("Add", (dialog, whichButton) -> {
            String listName = listNameEditText.getText().toString().trim();
            if (!listName.isEmpty()) {
                String uid = shoppingListRef.push().getKey();
                ShoppingList shoppingList = new ShoppingList(uid, FirebaseAuth.getInstance().getCurrentUser().getUid(), listName, new HashMap<>());
                shoppingListRef.child(uid).setValue(shoppingList);
            }
        });
        dialogBuilder.setNegativeButton("Cancel", (dialog, whichButton) -> dialog.dismiss());

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void showAddEditItemDialog(@Nullable ShoppingItem item) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_edit_item, null);
        dialogBuilder.setView(dialogView);

        EditText nameEditText = dialogView.findViewById(R.id.itemNameEditText);
        EditText costEditText = dialogView.findViewById(R.id.itemCostEditText);
        CheckBox doneCheckBox = dialogView.findViewById(R.id.itemDoneCheckBox);

        if (item != null) {
            nameEditText.setText(item.getName());
            costEditText.setText(String.valueOf(item.getCost()));
            doneCheckBox.setChecked(item.isDone());
        }

        dialogBuilder.setTitle(item == null ? "Add Item" : "Update Item");
        dialogBuilder.setPositiveButton(item == null ? "Add" : "Update", (dialog, whichButton) -> {
            String name = nameEditText.getText().toString().trim();
            String costString = costEditText.getText().toString().trim();
            boolean done = doneCheckBox.isChecked();

            if (name.isEmpty() || costString.isEmpty()) {
                Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            double cost = Double.parseDouble(costString);
            String listUid = shoppingLists.get(selectedListPosition).getUid();

            if (item == null) {
                // Create a new item
                String itemUid = shoppingListRef.push().getKey();
                ShoppingItem newItem = new ShoppingItem(itemUid, listUid, name, cost, done);
                shoppingListRef.child(listUid).child("shoppingItems").child(itemUid).setValue(newItem);
            } else {
                // Update the existing item
                item.setName(name);
                item.setCost(cost);
                item.setDone(done);
                shoppingListRef.child(item.getShoppingListUid()).child("shoppingItems").child(item.getUid()).setValue(item);
            }

            // Reload the items for the current list after adding/updating
            loadShoppingItems(listUid);
        });

        dialogBuilder.setNegativeButton("Cancel", (dialog, whichButton) -> dialog.dismiss());

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }
}
