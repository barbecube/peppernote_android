package cz.muni.fi.peppernote;

import java.util.ArrayList;
import java.util.List;

import cz.muni.fi.peppernote.OptionsActivity.SendChangesTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ConflictsActivity extends Activity{
	private PepperNoteManager manager;
	private ListView list;
	private ServerConflict currentConflict;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conflicts);
        PepperNote appState = (PepperNote)getApplicationContext();
        manager = appState.manager();

        list = (ListView)findViewById(R.id.conflictList);        
        
        manager.getNotebooksFromDB();  
        
        //initializeAddButton();
        initializeConflictList();
        //initializeSearchButton();        
    }
	
	public void initializeConflictList(){
		ArrayAdapter<ServerConflict> conflictsAdapter = new ArrayAdapter<ServerConflict>(this,
        		R.layout.conflict_list_item, manager.getConflicts());
        conflictsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        list.setAdapter(conflictsAdapter);
        //registerForContextMenu(list);
        list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				ArrayAdapter<ServerConflict> adapter = (ArrayAdapter<ServerConflict>) parent
						.getAdapter();
				currentConflict = adapter.getItem(position);
				switch(currentConflict.getConflict()){
					case ServerConflict.TAKEN_VALUE:
						takenValueDialog();
						break;
					case ServerConflict.ID_NOT_FOUND:
						idNotFoundDialog();
						break;
					case ServerConflict.NEW_VERSION:
						newVersionDialog();
						break;
				}
			}
		});
		
	}
	
	public void takenValueDialog(){
		String message = entityString(currentConflict);
		String title = entity(currentConflict);
		
		new AlertDialog.Builder(ConflictsActivity.this)
	    .setTitle(title + " is already taken")
		.setMessage("Please rename " + message)
		.setCancelable(true)
	    .show();
	}
	
	public void idNotFoundDialog(){
		String message = entityString(currentConflict);		
		new AlertDialog.Builder(ConflictsActivity.this)
	    .setTitle("Deleted on server")
		.setMessage("Want to recreate " + message + "?")
	    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {	        		
	        	switch(currentConflict.getType_of_entity()){
					case PepperNoteManager.NOTEBOOK_ENTITY:
						Notebook nb = (Notebook)currentConflict.getEntity();
						manager.createChange(nb.get_id(), 0, Change.CREATE_CHANGE, PepperNoteManager.NOTEBOOK_ENTITY);
						List<Note> notes = manager.getNotesByNotebookFromDB(nb);
						for(int i = 0; i < notes.size();i++){
							manager.createChange(notes.get(i).get_id(), 0, Change.CREATE_CHANGE, PepperNoteManager.NOTE_ENTITY);
						}
						break;
					case PepperNoteManager.NOTE_ENTITY:
						Note note = (Note)currentConflict.getEntity();
						manager.createChange(note.get_id(), 0, Change.CREATE_CHANGE, PepperNoteManager.NOTE_ENTITY);
						break;
				}
	        	manager.getConflicts().remove(currentConflict);
	            ((ArrayAdapter<ServerConflict>)list.getAdapter()).notifyDataSetChanged();
	        }
	    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            manager.getConflicts().remove(currentConflict);
	            ((ArrayAdapter<ServerConflict>)list.getAdapter()).notifyDataSetChanged();
	        }
	    }).show();
	}
	
	public void newVersionDialog(){
		if(currentConflict.getType_of_change() == Change.UPDATE_CHANGE){
			new AlertDialog.Builder(ConflictsActivity.this)
		    .setTitle("Newer version on server")
			.setMessage("Update with older version?")
		    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {	
		        	Notebook nb = (Notebook)currentConflict.getEntity();
		        	Notebook old = manager.getNotebookById(nb.get_id());
		        	old.set_version(nb.get_version());
		        	manager.updateNotebook(old, false);
		        	manager.createChange(nb.get_id(), nb.get_version(), Change.UPDATE_CHANGE, PepperNoteManager.NOTEBOOK_ENTITY);
		        	manager.getConflicts().remove(currentConflict);
		            ((ArrayAdapter<ServerConflict>)list.getAdapter()).notifyDataSetChanged();
		        }
		    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		        	Notebook nb = (Notebook)currentConflict.getEntity();
		        	manager.updateNotebook(nb, false);
		        	manager.getConflicts().remove(currentConflict);
		            ((ArrayAdapter<ServerConflict>)list.getAdapter()).notifyDataSetChanged();
		        }
		    }).show();
		} else {
			new AlertDialog.Builder(ConflictsActivity.this)
		    .setTitle("Newer version on server")
			.setMessage("Save new version\non next synchronization?")
		    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {	
		        	manager.getConflicts().remove(currentConflict);
		            ((ArrayAdapter<ServerConflict>)list.getAdapter()).notifyDataSetChanged();
		        }
		    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		        	switch(currentConflict.getType_of_entity()){
						case PepperNoteManager.NOTEBOOK_ENTITY:
							Notebook nb = (Notebook)currentConflict.getEntity();
							manager.createChange(nb.get_server_id(), nb.get_version(), Change.DELETE_CHANGE, PepperNoteManager.NOTEBOOK_ENTITY);						
							manager.getConflicts().remove(currentConflict);
				            ((ArrayAdapter<ServerConflict>)list.getAdapter()).notifyDataSetChanged();
							break;
						case PepperNoteManager.NOTE_ENTITY:
							Note note = (Note)currentConflict.getEntity();
							manager.createChange(note.get_server_id(), note.get_version(), Change.DELETE_CHANGE, PepperNoteManager.NOTE_ENTITY);
							manager.getConflicts().remove(currentConflict);
				            ((ArrayAdapter<ServerConflict>)list.getAdapter()).notifyDataSetChanged();
							break;
					}
		        }
		    }).show();
		}
	}
	
	public String entityString(ServerConflict conflict){
		switch(conflict.getType_of_entity()){
			case PepperNoteManager.NOTEBOOK_ENTITY:
				Notebook nb = (Notebook)conflict.getEntity();
				return "notebook: " + nb.get_name();
			case PepperNoteManager.NOTE_ENTITY:
				Note note = (Note)conflict.getEntity();
				return "note: " + note.get_title();
		}
		return null;
	}
	
	public String entity(ServerConflict conflict){
		switch(conflict.getType_of_entity()){
			case PepperNoteManager.NOTEBOOK_ENTITY:
				Notebook nb = (Notebook)conflict.getEntity();
				return "Name";
			case PepperNoteManager.NOTE_ENTITY:
				Note note = (Note)conflict.getEntity();
				return "Title";
		}
		return null;
	}
	
	
}
