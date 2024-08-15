package com.islam.familynest.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.islam.familynest.R;
import com.islam.familynest.adapters.FamilyMemberAdapter;
import com.islam.familynest.configs.Config;
import com.islam.familynest.models.FamilyMember;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private RecyclerView familyMembersRecyclerView;
    private FamilyMemberAdapter familyMemberAdapter;
    private List<FamilyMember> familyMemberList;
    private EditText nameEditText, emailEditText;
    private ImageView profileImageView;
    private Button updateProfileButton, addFamilyMemberButton;
    private DatabaseReference userRef;
    private StorageReference storageRef;
    private ProgressBar imageUploadProgressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        nameEditText = view.findViewById(R.id.nameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        profileImageView = view.findViewById(R.id.profileImageView);
        updateProfileButton = view.findViewById(R.id.updateProfileButton);
        addFamilyMemberButton = view.findViewById(R.id.addFamilyMemberButton);
        imageUploadProgressBar = view.findViewById(R.id.imageUploadProgressBar);

        familyMembersRecyclerView = view.findViewById(R.id.familyMembersRecyclerView);
        familyMembersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        familyMemberList = new ArrayList<>();
        familyMemberAdapter = new FamilyMemberAdapter(familyMemberList, this);
        familyMembersRecyclerView.setAdapter(familyMemberAdapter);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance(Config.DB_URL).getReference("Users").child(uid);
            storageRef = FirebaseStorage.getInstance().getReference("profile_images").child(uid);

            // Load the user's profile
            loadUserProfile();
            // Load the family members
            loadFamilyMembers();
        }

        profileImageView.setOnClickListener(v -> openImageChooser());
        updateProfileButton.setOnClickListener(v -> updateProfile());
        addFamilyMemberButton.setOnClickListener(v -> showAddEditFamilyMemberDialog(null));
    }

    private void loadUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                    // Set name and email fields in the screen
                    nameEditText.setText(name);
                    emailEditText.setText(email);

                    // Load profile image if available
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(ProfileFragment.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_user_placeholder)
                                .circleCrop()
                                .into(profileImageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
            Toast.makeText(requireContext(), "Name and Email cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update the name and email in the database
        userRef.child("name").setValue(name);
        userRef.child("email").setValue(email);

        Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show();
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == requireActivity().RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadProfileImage(imageUri);
        }
    }

    private void uploadProfileImage(Uri imageUri) {
        if (imageUri != null) {
            imageUploadProgressBar.setVisibility(View.VISIBLE);
            storageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        userRef.child("imageUrl").setValue(imageUrl);

                        if (isAdded()) {
                            Glide.with(ProfileFragment.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_user_placeholder)
                                    .circleCrop()
                                    .into(profileImageView);
                        }

                        Toast.makeText(requireContext(), "Profile image updated", Toast.LENGTH_SHORT).show();
                        imageUploadProgressBar.setVisibility(View.GONE);
                    }))
                    .addOnFailureListener(e -> {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                            imageUploadProgressBar.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private void loadFamilyMembers() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference familyRef = FirebaseDatabase.getInstance(Config.DB_URL).getReference("FamilyMembers").child(currentUserId);

        familyRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!isAdded()) return;

                familyMemberList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FamilyMember member = snapshot.getValue(FamilyMember.class);
                    if (member != null) {
                        familyMemberList.add(member);
                    }
                }

                familyMemberAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Failed to load family members", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showAddEditFamilyMemberDialog(@Nullable FamilyMember member) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_edit_family_member, null);
        dialogBuilder.setView(dialogView);

        EditText nameEditText = dialogView.findViewById(R.id.nameEditText);
        EditText rewardsEditText = dialogView.findViewById(R.id.rewardsEditText);

        if (member != null) {
            nameEditText.setText(member.getName());
            rewardsEditText.setText(String.valueOf(member.getRewards()));
        }

        dialogBuilder.setTitle(member == null ? "Add Family Member" : "Edit Family Member");
        dialogBuilder.setPositiveButton(member == null ? "Add" : "Update", (dialog, whichButton) -> {
            String name = nameEditText.getText().toString().trim();
            String rewardsString = rewardsEditText.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            int rewards = TextUtils.isEmpty(rewardsString) ? 0 : Integer.parseInt(rewardsString);

            DatabaseReference familyRef = FirebaseDatabase.getInstance(Config.DB_URL).getReference("FamilyMembers").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

            if (member == null) {
                String id = familyRef.push().getKey();
                FamilyMember newMember = new FamilyMember(id, name, rewards);
                if (id != null) {
                    familyRef.child(id).setValue(newMember);
                }
            } else {
                member.setName(name);
                member.setRewards(rewards);
                familyRef.child(member.getUid()).setValue(member);
            }
        });

        dialogBuilder.setNegativeButton("Cancel", (dialog, whichButton) -> dialog.dismiss());

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void deleteFamilyMember(FamilyMember member) {
        DatabaseReference familyRef = FirebaseDatabase.getInstance(Config.DB_URL).getReference("FamilyMembers").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Family Member")
                .setMessage("Are you sure you want to delete " + member.getName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    familyRef.child(member.getUid()).removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(requireContext(), member.getName() + " has been deleted", Toast.LENGTH_SHORT).show();
                            familyMemberList.remove(member);
                            familyMemberAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(requireContext(), "Failed to delete family member", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }
}
