package com.islam.familynest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.islam.familynest.configs.Config;
import com.islam.familynest.fragments.BudgetFragment;
import com.islam.familynest.fragments.ProfileFragment;
import com.islam.familynest.fragments.ShoppingListFragment;
import com.islam.familynest.fragments.TaskFragment;

public class MainActivity extends AppCompatActivity {

    private TextView userNameTextView;
    private TextView userRewardsTextView;
    private ImageView profileImageView;
    private ImageView logoImageView;
    private DatabaseReference userRef;

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        userNameTextView = findViewById(R.id.userNameTextView);
        userRewardsTextView = findViewById(R.id.userRewardsTextView);
        profileImageView = findViewById(R.id.profileImageView);
        logoImageView = findViewById(R.id.logoImageView);

        logoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AboutUsActivity.class);
                startActivity(intent);
            }
        });

        fragmentManager = getSupportFragmentManager();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance(Config.DB_URL).getReference("Users").child(uid);

            // Fetch user information from Firebase Realtime Database
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String userName = dataSnapshot.child("name").getValue(String.class);
                        Integer userRewards = dataSnapshot.child("rewards").getValue(Integer.class);
                        String profileImageUrl = dataSnapshot.child("imageUrl").getValue(String.class);

                        // Update the UI with the user's name and rewards
                        userNameTextView.setText(userName != null ? userName : "User");
                        userRewardsTextView.setText("Rewards: $" + (userRewards != null ? userRewards : 0));

                        // Load the profile image using Glide
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(MainActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.ic_user_placeholder)
                                    .circleCrop()
                                    .into(profileImageView);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("DB ERROR", databaseError.getMessage());
                }
            });
        }

        // Listener for navigation item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_budget) {
                selectedFragment = new BudgetFragment();
            } else if (itemId == R.id.nav_tasks) {
                selectedFragment = new TaskFragment();
            } else if (itemId == R.id.nav_shopping) {
                selectedFragment = new ShoppingListFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                replaceFragment(selectedFragment, itemId);
            }

            return true;
        });

        // Initial fragment
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_budget);
        }
    }

    private void replaceFragment(Fragment fragment, int itemId) {
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
            // If the fragment is already displayed, do nothing
            return;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, String.valueOf(itemId));
        transaction.commitAllowingStateLoss();  // to avoid crashes
    }

    public void updateProfileImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_user_placeholder)
                    .circleCrop()
                    .into(profileImageView);
        }
    }
}
