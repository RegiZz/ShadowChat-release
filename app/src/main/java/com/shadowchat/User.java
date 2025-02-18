package com.shadowchat;

public class User {
    private String nickname;

    // Konstruktor domyślny wymagany przez Firebase
    public User() {}

    public User(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}