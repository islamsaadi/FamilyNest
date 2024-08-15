package com.islam.familynest.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.islam.familynest.R;
import com.islam.familynest.models.ShoppingItem;

import java.util.List;

public class ShoppingItemAdapter extends RecyclerView.Adapter<ShoppingItemAdapter.ShoppingItemViewHolder> {
    private List<ShoppingItem> shoppingItemList;
    private ShoppingItemListener listener;

    public interface ShoppingItemListener {
        void onEditItem(ShoppingItem item);
        void onDeleteItem(ShoppingItem item);
        void onMarkItemDone(ShoppingItem item);
    }

    public ShoppingItemAdapter(List<ShoppingItem> shoppingItemList, ShoppingItemListener listener) {
        this.shoppingItemList = shoppingItemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShoppingItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shopping, parent, false);
        return new ShoppingItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingItemViewHolder holder, int position) {
        ShoppingItem shoppingItem = shoppingItemList.get(position);
        holder.nameTextView.setText(shoppingItem.getName());
        holder.costTextView.setText("$" + shoppingItem.getCost());
        holder.doneButton.setChecked(shoppingItem.isDone());

        holder.editButton.setOnClickListener(v -> listener.onEditItem(shoppingItem));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteItem(shoppingItem));
        holder.doneButton.setOnClickListener(v -> listener.onMarkItemDone(shoppingItem));
    }

    @Override
    public int getItemCount() {
        return shoppingItemList.size();
    }

    public static class ShoppingItemViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, costTextView;
        ImageButton editButton, deleteButton;
        CheckBox doneButton;

        public ShoppingItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            costTextView = itemView.findViewById(R.id.costTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            doneButton = itemView.findViewById(R.id.doneButton);
        }
    }
}
