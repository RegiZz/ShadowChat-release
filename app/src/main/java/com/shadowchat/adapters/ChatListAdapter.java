package com.shadowchat.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.shadowchat.R;

import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends BaseAdapter {

    private final Context context;
    private final List<String> chatIDs;
    private final List<String> chatNames;

    public ChatListAdapter(Context context) {
        this.context = context;
        this.chatIDs = new ArrayList<>();
        this.chatNames = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return chatIDs.size();
    }

    @Override
    public Object getItem(int position) {
        return chatIDs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public String getChatID(int position) {
        return chatIDs.get(position);
    }

    public void addChat(String chatID, String chatName) {
        chatIDs.add(chatID);
        chatNames.add(chatName);
        notifyDataSetChanged();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        }

        TextView chatNameTextView = convertView.findViewById(R.id.chatNameTextView);
        chatNameTextView.setText(chatNames.get(position));

        return convertView;
    }

    public void clear() {
        chatIDs.clear();
        chatNames.clear();
        notifyDataSetChanged();
    }
}
