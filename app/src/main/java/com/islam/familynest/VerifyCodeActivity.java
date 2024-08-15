package com.islam.familynest;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.islam.familynest.helpers.FirebaseAuthHelper;

public class VerifyCodeActivity extends AppCompatActivity {

    private FirebaseAuthHelper authHelper;

    private EditText codeInput;
    private Button verifyButton;
    private String verificationId, name, email;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_code_activity);

        authHelper = new FirebaseAuthHelper(this);

        codeInput = findViewById(R.id.codeInput);
        verifyButton = findViewById(R.id.verifyButton);

        verificationId = getIntent().getStringExtra("verificationId");
        name = getIntent().getStringExtra("name");
        email = getIntent().getStringExtra("email");

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
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        progressBar.setVisibility(View.GONE);
        verifyButton.setEnabled(true);
        authHelper.signInWithPhoneAuthCredential(credential, name, email);
    }
}
