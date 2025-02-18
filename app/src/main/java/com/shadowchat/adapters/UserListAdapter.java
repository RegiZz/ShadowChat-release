package com.shadowchat.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shadowchat.R;

import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {
    private List<String> userNicknames;

    public UserListAdapter(List<String> userNicknames) {
        this.userNicknames = userNicknames;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        String nickname = userNicknames.get(position);
        holder.userNicknameTextView.setText(nickname);
    }

    @Override
    public int getItemCount() {
        return userNicknames.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userNicknameTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userNicknameTextView = itemView.findViewById(R.id.userNicknameTextView);
        }
    }
}

