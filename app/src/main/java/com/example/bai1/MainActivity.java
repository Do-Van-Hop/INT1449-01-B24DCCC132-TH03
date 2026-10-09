package com.example.bai1;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bai1.adapter.NoteAdapter;
import com.example.bai1.database.DatabaseHelper;
import com.example.bai1.model.Note;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnNoteClickListener {

    private RecyclerView rvNotes;
    private TextView tvNoteCount;
    private LinearLayout llEmptyState;
    private FloatingActionButton fabAddNote;
    private EditText etSearch;
    private ImageView btnClearSearch;

    private DatabaseHelper databaseHelper;
    private NoteAdapter noteAdapter;
    private List<Note> noteList = new ArrayList<>();
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initDatabase();
        setupRecyclerView();
        setupSearch();

        fabAddNote.setOnClickListener(v -> startActivity(AddEditNoteActivity.newIntentForCreate(this)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    private void initViews() {
        rvNotes = findViewById(R.id.rvNotes);
        tvNoteCount = findViewById(R.id.tvNoteCount);
        llEmptyState = findViewById(R.id.llEmptyState);
        fabAddNote = findViewById(R.id.fabAddNote);
        etSearch = findViewById(R.id.etSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
    }

    private void initDatabase() {
        databaseHelper = new DatabaseHelper(this);
        databaseHelper.seedSampleNotesIfEmpty();
    }

    private void setupRecyclerView() {
        rvNotes.setLayoutManager(new LinearLayoutManager(this));
        noteAdapter = new NoteAdapter(noteList, this);
        rvNotes.setAdapter(noteAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                btnClearSearch.setVisibility(currentSearchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                loadNotes();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            currentSearchQuery = "";
            btnClearSearch.setVisibility(View.GONE);
            loadNotes();
        });
    }

    private void loadNotes() {
        noteList = databaseHelper.getAllNotes(currentSearchQuery);
        noteAdapter.setNotes(noteList);

        int count = noteList.size();
        tvNoteCount.setText(getString(R.string.note_count_format, count));

        if (count == 0) {
            rvNotes.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvNotes.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
        }
    }

    private void showDeleteConfirmationDialog(Note note) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(getString(R.string.confirm_delete_msg) + "\n\n\"" + note.getTitle() + "\"")
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    databaseHelper.deleteNote(note.getId());
                    Toast.makeText(MainActivity.this, R.string.msg_deleted, Toast.LENGTH_SHORT).show();
                    loadNotes();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onItemClick(Note note, int position) {
        startActivity(NoteDetailActivity.newIntent(this, note.getId()));
    }

    @Override
    public void onEditClick(Note note, int position) {
        startActivity(AddEditNoteActivity.newIntentForEdit(this, note.getId()));
    }

    @Override
    public void onDeleteClick(Note note, int position) {
        showDeleteConfirmationDialog(note);
    }
}
