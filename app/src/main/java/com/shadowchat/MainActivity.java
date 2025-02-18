package com.shadowchat;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private SharedPreferencesHelper sharedPreferencesHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        FirebaseApp.initializeApp(this);

        super.onCreate(savedInstanceState);

        sharedPreferencesHelper = new SharedPreferencesHelper(this);

        String userID = sharedPreferencesHelper.getUserID();
        if (userID == null) {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(this, UserChatsActivity.class);
            startActivity(intent);
            finish();
        }
    }
}