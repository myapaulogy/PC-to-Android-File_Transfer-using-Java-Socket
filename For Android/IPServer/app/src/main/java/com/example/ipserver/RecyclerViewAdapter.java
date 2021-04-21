package com.example.ipserver;

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
    private final ArrayList<FileDataStructure> fileDataStructure;

    public RecyclerViewAdapter(ArrayList<FileDataStructure> fileDataStructure){
        this.fileDataStructure = fileDataStructure;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.pi_file, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.fileName.setText(fileDataStructure.get(position).getFileName());
        holder.recyclerProgressBar.setMax(fileDataStructure.get(position).getMaxProgress());
        holder.recyclerProgressBar.setProgress(fileDataStructure.get(position).getProgress());
        holder.status.setText(fileDataStructure.get(position).getSyncStatus());

        if(fileDataStructure.get(position).getEnableProgress()) {
            holder.recyclerProgressBar.setVisibility(View.VISIBLE);
        } else {
            holder.recyclerProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return fileDataStructure.size();
    }

    public static boolean isClickable = true;
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView fileName;
        private final ProgressBar recyclerProgressBar;
        private final TextView status;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            fileName = view.findViewById(R.id.fileName);
            recyclerProgressBar = view.findViewById(R.id.recyclerProgressBar);
            status = view.findViewById(R.id.status);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(isClickable) {
                int position = getAdapterPosition();

                if (position != RecyclerView.NO_POSITION) {
                    //Toast.makeText(view.getContext(), "Downloading Content: " + position + "  " + fileName.getText().toString(), Toast.LENGTH_SHORT).show();
                    while(MainActivity.getFileIndex() != -1);
                    MainActivity.setFileIndex(position);
                }
            } else {
                Toast.makeText(view.getContext(), "In the progress of downloading Files", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
