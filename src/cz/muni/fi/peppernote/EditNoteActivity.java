package cz.muni.fi.peppernote;

import java.util.ArrayList;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class EditNoteActivity extends Activity {
	private PepperNoteManager manager;
	private Note note;
	private TextView title;
	private EditText content;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_note);        
        
        PepperNote appState = (PepperNote)getApplicationContext();
        manager = appState.manager();

        int noteId = getIntent().getExtras().getInt(NotesActivity.NOTE_ID);
        note = manager.getNoteById(noteId);
        
        title = (TextView)findViewById(R.id.editNoteTitleEditText);
        content = (EditText)findViewById(R.id.editNoteContent);
        
        title.setText(note.get_title());
        content.setText(note.get_content());
        
        initializeSaveButton();                
    }
	
	public void initializeSaveButton(){
		Button b = (Button)findViewById(R.id.saveEditNoteButton);
		b.setOnClickListener(new View.OnClickListener() {
			
			
			public void onClick(View arg0) {
				String old_title = note.get_title();
				note.set_title(title.getText().toString().trim());
				note.set_content(content.getText().toString());

				if (!(note.isValid())){
	            	allertMessage("Title is not valid.\nLength must be between 0 and 51");				            
	            } else if(isNameTaken(note,old_title)){ 
	            	allertMessage("Name is already taken!");
	            } else {
	            	manager.updateNote(note, isOnline());
					finish();
	            }				
			}
		});
	}
	
	public boolean isNameTaken(Note note){
		Notebook notebook = manager.getNotebookById(note.get_notebook_id());
		ArrayList<Note> notes = (ArrayList<Note>)manager.getNotesByNotebookFromDB(notebook);
		for(int i = 0; i< notes.size();i++){
			if(notes.get(i).get_title().equals(note.get_title())){
				return true;
			}
		}
		return false;
	}
	
	public boolean isNameTaken(Note note, String old_title){
		if(note.get_title().equals(old_title)){
			return false;
		}
		Notebook notebook = manager.getNotebookById(note.get_notebook_id());
		ArrayList<Note> notes = (ArrayList<Note>)manager.getNotesByNotebookFromDB(notebook);
		for(int i = 0; i< notes.size();i++){
			if(notes.get(i).get_title().equals(note.get_title())){
				return true;
			}
		}
		return false;
	}
	
	public void allertMessage(String s){
		new AlertDialog.Builder(EditNoteActivity.this)
			.setMessage(s).setCancelable(true).show();
	}
	
	private boolean isOnline(){
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if(info == null){
			return false;
		} else {
			return info.isConnected();
		}
	}
	
	private void errorDialog(Exception e){
		AlertDialog.Builder builder = new AlertDialog.Builder(EditNoteActivity.this);
		builder.setMessage(e.toString())
		       .setCancelable(true);
		AlertDialog error_dialog = builder.create();
		error_dialog.show();
	}
}
