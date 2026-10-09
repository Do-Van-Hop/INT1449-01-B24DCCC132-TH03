package com.example.bai1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bai1.database.DatabaseHelper;
import com.example.bai1.model.Note;
import com.google.android.material.button.MaterialButton;

public class NoteDetailActivity extends AppCompatActivity {

    public static final String EXTRA_NOTE_ID = "extra_note_id";

    private ImageButton btnBack;
    private ImageButton btnHeaderEdit;
    private ImageButton btnHeaderDelete;
    private TextView tvDetailTitle;
    private TextView tvDetailId;
    private TextView tvDetailCreatedAt;
    private TextView tvDetailUpdatedAt;
    private TextView tvDetailContent;
    private MaterialButton btnBottomEdit;
    private MaterialButton btnBottomDelete;

    private DatabaseHelper databaseHelper;
    private long noteId = -1;
    private Note note;

    public static Intent newIntent(Context context, long noteId) {
        Intent intent = new Intent(context, NoteDetailActivity.class);
        intent.putExtra(EXTRA_NOTE_ID, noteId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_note_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);

        if (getIntent() != null) {
            noteId = getIntent().getLongExtra(EXTRA_NOTE_ID, -1);
        }

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNoteDetails();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnHeaderEdit = findViewById(R.id.btnHeaderEdit);
        btnHeaderDelete = findViewById(R.id.btnHeaderDelete);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailId = findViewById(R.id.tvDetailId);
        tvDetailCreatedAt = findViewById(R.id.tvDetailCreatedAt);
        tvDetailUpdatedAt = findViewById(R.id.tvDetailUpdatedAt);
        tvDetailContent = findViewById(R.id.tvDetailContent);
        btnBottomEdit = findViewById(R.id.btnBottomEdit);
        btnBottomDelete = findViewById(R.id.btnBottomDelete);
    }

    private void loadNoteDetails() {
        if (noteId == -1) {
            Toast.makeText(this, R.string.msg_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        note = databaseHelper.getNote(noteId);
        if (note == null) {
            Toast.makeText(this, R.string.msg_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvDetailTitle.setText(note.getTitle());
        tvDetailId.setText(getString(R.string.id_format, note.getId()));
        tvDetailCreatedAt.setText(note.getFormattedCreatedAt());
        tvDetailUpdatedAt.setText(note.getFormattedUpdatedAt());
        tvDetailContent.setText(note.getContent());
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnHeaderEdit.setOnClickListener(v -> openEditScreen());
        btnBottomEdit.setOnClickListener(v -> openEditScreen());
        btnHeaderDelete.setOnClickListener(v -> confirmDeleteNote());
        btnBottomDelete.setOnClickListener(v -> confirmDeleteNote());
    }

    private void openEditScreen() {
        if (note != null) {
            startActivity(AddEditNoteActivity.newIntentForEdit(this, note.getId()));
        }
    }

    private void confirmDeleteNote() {
        if (note == null) return;

        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(getString(R.string.confirm_delete_msg) + "\n\n\"" + note.getTitle() + "\"")
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    databaseHelper.deleteNote(note.getId());
                    Toast.makeText(NoteDetailActivity.this, R.string.msg_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
