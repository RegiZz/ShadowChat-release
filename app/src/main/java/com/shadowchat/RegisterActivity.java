package com.shadowchat;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private EditText emailInput, passwordInput, nicknameInput;
    private Button registerButton;

    private Button loginButton;
    private FirebaseHelper firebaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        nicknameInput = findViewById(R.id.nicknameInput);
        registerButton = findViewById(R.id.registerButton);
        loginButton = findViewById(R.id.loginThatRegister);

        firebaseHelper = new FirebaseHelper(this);

        registerButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();
            String nickname = nicknameInput.getText().toString();

            firebaseHelper.registerUser(email, password, nickname, success -> {
                if (success) {
                    startActivity(new Intent(RegisterActivity.this, UserChatsActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Rejestracja nieudana!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        loginButton.setOnClickListener(l ->{
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        });


    }
}
