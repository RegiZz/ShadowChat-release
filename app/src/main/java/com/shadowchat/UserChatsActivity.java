package com.shadowchat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.shadowchat.adapters.ChatListAdapter;
import com.shadowchat.utils.CryptoHelper;

import javax.crypto.SecretKey;

public class UserChatsActivity extends AppCompatActivity {

    private FirebaseHelper firebaseHelper;
    private SharedPreferencesHelper sharedPreferencesHelper;
    private ListView chatListView;
    private ChatListAdapter chatListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chats);

        firebaseHelper = new FirebaseHelper(this);
        sharedPreferencesHelper = new SharedPreferencesHelper(this);
        chatListView = findViewById(R.id.chatListView);
        chatListAdapter = new ChatListAdapter(this);
        chatListView.setAdapter(chatListAdapter);

        String userID = sharedPreferencesHelper.getUserID();
        if (userID != null) {
            firebaseHelper.getChatList(userID, new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    chatListAdapter.clear();

                    new Handler().postDelayed(() -> {

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String chatID = snapshot.getKey();
                            String chatName = snapshot.child("name").getValue(String.class);
                            chatListAdapter.addChat(chatID, chatName);
                        }
                    }, 50);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(UserChatsActivity.this, "Błąd pobierania czatów", Toast.LENGTH_SHORT).show();
                }
            });
        }

        Button createChatButton = findViewById(R.id.createChatButton);
        createChatButton.setOnClickListener(view -> {
            String chatName = "Chat_" + System.currentTimeMillis();
            String chatID = "chat_" + System.currentTimeMillis();

            firebaseHelper.createChat(chatID, chatName, userID);
            Toast.makeText(this, "Chat stworzony!", Toast.LENGTH_SHORT).show();

            SecretKey chatKey = null;
            try {
                chatKey = CryptoHelper.generateKey();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            String encodedKey = CryptoHelper.keyToString(chatKey);

            firebaseHelper.saveChatKey(chatID, encodedKey);

        });

        Button joinChatButton = findViewById(R.id.joinChatButton);
        joinChatButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, JoinActivity.class);
            startActivity(intent);
            finish();
        });



        chatListView.setOnItemClickListener((parent, view, position, id) -> {
            String chatID = chatListAdapter.getChatID(position);
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("chatID", chatID);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
        });
    }
}