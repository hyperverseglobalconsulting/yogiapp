package com.example.yogiapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.yogiapp.R;
import com.example.yogiapp.models.ChatMessage;
import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_REQUEST = 0;
    private static final int VIEW_TYPE_RESPONSE = 1;

    private List<ChatMessage> messages = new ArrayList<>();

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        return message.getType() == ChatMessage.Type.REQUEST ? VIEW_TYPE_REQUEST : VIEW_TYPE_RESPONSE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_REQUEST) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_snapshot, parent, false);
            return new RequestViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_response, parent, false);
            return new ResponseViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder instanceof RequestViewHolder) {
            ((RequestViewHolder) holder).tvSnapshot.setText(message.getMessage());
        } else if (holder instanceof ResponseViewHolder) {
            ((ResponseViewHolder) holder).tvResponse.setText(message.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvSnapshot;
        RequestViewHolder(View itemView) {
            super(itemView);
            tvSnapshot = itemView.findViewById(R.id.tvSnapshot);
        }
    }

    static class ResponseViewHolder extends RecyclerView.ViewHolder {
        TextView tvResponse;
        ResponseViewHolder(View itemView) {
            super(itemView);
            tvResponse = itemView.findViewById(R.id.tvResponse);
        }
    }
}
