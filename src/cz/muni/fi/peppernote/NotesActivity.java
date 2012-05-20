package cz.muni.fi.peppernote;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class NotesActivity extends Activity{
	private PepperNoteManager manager;
	private Notebook notebook;
	private ArrayList<Note> notes;
	private ListView list;
	
	public static final String NOTE_ID = "NOTE_ID";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes);
        PepperNote appState = (PepperNote)getApplicationContext();
        manager = appState.manager();

        list = (ListView)findViewById(R.id.notesList);        
        int notebookIndex = getIntent().getExtras().getInt(NotebooksActivity.NOTEBOOK_INDEX);
        notebook = manager.getNotebooks().get(notebookIndex);
        
        TextView title = (TextView)findViewById(R.id.notebookNameTextView);
        title.setText(notebook.get_name());
        
        getNotes();
        checkNotes();
        
        initializeAddButton();
        initializeNoteList();
        initializeSearchButton();
        
    }
	
	@Override
	public void onResume(){
		super.onResume();		
		notes.clear();
		notes.addAll((ArrayList<Note>)manager.getNotesByNotebookFromDB(notebook));
		((ArrayAdapter<Note>)list.getAdapter()).notifyDataSetChanged();
		checkNotes();
	}	
	
	public void getNotes(){
		notes = (ArrayList<Note>)manager.getNotesByNotebookFromDB(notebook);
	}

	public void checkNotes(){
		View emptyList = findViewById(R.id.emptyNotesListView);
		if(notes.isEmpty()){
        	list.setVisibility(View.INVISIBLE);
        	emptyList.setVisibility(View.VISIBLE);
        } else {
        	list.setVisibility(View.VISIBLE);
        	emptyList.setVisibility(View.INVISIBLE);
        }
	}
	
	public void initializeNoteList(){
		ArrayAdapter<Note> notesAdapter = new ArrayAdapter<Note>(this,
        		R.layout.note_list_item, notes);
        notesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        list.setAdapter(notesAdapter);
        
        list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				ArrayAdapter<Note> adapter = (ArrayAdapter<Note>) parent
						.getAdapter();

				Intent intent = new Intent(NotesActivity.this,
						ShowNoteActivity.class);

				Bundle b = new Bundle();
				b.putInt(NOTE_ID, adapter.getItem(position).get_id());
				intent.putExtras(b);

				startActivity(intent);
			}
		});
	}
	
	public void initializeAddButton(){
		Button b = (Button)findViewById(R.id.addNoteButton);
		b.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				Intent intent = new Intent(NotesActivity.this, NewNoteActivity.class);
				Bundle b =new Bundle();
				b.putInt(NotebooksActivity.NOTEBOOK_INDEX, getIntent().getExtras().getInt(NotebooksActivity.NOTEBOOK_INDEX));
				intent.putExtras(b);
				
				startActivity(intent);
			}
		});
	}
	
	private void initializeSearchButton() {		

		Button searchButton = (Button) findViewById(R.id.searchNotesButton);
		final EditText searchEditText = (EditText) findViewById(R.id.searchNoteEditText);

		searchButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (searchEditText.getVisibility() == View.INVISIBLE) {
					searchEditText.setVisibility(View.VISIBLE);
				} else {
					searchEditText.setVisibility(View.INVISIBLE);
					searchEditText.clearComposingText();
					searchEditText.setText("");
				}
			}
		});

		searchEditText.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				((ArrayAdapter) list.getAdapter())
						.getFilter().filter(s.toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});

	}
}

