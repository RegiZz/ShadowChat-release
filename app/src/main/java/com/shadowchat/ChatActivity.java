package com.shadowchat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.shadowchat.adapters.ChatMessageAdapter;
import com.shadowchat.models.ChatMessage;
import com.shadowchat.utils.CryptoHelper;

public class ChatActivity extends AppCompatActivity {

    private FirebaseHelper firebaseHelper;
    private SharedPreferencesHelper sharedPreferencesHelper;
    private String chatID;
    private ListView messagesListView;
    private EditText messageInput;
    private Button sendButton;
    private ChatMessageAdapter chatMessageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        firebaseHelper = new FirebaseHelper(this);
        sharedPreferencesHelper = new SharedPreferencesHelper(this);

        chatID = getIntent().getStringExtra("chatID");
        messagesListView = findViewById(R.id.messagesListView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);


        chatMessageAdapter = new ChatMessageAdapter(this, chatID);
        messagesListView.setAdapter(chatMessageAdapter);
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton settingsBtn = findViewById(R.id.settingsBtn);

        settingsBtn.setOnClickListener(s -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("chatID", chatID);
            startActivity(intent);
        });


        btnBack.setOnClickListener(v -> {
            finish();
        });


        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (messageText.isEmpty()) {
                return;
            }

            String userID = sharedPreferencesHelper.getUserID();

            // Pobranie klucza szyfrowania z Firebase
            firebaseHelper.getChatEncryptionKey(chatID, encryptionKey -> {
                if (encryptionKey == null) {
                    Toast.makeText(this, "Błąd: brak klucza szyfrowania!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Pobranie danych użytkownika
                firebaseHelper.getUser(userID, user -> {
                    if (user == null) {
                        Toast.makeText(this, "Nie znaleziono użytkownika z id"+userID, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        // Szyfrowanie wiadomości
                        String encryptedMessage = CryptoHelper.encrypt(messageText, encryptionKey);

                        String nickname = user.getNickname();

                        ChatMessage chatMessage = new ChatMessage(userID, nickname, encryptedMessage);

                        // Wysłanie wiadomości do Firebase
                        firebaseHelper.sendMessage(chatID, chatMessage);
                        messageInput.setText("");

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Błąd szyfrowania wiadomości", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        firebaseHelper.fetchMessages(chatID, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chatMessageAdapter.clear();

                firebaseHelper.getChatEncryptionKey(chatID, encryptionKey -> {
                    if (encryptionKey == null) {
                        return;
                    }

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);

                        if (chatMessage != null) {
                            try {
                                String decryptedMessage = CryptoHelper.decrypt(chatMessage.getMessage(), encryptionKey);
                                chatMessage.setMessage(decryptedMessage);
                            } catch (Exception e) {
                                e.printStackTrace();
                                chatMessage.setMessage("Błąd odszyfrowania wiadomości");
                            }
                            chatMessageAdapter.addMessage(chatMessage);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, "Błąd ładowania wiadomości", Toast.LENGTH_SHORT).show();
            }
        });
    }
}