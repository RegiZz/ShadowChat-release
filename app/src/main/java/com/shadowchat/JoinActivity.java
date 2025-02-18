package com.shadowchat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class JoinActivity extends AppCompatActivity {

    private EditText inviteCodeText;
    private Button joinChatButton;

    private ImageButton btnBack;
    private FirebaseHelper firebaseHelper;
    private SharedPreferencesHelper sharedPreferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        inviteCodeText = findViewById(R.id.chatUIDInput);
        joinChatButton = findViewById(R.id.joinChat);
        btnBack = findViewById(R.id.btnBack);
        firebaseHelper = new FirebaseHelper(this);

        sharedPreferencesHelper = new SharedPreferencesHelper(this);

        btnBack.setOnClickListener(back ->{
            Intent intent = new Intent(JoinActivity.this, UserChatsActivity.class);
            startActivity(intent);
            finish();
        });


        joinChatButton.setOnClickListener(v -> {
            String inviteCode = inviteCodeText.getText().toString().trim();

            if (inviteCode.isEmpty()) {
                Toast.makeText(this, "Wpisz kod!", Toast.LENGTH_SHORT).show();
                return;
            }

            String userID = sharedPreferencesHelper.getUserID();

            firebaseHelper.joinChatByInviteCode(inviteCode, userID, chatID -> {
                if (chatID != null && !isUserInChat(chatID)) {
                    Toast.makeText(this, "Dołączono do czatu!", Toast.LENGTH_SHORT).show();
                    saveUserChatData(chatID);
                    Intent intent = new Intent(JoinActivity.this, ChatActivity.class);
                    intent.putExtra("chatID", chatID);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Niepoprawny kod! Lub już tam jesteś", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private boolean isUserInChat(String chatID) {
        return sharedPreferencesHelper.getBoolean(chatID, false);
    }

    private void saveUserChatData(String chatID) {
        sharedPreferencesHelper.saveBoolean(chatID, true);
    }
}
