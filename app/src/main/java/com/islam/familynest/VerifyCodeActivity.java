package com.islam.familynest;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.islam.familynest.helpers.FirebaseAuthHelper;

import java.util.concurrent.TimeUnit;

public class VerifyCodeActivity extends AppCompatActivity {

    private static final long RESEND_TIMEOUT = 30000; // 30 seconds

    private FirebaseAuthHelper authHelper;
    private TextView phoneNumberTextView;
    private EditText codeInput;
    private Button verifyButton, backToLastActivity, resendCodeButton;
    private String verificationId, name, email, phoneNumber;
    private ProgressBar progressBar;
    private PhoneAuthProvider.ForceResendingToken resendToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_code_activity);

        authHelper = new FirebaseAuthHelper(this);

        codeInput = findViewById(R.id.codeInput);
        verifyButton = findViewById(R.id.verifyButton);
        backToLastActivity = findViewById((R.id.backToLastActivity));
        resendCodeButton = findViewById(R.id.resendCodeButton);
        phoneNumberTextView = findViewById(R.id.phoneNumberTextView);


        verificationId = getIntent().getStringExtra("verificationId");
        name = getIntent().getStringExtra("name");
        email = getIntent().getStringExtra("email");
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        resendToken = getIntent().getParcelableExtra("resendToken");

        phoneNumberTextView.setText("Sending code to: " + phoneNumber);

        progressBar = findViewById(R.id.progressBar);


        verifyButton.setOnClickListener(v -> {
            String code = codeInput.getText().toString().trim();
            if (TextUtils.isEmpty(code)) {
                Toast.makeText(VerifyCodeActivity.this, "Please enter the verification code.", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            verifyButton.setEnabled(false);

            verifyCode(code);
        });

        startResendCountdown();

        backToLastActivity.setOnClickListener(v -> finish()); // return to previuos activity

        resendCodeButton.setOnClickListener(v -> {
            resendVerificationCode();
            startResendCountdown(); // Disable button again and start countdown
        });

    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        progressBar.setVisibility(View.GONE);
        verifyButton.setEnabled(true);
        authHelper.signInWithPhoneAuthCredential(credential, name, email);
    }

    private void startResendCountdown() {
        resendCodeButton.setEnabled(false);
        resendCodeButton.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.resend_green_disabled));
        new CountDownTimer(RESEND_TIMEOUT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                resendCodeButton.setText("Resend Code (" + millisUntilFinished / 1000 + "s)");
            }

            @Override
            public void onFinish() {
                resendCodeButton.setText("Resend Code");
                resendCodeButton.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.resend_green_enabled));
                resendCodeButton.setEnabled(true);
            }
        }.start();
    }

    private void resendVerificationCode() {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber(phoneNumber)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                // Activity (for callback binding)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        Toast.makeText(VerifyCodeActivity.this, "Verification completed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(VerifyCodeActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String newVerificationId, @NonNull PhoneAuthProvider.ForceResendingToken newToken) {
                        Toast.makeText(VerifyCodeActivity.this, "Code sent successfully", Toast.LENGTH_SHORT).show();
                        verificationId = newVerificationId;
                        resendToken = newToken;
                    }
                })
                .setForceResendingToken(resendToken) // Pass the token here to resend the code
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }


}
