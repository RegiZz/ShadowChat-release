package com.shadowchat;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shadowchat.models.ChatMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class FirebaseHelper {

    private final DatabaseReference databaseReference;

    private final FirebaseAuth auth;

    private SharedPreferencesHelper sharedPreferencesHelper;

    public FirebaseHelper(Context context) {
        String databaseUrl = BuildConfig.FIREBASE_DATABASE_URL;
        FirebaseDatabase database = FirebaseDatabase.getInstance(databaseUrl);
        this.databaseReference = database.getReference();
        this.sharedPreferencesHelper = new SharedPreferencesHelper(context);
        this.auth = FirebaseAuth.getInstance();
    }

    public void sendMessage(String chatID, ChatMessage message) {
        databaseReference.child("chats").child(chatID).push().setValue(message);
    }

    public void fetchMessages(String chatID, ValueEventListener listener) {
        databaseReference.child("chats").child(chatID).addValueEventListener(listener);
    }

    public void createChat(String chatID, String chatName, String userID) {
        HashMap<String, Object> chatData = new HashMap<>();
        chatData.put("name", chatName);
        chatData.put("creator", userID);

        HashMap<String, Boolean> members = new HashMap<>();
        members.put(userID, true);
        chatData.put("members", members);

        databaseReference.child("chatList").child(chatID).setValue(chatData);
    }

    public void addUserToChat(String chatID, String userID, OnUserAddedListener listener) {
        databaseReference.child("chats").child(chatID).child("members").child(userID).setValue(true)
                .addOnSuccessListener(aVoid -> listener.onUserAdded(true))
                .addOnFailureListener(e -> listener.onUserAdded(false));

        databaseReference.child("chatList").child(chatID).child("members").child(userID).setValue(true);
    }

    public void getChatList(String userID, ValueEventListener listener) {
        databaseReference.child("chatList").orderByChild("creator").equalTo(userID)
                .addValueEventListener(listener);

        databaseReference.child("chats").orderByChild("members" + userID).equalTo(true)
                .addListenerForSingleValueEvent(listener);
    }

    public void registerUser(String email, String password, String nickname, OnUserRegisteredListener listener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            saveUserToDatabase("user_"+user.getUid(), nickname);
                            sharedPreferencesHelper.saveUserID("user_"+user.getUid());
                            listener.onUserRegistered(true);
                        } else {
                            listener.onUserRegistered(false);
                        }
                    } else {
                        listener.onUserRegistered(false);
                    }
                });
    }

    public void loginUser(String email, String password, OnUserLoginListener listener) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        assert user != null;
                        sharedPreferencesHelper.saveUserID("user_"+user.getUid());
                        listener.onUserLogin(true);
                    } else {
                        listener.onUserLogin(false);
                    }
                });
    }

    public void getUser(String userID, OnUserRetrievedListener listener) {
        databaseReference.child("users").child(userID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class); // Zamiana na obiekt klasy User
                            listener.onUserRetrieved(user);
                        } else {
                            listener.onUserRetrieved(null);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onUserRetrieved(null);
                    }
                });
    }

    public void getChatMembersWithNicknames(String chatID, OnChatMembersWithNicknamesListener listener) {
        databaseReference.child("chatList").child(chatID).child("members")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Log.e("FirebaseHelper", "Brak 'members' w czacie: " + chatID);
                            listener.onChatMembersWithNicknames(null);
                            return;
                        }

                        Log.d("FirebaseHelper", "Pobrano members dla chatID: " + chatID);

                        HashMap<String, String> membersMap = new HashMap<>();
                        List<String> userIds = new ArrayList<>();

                        for (DataSnapshot child : snapshot.getChildren()) {
                            String userID = child.getKey();
                            Log.d("FirebaseHelper", "Znaleziono userID: " + userID);
                            userIds.add(userID);
                        }

                        if (userIds.isEmpty()) {
                            Log.e("FirebaseHelper", "Brak użytkowników w czacie: " + chatID);
                            listener.onChatMembersWithNicknames(null);
                            return;
                        }

                        for (String userID : userIds) {
                            databaseReference.child("users").child(userID).child("nickname")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                            if (userSnapshot.exists()) {
                                                String nickname = userSnapshot.getValue(String.class);
                                                Log.d("FirebaseHelper", "Pobrano nick: " + nickname + " dla userID: " + userID);
                                                membersMap.put(userID, nickname);
                                            } else {
                                                Log.e("FirebaseHelper", "Brak nicka dla userID: " + userID);
                                            }

                                            // Kiedy pobrano wszystkie nicki, zwracamy wynik
                                            if (membersMap.size() == userIds.size()) {
                                                listener.onChatMembersWithNicknames(membersMap);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.e("FirebaseHelper", "Błąd pobierania nicka: " + error.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseHelper", "Błąd pobierania członków czatu: " + error.getMessage());
                        listener.onChatMembersWithNicknames(null);
                    }
                });
    }



    // Zapisz klucz AES w Firebase
    public void saveChatKey(String chatID, String key) {
        databaseReference.child("chats").child(chatID).child("key").setValue(key);
    }

    // Pobierz klucz AES z Firebase
    public void getChatEncryptionKey(String chatID, OnKeyRetrievedListener listener) {
        databaseReference.child("chats").child(chatID).child("key")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            try {
                                // Pobranie klucza jako Base64
                                String encodedKey = snapshot.getValue(String.class);

                                // Dekodowanie Base64 do bajtów
                                byte[] decodedKey = Base64.decode(encodedKey, Base64.DEFAULT);

                                // Konwersja do SecretKey
                                SecretKey secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

                                listener.onKeyRetrieved(secretKey);
                            } catch (Exception e) {
                                e.printStackTrace();
                                listener.onKeyRetrieved(null);
                            }
                        } else {
                            listener.onKeyRetrieved(null);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        listener.onKeyRetrieved(null);
                    }
                });
    }

    public void updateChatName(String chatID, String newChatName, OnUpdateChatNameListener listener) {
        DatabaseReference chatRef = databaseReference.child("chatList").child(chatID);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Jeśli czat istnieje, aktualizujemy nazwę
                    HashMap<String, Object> updateData = new HashMap<>();
                    updateData.put("name", newChatName);

                    chatRef.updateChildren(updateData)
                            .addOnSuccessListener(aVoid -> listener.onUpdateChatName(true))
                            .addOnFailureListener(e -> listener.onUpdateChatName(false));
                } else {
                    // Jeśli czat nie istnieje, zwracamy false
                    listener.onUpdateChatName(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onUpdateChatName(false);
            }
        });
    }

    public void canEditChatName(String chatID, String userID, OnPermissionCheckListener listener) {
        databaseReference.child("chatList").child(chatID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    listener.onPermissionCheck(false);
                    return;
                }

                String creatorID = snapshot.child("creator").getValue(String.class);
                boolean isModerator = snapshot.child("moderators").child(userID).exists();

                if (userID.equals(creatorID) || isModerator) {
                    listener.onPermissionCheck(true);
                } else {
                    listener.onPermissionCheck(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                listener.onPermissionCheck(false);
            }
        });
    }

    public void whoIsOwner(String chatID, OnOwnerRetrievedListener listener) {
        databaseReference.child("chatList").child(chatID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String creatorID = snapshot.child("creator").getValue(String.class);
                    listener.onOwnerRetrieved(creatorID);
                } else {
                    listener.onOwnerRetrieved(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onOwnerRetrieved(null);
            }
        });
    }

    public void generateInviteCode(String chatID, OnInviteCodeGeneratedListener listener) {
        String inviteCode = generateRandomCode(6); // 6-znakowy kod

        databaseReference.child("inviteCodes").child(inviteCode).setValue(chatID)
                .addOnSuccessListener(aVoid -> listener.onCodeGenerated(inviteCode))
                .addOnFailureListener(e -> listener.onCodeGenerated(null));
    }

    private String generateRandomCode(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return code.toString();
    }

    public void joinChatByInviteCode(String inviteCode, String userID, OnChatJoinedListener listener) {
        databaseReference.child("inviteCodes").child(inviteCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String chatID = snapshot.getValue(String.class);

                            if (chatID != null && !isUserInChat(chatID)) {
                                addUserToChat(chatID, userID, success -> {
                                    if (success) {
                                        databaseReference.child("inviteCodes").child(inviteCode).removeValue(); // Usuń kod po użyciu
                                        listener.onChatJoined(chatID);
                                    } else {
                                        listener.onChatJoined(null);
                                    }
                                });
                            } else {
                                listener.onChatJoined(null);
                            }
                        } else {
                            listener.onChatJoined(null);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onChatJoined(null);
                    }
                });
    }
    private void saveUserToDatabase(String userID, String nickname) {
        HashMap<String, String> userData = new HashMap<>();
        userData.put("nickname", nickname);
        databaseReference.child("users").child(userID).setValue(userData);
    }

    private boolean isUserInChat(String chatID){
        return sharedPreferencesHelper.getBoolean(chatID, false);
    }

    public interface OnChatJoinedListener {
        void onChatJoined(String chatID);
    }

    public interface OnInviteCodeGeneratedListener {
        void onCodeGenerated(String code);
    }


    public interface OnOwnerRetrievedListener {
        void onOwnerRetrieved(String ownerID);
    }

    public interface OnPermissionCheckListener {
        void onPermissionCheck(boolean hasPermission);
    }


    public interface OnUpdateChatNameListener {
        void onUpdateChatName(boolean success);
    }

    public interface OnChatMembersWithNicknamesListener {
        void onChatMembersWithNicknames(HashMap<String, String> members);
    }



    public interface OnKeyRetrievedListener {
        void onKeyRetrieved(SecretKey key);
    }

    public interface OnUserAddedListener {
        void onUserAdded(boolean success);
    }

    public interface OnUserRegisteredListener {
        void onUserRegistered(boolean success);
    }

    public interface OnUserLoginListener{
        void onUserLogin(boolean success);
    }

    public interface OnUserRetrievedListener {
        void onUserRetrieved(User user);
    }
}