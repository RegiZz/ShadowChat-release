package com.shadowchat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.shadowchat.adapters.UserListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    private RecyclerView userListRecyclerView;
    private UserListAdapter userListAdapter;
    private List<String> userNicknames = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        ImageButton btnBack = findViewById(R.id.btnBack);
        EditText chatName = findViewById(R.id.chatNameEditText);
        Button saveChatName = findViewById(R.id.saveChatNameButton);
        Button generateCode = findViewById(R.id.generateInviteCodeBtn);
        TextView inviteCodeTextView = findViewById(R.id.inviteCodeTextView);
        Button copyCode = findViewById(R.id.copyInviteCodeButton);
        userListAdapter = new UserListAdapter(userNicknames);
        userListRecyclerView = findViewById(R.id.userListRecyclerView);
        userListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userListRecyclerView.setAdapter(userListAdapter);
        getUsersInChat();


        btnBack.setOnClickListener(b -> {
            finish();
        });

        FirebaseHelper firebaseHelper = new FirebaseHelper(this);
        SharedPreferencesHelper sharedPreferencesHelper = new SharedPreferencesHelper(this);

        String chatID = getIntent().getStringExtra("chatID");
        String userID = sharedPreferencesHelper.getUserID();

        if (chatID == null || chatID.isEmpty()) {
            Toast.makeText(this, "Błąd: chatID jest pusty!", Toast.LENGTH_SHORT).show();
            finish(); // Zamknij aktywność, jeśli brakuje chatID
            return;
        }

        firebaseHelper.canEditChatName(chatID, userID, hasPermission -> {
            if (hasPermission) {
                saveChatName.setVisibility(View.VISIBLE);
                chatName.setEnabled(true);
            } else {
                saveChatName.setVisibility(View.GONE);
                chatName.setEnabled(false);
            }
        });


        saveChatName.setOnClickListener(n -> {
            String newChatName = chatName.getText().toString().trim();

            if(newChatName.isEmpty()) {
                Toast.makeText(this, "Wprowadź nazwe na którą chcesz zmienić", Toast.LENGTH_LONG).show();
            }
            else{
                saveChatName(newChatName);
            }
        });

        generateCode.setOnClickListener(g -> {
            firebaseHelper.generateInviteCode(chatID, code -> {
                if(code != null){
                    inviteCodeTextView.setText("Kod zaproszenia: " + code);
                    copyCode.setVisibility(View.VISIBLE);
                }
                else{
                    Toast.makeText(this, "Błąd generowania kodu!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        copyCode.setOnClickListener(c -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Invite Code", inviteCodeTextView.getText().toString().replace("Kod zaproszenia: ", ""));
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Kod skopiowany", Toast.LENGTH_SHORT).show();
        });


    }

    private void saveChatName(String newChatName) {
        String chatID = getIntent().getStringExtra("chatID");

        FirebaseHelper firebaseHelper = new FirebaseHelper(this);
        firebaseHelper.updateChatName(chatID, newChatName, success -> {
            if (chatID == null || chatID.isEmpty()) {
                Toast.makeText(SettingsActivity.this, "Błąd podczas aktualizacji nazwy", Toast.LENGTH_SHORT).show();
            }
            else if (success) {
                Toast.makeText(SettingsActivity.this, "Nazwa czatu została zaktualizowana", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(SettingsActivity.this, "Błąd podczas aktualizacji nazwy", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUsersInChat() {
        String chatID = getIntent().getStringExtra("chatID");
        FirebaseHelper firebaseHelper = new FirebaseHelper(this);

        if(chatID == null){
            Log.e("ChatID", "Brak ChatID w Intent");
            return;
        }

        firebaseHelper.getChatMembersWithNicknames(chatID, new FirebaseHelper.OnChatMembersWithNicknamesListener() {
            @Override
            public void onChatMembersWithNicknames(HashMap<String, String> members) {
                if (members != null) {
                    userNicknames.clear();
                    userNicknames.addAll(members.values());

                    userListAdapter.notifyDataSetChanged();
                } else {
                    Log.e("ChatMembers", "Nie udało się pobrać członków czatu");
                }
            }
        });
    }
}