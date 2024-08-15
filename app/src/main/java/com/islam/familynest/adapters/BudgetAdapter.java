package com.islam.familynest.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.islam.familynest.R;
import com.islam.familynest.configs.Config;
import com.islam.familynest.models.BudgetItem;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private List<BudgetItem> budgetItemList;
    private BudgetItemListener listener;

    public BudgetAdapter(List<BudgetItem> budgetItemList, BudgetItemListener listener) {
        this.budgetItemList = budgetItemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.budget_item, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        BudgetItem budgetItem = budgetItemList.get(position);

        // Bind data to the views
        holder.itemNameTextView.setText(budgetItem.getItemName());
        holder.amountTextView.setText(String.format(Locale.getDefault(), "$%.2f", budgetItem.getAmount()));
        holder.typeTextView.setText(budgetItem.getType());
        holder.dateTextView.setText(new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(budgetItem.getDate()));

        holder.editButton.setOnClickListener(v -> listener.onEditItem(budgetItem));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteItem(budgetItem));

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userRef = FirebaseDatabase.getInstance(Config.DB_URL).getReference("Users").child(currentUser.getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String profileImageUrl = dataSnapshot.child("imageUrl").getValue(String.class);

                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        // Load the profile image using Glide
                        Glide.with(holder.profileImageView.getContext())
                                .load(profileImageUrl)
                                .placeholder(R.drawable.ic_user_placeholder)
                                .circleCrop()
                                .into(holder.profileImageView);
                    } else {
                        // Set default image if no profile image is found
                        holder.profileImageView.setImageResource(R.drawable.ic_user_placeholder);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DB ERROR", databaseError.getMessage());
            }
        });


        if ("Income".equals(budgetItem.getType())) {
            holder.typeTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.income_color));
        } else {
            holder.typeTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.expense_color));
        }

        // Alternate background color
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.item_color_one));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.item_color_two));
        }
    }

    @Override
    public int getItemCount() {
        return budgetItemList.size();
    }

    public static class BudgetViewHolder extends RecyclerView.ViewHolder {

        ImageView profileImageView;
        TextView itemNameTextView;
        TextView amountTextView;
        TextView typeTextView;
        TextView dateTextView;
        ImageButton editButton;
        ImageButton deleteButton;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            amountTextView = itemView.findViewById(R.id.amountTextView);
            typeTextView = itemView.findViewById(R.id.typeTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    public interface BudgetItemListener {
        void onEditItem(BudgetItem budgetItem);
        void onDeleteItem(BudgetItem budgetItem);
    }
}