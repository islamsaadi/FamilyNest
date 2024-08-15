package com.islam.familynest;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.islam.familynest.helpers.FirebaseAuthHelper;
import com.google.firebase.auth.PhoneAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuthHelper authHelper;
    private EditText phoneNumberInput;
    private Button loginButton;
    private TextView signupTextView;

    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authHelper = new FirebaseAuthHelper(this);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // If the user is already signed in, redirect to MainActivity
            authHelper.checkAuthUserSavedInDB(currentUser.getUid(), currentUser.getPhoneNumber());

        } else {
            setContentView(R.layout.activity_login);

            phoneNumberInput = findViewById(R.id.phoneNumberInput);
            loginButton = findViewById(R.id.loginButton);
            signupTextView = findViewById(R.id.signupTextView);

            progressBar = findViewById(R.id.progressBar);


            loginButton.setOnClickListener(v -> {
                String phoneNumber = phoneNumberInput.getText().toString().trim();

                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(LoginActivity.this, "Please enter your phone number.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Add country code if needed
                if (!phoneNumber.startsWith("+")) {
                    Toast.makeText(LoginActivity.this, "Please include your country code, starting with '+'.", Toast.LENGTH_LONG).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                loginButton.setEnabled(false);

                // Use FirebaseAuthHelper to send verification code
                authHelper.sendVerificationCode(phoneNumber, null, null, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        progressBar.setVisibility(View.GONE);
                        loginButton.setEnabled(true);
                        authHelper.signInWithPhoneAuthCredential(credential, "", "");
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        progressBar.setVisibility(View.GONE);
                        loginButton.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        progressBar.setVisibility(View.GONE);
                        loginButton.setEnabled(true);
                        Intent intent = new Intent(LoginActivity.this, VerifyCodeActivity.class);
                        intent.putExtra("verificationId", verificationId);
                        startActivity(intent);
                    }
                });
            });

            signupTextView.setOnClickListener(v -> {
                // Navigate to SignupActivity
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}
