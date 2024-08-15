package com.islam.familynest.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.islam.familynest.R;
import com.islam.familynest.fragments.ProfileFragment;
import com.islam.familynest.models.FamilyMember;

import java.util.List;

public class FamilyMemberAdapter extends RecyclerView.Adapter<FamilyMemberAdapter.FamilyMemberViewHolder> {

    private List<FamilyMember> familyMembers;
    private ProfileFragment profileFragment;

    public FamilyMemberAdapter(List<FamilyMember> familyMemberList, ProfileFragment profileFragment) {
        this.familyMembers = familyMemberList;
        this.profileFragment = profileFragment;
    }

    @NonNull
    @Override
    public FamilyMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_family_member, parent, false);
        return new FamilyMemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FamilyMemberViewHolder holder, int position) {

        FamilyMember member = familyMembers.get(position);
        Log.d("FamilyMemberAdapter", "Binding family member: " + member.getName() + ", Rewards: " + member.getRewards());
        holder.nameTextView.setText(member.getName());
        holder.rewardsTextView.setText("Rewards: " + member.getRewards());
        holder.editButton.setOnClickListener(v -> profileFragment.showAddEditFamilyMemberDialog(member));
        holder.deleteButton.setOnClickListener(v -> profileFragment.deleteFamilyMember(member));
    }

    @Override
    public int getItemCount() {
        Log.d("FamilyMemberAdapter", "getItemCount: " + familyMembers.size());
        return familyMembers.size();
    }

    public static class FamilyMemberViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, rewardsTextView;
        ImageButton editButton, deleteButton;

        public FamilyMemberViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            rewardsTextView = itemView.findViewById(R.id.rewardsTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
