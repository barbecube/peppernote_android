package cz.muni.fi.peppernote;

import java.util.ArrayList;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class NewNoteActivity extends Activity{
	private PepperNoteManager manager;
	private Notebook notebook;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_note);        
        
        PepperNote appState = (PepperNote)getApplicationContext();
        manager = appState.manager();

        int notebookIndex = getIntent().getExtras().getInt(NotebooksActivity.NOTEBOOK_INDEX);
        notebook = manager.getNotebooks().get(notebookIndex);
                      
        initializeSaveButton();                
    }
	
	public void initializeSaveButton(){
		final TextView title = (TextView)findViewById(R.id.newNoteTitleEditText);
        final EditText content = (EditText)findViewById(R.id.newNoteContent);
		Button b = (Button)findViewById(R.id.saveNewNoteButton);
		b.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				Note new_note = new Note();
				new_note.set_title(title.getText().toString().trim());
				new_note.set_content(content.getText().toString());
				new_note.set_notebook_id(notebook.get_id());
				
					if (!(Note.isValidTitle(new_note.get_title()))){
		            	allertMessage("Title is not valid.\nLength must be between 0 and 51");				            
		            } else if(isTitleTaken(new_note)){
		            	allertMessage("Name is already taken!");
		            } else {
		            	Note returned = manager.createNote(new_note, notebook, isOnline());
						finish();
		            }				
			}
		});
	}
	
	public boolean isTitleTaken(Note note){
		ArrayList<Note> notes = (ArrayList<Note>)manager.getNotesByNotebookFromDB(notebook);
		for(int i = 0; i< notes.size();i++){
			if(notes.get(i).get_title().equals(note.get_title())){
				return true;
			}
		}
		return false;
	}
	
	public void allertMessage(String s){
		new AlertDialog.Builder(NewNoteActivity.this)
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
		AlertDialog.Builder builder = new AlertDialog.Builder(NewNoteActivity.this);
		builder.setMessage(e.toString())
		       .setCancelable(true);
		AlertDialog error_dialog = builder.create();
		error_dialog.show();
	}
}
