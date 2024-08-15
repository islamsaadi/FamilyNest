package com.islam.familynest;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.islam.familynest.helpers.FirebaseAuthHelper;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuthHelper authHelper;

    private EditText phoneNumberInput, nameInput, emailInput;
    private Button signupButton;

    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        authHelper = new FirebaseAuthHelper(this);

        phoneNumberInput = findViewById(R.id.phoneNumberInput);
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        signupButton = findViewById(R.id.signupButton);

        progressBar = findViewById(R.id.progressBar);


        Intent intent = getIntent();
        String phone = intent.getStringExtra("PHONE");
        if(phone != null)
            phoneNumberInput.setText(phone);

        signupButton.setOnClickListener(v -> {
            String phoneNumber = phoneNumberInput.getText().toString().trim();
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();

            if (TextUtils.isEmpty(phoneNumber) || !phoneNumber.startsWith("+")) {
                Toast.makeText(SignupActivity.this, "Please enter a valid phone number with country code.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(SignupActivity.this, "Please enter your name.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(SignupActivity.this, "Please enter your email.", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            signupButton.setEnabled(false);


            // Proceed with phone number verification
            authHelper.sendVerificationCode(phoneNumber, name, email, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    progressBar.setVisibility(View.GONE);
                    signupButton.setEnabled(true);
                    authHelper.signInWithPhoneAuthCredential(credential, name, email);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    progressBar.setVisibility(View.GONE);
                    signupButton.setEnabled(true);
                    Toast.makeText(SignupActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    progressBar.setVisibility(View.GONE);
                    signupButton.setEnabled(true);
                    Intent intent = new Intent(SignupActivity.this, VerifyCodeActivity.class);
                    intent.putExtra("verificationId", verificationId);
                    intent.putExtra("name", name);
                    intent.putExtra("email", email);
                    startActivity(intent);
                }
            });
        });
    }
}
