package cz.muni.fi.peppernote;

import java.util.ArrayList;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class ShowNoteActivity extends Activity{
	private PepperNoteManager manager;
	private Note note;
	private TextView title;
	private EditText content;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_note);

        PepperNote appState = (PepperNote)getApplicationContext();
        manager = appState.manager();

        
                
        title = (TextView)findViewById(R.id.noteTitleTextView);
        content = (EditText)findViewById(R.id.noteContent);
        
        content.setFocusable(false);


        initializeDeleteButton();
        initializeEditButton();        
    }
	
	@Override
	public void onResume(){
		super.onResume();
		int noteId = getIntent().getExtras().getInt(NotesActivity.NOTE_ID);
		note = manager.getNoteById(noteId);
		title.setText(note.get_title());
        content.setText(note.get_content());        
	}
	
	public void initializeDeleteButton(){
		Button b = (Button)findViewById(R.id.deleteNoteButton);
		b.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				int note_id = getIntent().getExtras().getInt(NotesActivity.NOTE_ID);
				final Note note = manager.getNoteById(note_id);
				new AlertDialog.Builder(ShowNoteActivity.this)
			    .setMessage("Delete note: " + note.get_title() +"?")
			    .setCancelable(true)
			    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {			            
						manager.deleteNote(note, isOnline());
						finish();						            
			        }
			    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			            // Do nothing.
			        }
			    }).show();
			}
		});
	}
	
	public void initializeEditButton(){
		Button b = (Button)findViewById(R.id.editNoteButton);
		b.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(ShowNoteActivity.this,
						EditNoteActivity.class);

				Bundle b = new Bundle();
				b.putInt(NotesActivity.NOTE_ID, note.get_id());
				intent.putExtras(b);

				startActivity(intent);				
			}
		});
			}
	
	public boolean isOnline(){
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if(info == null){
			return false;
		} else {
			return info.isConnected();
		}
	}
	
	public void errorDialog(Exception e){
		AlertDialog.Builder builder = new AlertDialog.Builder(ShowNoteActivity.this);
		builder.setMessage(e.toString())
		       .setCancelable(true);
		AlertDialog error_dialog = builder.create();
		error_dialog.show();
	}
}
