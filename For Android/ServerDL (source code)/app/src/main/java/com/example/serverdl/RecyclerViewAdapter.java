package com.example.serverdl;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private final ArrayList<ServerFilesDataStructure> files;

    public RecyclerViewAdapter(ArrayList<ServerFilesDataStructure> fileDataStructure){
        this.files = fileDataStructure;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.server_file_view, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.fileName.setText(files.get(position).getFileName());
        holder.progressBar.setMax(100);
        holder.progressBar.setProgress(files.get(position).getProgress());
        holder.status.setText(files.get(position).getSyncStatus());

        if(files.get(position).getEnableProgress()) {
            holder.progressBar.setVisibility(View.VISIBLE);
        } else {
            holder.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView fileName;
        private final ProgressBar progressBar;
        private final TextView status;

        public ViewHolder(View view) {
            super(view);

            fileName = view.findViewById(R.id.CARD_fileName);
            progressBar = view.findViewById(R.id.CARD_fileProgressBar);
            status = view.findViewById(R.id.CARD_status);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // User clicked a file and wants to download or go into the Dir.
            // Toast.makeText(view.getContext(),"You want " + getAdapterPosition() + " its yours my friend", Toast.LENGTH_LONG).show();

            synchronized (MainActivity.filesRequested) {
                MainActivity.filesRequested.add(getAdapterPosition());
                MainActivity.filesRequested.notify();
            }
        }
    }
}
