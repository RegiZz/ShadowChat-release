package com.shadowchat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.shadowchat.FirebaseHelper;
import com.shadowchat.R;
import com.shadowchat.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatMessageAdapter extends BaseAdapter {

    private final Context context;
    private final List<ChatMessage> messages;
    private String creatorID = null;

    public ChatMessageAdapter(Context context, String chatID) {
        this.context = context;
        this.messages = new ArrayList<>();

        // Pobieramy właściciela czatu
        FirebaseHelper firebaseHelper = new FirebaseHelper(context);
        firebaseHelper.whoIsOwner(chatID, new FirebaseHelper.OnOwnerRetrievedListener() {
            @Override
            public void onOwnerRetrieved(String owner) {
                creatorID = owner;
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyDataSetChanged();
    }

    public void clear() {
        messages.clear();
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        }

        TextView messageTextView = convertView.findViewById(R.id.messageTextView);
        TextView nicknameTextView = convertView.findViewById(R.id.nicknameTextView);
        ImageView ownerIcon = convertView.findViewById(R.id.ownerIcon);

        ChatMessage message = messages.get(position);
        messageTextView.setText(message.getMessage());
        nicknameTextView.setText(message.getNickname());

        if (creatorID != null && message.getUserID().equals(creatorID)) {
            ownerIcon.setVisibility(View.VISIBLE);
        } else {
            ownerIcon.setVisibility(View.GONE);
        }

        return convertView;
    }
}
