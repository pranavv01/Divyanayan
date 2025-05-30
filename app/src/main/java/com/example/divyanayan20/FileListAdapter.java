package com.example.divyanayan20;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder> {

    private final List<String> items;
    private final OnItemClickListener listener;
    private final boolean isDirectoryMode;

    public interface OnItemClickListener {
        void onItemClick(String itemPathOrName);
    }

    public FileListAdapter(List<String> items, OnItemClickListener listener, boolean isDirectoryMode) {
        this.items = items;
        this.listener = listener;
        this.isDirectoryMode = isDirectoryMode;
    }

    @NonNull
    @Override
    public FileListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileListAdapter.ViewHolder holder, int position) {
        String item = items.get(position);
        String displayName = isDirectoryMode ? item : item.substring(item.lastIndexOf("/") + 1);
        holder.textView.setText(displayName);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.textViewItem);
        }
    }
}
