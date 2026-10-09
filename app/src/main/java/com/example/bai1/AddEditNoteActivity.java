package com.example.bai1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bai1.database.DatabaseHelper;
import com.example.bai1.model.Note;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddEditNoteActivity extends AppCompatActivity {

    public static final String EXTRA_NOTE_ID = "extra_note_id";

    private ImageButton btnBack;
    private TextView tvScreenTitle;
    private MaterialButton btnHeaderSave;
    private MaterialButton btnBottomSave;
    private TextInputEditText etTitle;
    private TextInputEditText etContent;

    private DatabaseHelper databaseHelper;
    private Note noteToEdit;
    private boolean isEditMode = false;

    public static Intent newIntentForCreate(Context context) {
        return new Intent(context, AddEditNoteActivity.class);
    }

    public static Intent newIntentForEdit(Context context, long noteId) {
        Intent intent = new Intent(context, AddEditNoteActivity.class);
        intent.putExtra(EXTRA_NOTE_ID, noteId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_note);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);

        initViews();
        checkIntent();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvScreenTitle = findViewById(R.id.tvScreenTitle);
        btnHeaderSave = findViewById(R.id.btnHeaderSave);
        btnBottomSave = findViewById(R.id.btnBottomSave);
        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
    }

    private void checkIntent() {
        if (getIntent() != null && getIntent().hasExtra(EXTRA_NOTE_ID)) {
            long noteId = getIntent().getLongExtra(EXTRA_NOTE_ID, -1);
            if (noteId != -1) {
                noteToEdit = databaseHelper.getNote(noteId);
            }
        }

        if (noteToEdit != null) {
            isEditMode = true;
            tvScreenTitle.setText(R.string.title_edit_note);
            btnHeaderSave.setText(R.string.update);
            btnBottomSave.setText(R.string.update);

            etTitle.setText(noteToEdit.getTitle());
            etContent.setText(noteToEdit.getContent());
        } else {
            isEditMode = false;
            tvScreenTitle.setText(R.string.title_add_note);
            btnHeaderSave.setText(R.string.save);
            btnBottomSave.setText(R.string.save);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnHeaderSave.setOnClickListener(v -> saveNote());
        btnBottomSave.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String content = etContent.getText() != null ? etContent.getText().toString().trim() : "";

        if (title.isEmpty()) {
            etTitle.setError(getString(R.string.error_empty_title));
            etTitle.requestFocus();
            return;
        }

        if (content.isEmpty()) {
            etContent.setError(getString(R.string.error_empty_content));
            etContent.requestFocus();
            return;
        }

        long now = System.currentTimeMillis();

        if (isEditMode && noteToEdit != null) {
            noteToEdit.setTitle(title);
            noteToEdit.setContent(content);
            noteToEdit.setUpdatedAt(now);

            databaseHelper.updateNote(noteToEdit);
            Toast.makeText(this, R.string.msg_updated, Toast.LENGTH_SHORT).show();
        } else {
            Note newNote = new Note(title, content, now, now);
            databaseHelper.insertNote(newNote);
            Toast.makeText(this, R.string.msg_added, Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
