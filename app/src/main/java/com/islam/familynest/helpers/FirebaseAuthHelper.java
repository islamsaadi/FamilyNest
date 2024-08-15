package com.islam.familynest.helpers;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.islam.familynest.MainActivity;
import com.islam.familynest.SignupActivity;
import com.islam.familynest.configs.Config;
import com.islam.familynest.models.User;
import java.util.concurrent.TimeUnit;

public class FirebaseAuthHelper {

    private final FirebaseAuth mAuth;
    private final DatabaseReference dbRef;
    private final Activity activity;

    public FirebaseAuthHelper(Activity activity) {
        this.mAuth = FirebaseAuth.getInstance();
        this.dbRef = FirebaseDatabase.getInstance(Config.DB_URL).getReference("Users");
        this.activity = activity;
    }

    public void sendVerificationCode(String phoneNumber, String name, String email, PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public void checkAuthUserSavedInDB(String uid, String phone) {
        dbRef.child(uid).get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    // User already exists, navigate to main screen
                    navigateToMain();
                } else {
                    navigateToSignUp(phone);
                }
            }
        });
    }

    public void signInWithPhoneAuthCredential(PhoneAuthCredential credential, String name, String email) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        if (user != null) {

                            String uid = user.getUid();

                            // Check if the user already exists in the database
                            dbRef.child(uid).get().addOnCompleteListener(task1 -> {

                                if (task1.isSuccessful()) {

                                    if (task1.getResult().exists()) {
                                        // User already exists, navigate to main screen
                                        navigateToMain();
                                    } else {
                                        // New user, save their information
                                        if (name != null && email != null && !name.isEmpty() && !email.isEmpty()) {

                                            User newUser = new User(name, email, 0, "");
                                            dbRef.child(uid).setValue(newUser)
                                                    .addOnCompleteListener(task2 -> {
                                                        if (task2.isSuccessful()) {
                                                            navigateToMain();
                                                            Toast.makeText(activity, "User data saved successfully", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }).addOnFailureListener(e -> {
                                                        // Write failed
                                                        Toast.makeText(activity, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        e.printStackTrace();
                                                    });
                                        } else {
                                            // Missing user details, go to signup page
                                            navigateToSignUp(user.getPhoneNumber());
                                            Toast.makeText(activity, "User details missing", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } else {
                                    Toast.makeText(activity, "Failed to check user existence", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(e -> {
                                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();

                            });
                        }
                    } else {
                        Toast.makeText(activity, "Sign-in failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToSignUp(String phone) {
        Intent intent = new Intent(activity, SignupActivity.class);
        intent.putExtra("PHONE", phone);
        activity.startActivity(intent);
        activity.finish();
    }

    private void navigateToMain() {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }
}
