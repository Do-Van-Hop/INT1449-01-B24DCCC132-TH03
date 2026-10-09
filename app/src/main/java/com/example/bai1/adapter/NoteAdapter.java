package com.example.bai1.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bai1.R;
import com.example.bai1.model.Note;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> noteList;
    private final OnNoteClickListener listener;

    public interface OnNoteClickListener {
        void onItemClick(Note note, int position);
        void onEditClick(Note note, int position);
        void onDeleteClick(Note note, int position);
    }

    public NoteAdapter(List<Note> noteList, OnNoteClickListener listener) {
        this.noteList = noteList != null ? noteList : new ArrayList<>();
        this.listener = listener;
    }

    public void setNotes(List<Note> notes) {
        this.noteList = notes != null ? notes : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.bind(note, position, listener);
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNoteIdBadge;
        private final TextView tvTitle;
        private final TextView tvContent;
        private final TextView tvCreatedTime;
        private final TextView tvUpdatedTime;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNoteIdBadge = itemView.findViewById(R.id.tvNoteIdBadge);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvCreatedTime = itemView.findViewById(R.id.tvCreatedTime);
            tvUpdatedTime = itemView.findViewById(R.id.tvUpdatedTime);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Note note, int position, OnNoteClickListener listener) {
            tvNoteIdBadge.setText(itemView.getContext().getString(R.string.id_format, note.getId()));
            tvTitle.setText(note.getTitle());
            tvContent.setText(note.getContent());

            tvCreatedTime.setText("Tạo: " + note.getFormattedCreatedAt());
            tvUpdatedTime.setText("Sửa: " + note.getFormattedUpdatedAt());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(note, position);
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(note, position);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(note, position);
                }
            });
        }
    }
}
