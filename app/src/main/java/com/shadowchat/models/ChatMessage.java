package com.shadowchat.models;


public class ChatMessage {

    private String userID;
    private String nickname;
    private String message;

    public ChatMessage() {
    }

    public ChatMessage(String userID, String nickname, String message) {
        this.userID = userID;
        this.nickname = nickname;
        this.message = message;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
