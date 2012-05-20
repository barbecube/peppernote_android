package cz.muni.fi.peppernote;

import java.io.IOException;
import java.util.ArrayList;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class NotebooksActivity extends Activity{

	private PepperNoteManager manager;
	private ListView list;
	
	public static final String NOTEBOOK_INDEX = "NOTEBOOK_INDEX";
	private static final String TAG = "NotebooksActivity";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        PepperNote appState = (PepperNote)getApplicationContext();
        manager = appState.manager();
        manager.setNotebooks(new ArrayList<Notebook>());
        manager.getChangesFromDB();
        if(manager.getConflicts() == null){
        	manager.setConflicts(new ArrayList<ServerConflict>());
        }

        list = (ListView)findViewById(R.id.notebookList);        
        
        manager.getNotebooksFromDB();  
        
        initializeAddButton();
        initializeNotebookList();
        initializeSearchButton();
        
    }
	
	@Override
	public void onResume(){
		super.onResume();		
		manager.getNotebooksFromDB();    
		((ArrayAdapter<Note>)list.getAdapter()).notifyDataSetChanged();
        checkNotebooks();
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		String conflicts = ServerConflictJSON.serversToString(manager.getConflicts());
		SharedPreferences user_prefs = getSharedPreferences(PepperNoteActivity.USER_PREFS, MODE_PRIVATE);
		SharedPreferences.Editor editor = user_prefs.edit();
		editor.putString(PepperNoteActivity.CONFLICTS_PREF, conflicts);
		editor.commit();
	}
	
	public void checkNotebooks(){
		View emptyList = findViewById(R.id.emptyNotebookListView);
		if(manager.getNotebooks().isEmpty()){
        	list.setVisibility(View.INVISIBLE);
        	emptyList.setVisibility(View.VISIBLE);
        } else {
        	list.setVisibility(View.VISIBLE);
        	emptyList.setVisibility(View.INVISIBLE);
        }
	}
	
	public void initializeNotebookList(){
		ArrayAdapter<Notebook> notebooksAdapter = new ArrayAdapter<Notebook>(this,
        		R.layout.notebook_list_item, manager.getNotebooks());
        notebooksAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        list.setAdapter(notebooksAdapter);
        registerForContextMenu(list);
        
        list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				ArrayAdapter<Notebook> adapter = (ArrayAdapter<Notebook>) parent
						.getAdapter();

				Intent intent = new Intent(NotebooksActivity.this,
						NotesActivity.class);

				Bundle b = new Bundle();
				b.putInt(NOTEBOOK_INDEX, manager.getNotebooks().indexOf(adapter.getItem(position)));
				intent.putExtras(b);

				startActivity(intent);
			}
		});
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.notebook_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.renameNotebook:
			renameNotebook(info.position);
			return true;
		case R.id.deleteNotebook:
			deleteNotebook(info.position);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.options_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.sign_out:
	            signOut();
	            return true;
	        case R.id.options:
	        	Intent intent = new Intent(NotebooksActivity.this,
						OptionsActivity.class);
				startActivity(intent);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public void signOut(){
		new AlertDialog.Builder(NotebooksActivity.this)
	    .setMessage("Do you want to sign out?")
	    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	        	try {
					if(isOnline()){
						manager.signOut();
					}
				} catch (UnauthorizedException e) {
					Log.e(TAG, e.getMessage());
					/*
					errorDialog(e);
					return;*/
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
					/*
					errorDialog(e);
					return;*/
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage());
					/*
					errorDialog(e);
					return;*/
				} catch (NotFoundException e) {
					Log.e(TAG, e.getMessage());
					/*
					errorDialog(e);
					return;*/
				}
	        	manager.saveChangesFromConflicts();
	        	SharedPreferences user_prefs = getSharedPreferences(PepperNoteActivity.USER_PREFS, MODE_PRIVATE);
	        	SharedPreferences.Editor editor = user_prefs.edit();
	        	editor.remove(PepperNoteActivity.USER_ID_PREF);
	        	editor.remove(PepperNoteActivity.SERVER_PREF);
	        	editor.remove(PepperNoteActivity.TOKEN_PREF);
	        	editor.remove(PepperNoteActivity.CONFLICTS_PREF);
	        	editor.commit();
	        	finish();
	        }
	    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            // Do nothing.
	        }
	    }).show();
	}	
	
	public void renameNotebook(int position){
		final ArrayAdapter<Notebook> list_adapter = (ArrayAdapter<Notebook>)list.getAdapter();
		final Notebook nb_to_rename = list_adapter.getItem(position);
		final EditText input = new EditText(NotebooksActivity.this);		
		input.setText(nb_to_rename.get_name());
		new AlertDialog.Builder(NotebooksActivity.this)
		    .setMessage("Rename notebook")
		    .setView(input)
		    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            String new_name = input.getText().toString().trim();
		            if(new_name.equals(nb_to_rename.get_name())){
		            	return;
		            }		            	            	
	            	if (!(Notebook.isValidName(new_name))){
		            	allertMessage("Name is not valid.\nLength must be between 0 and 51");			            	
		            } else if(isNameTaken(new_name)){
		            	allertMessage("Name is already taken!");
		            } else {
		            	nb_to_rename.set_name(new_name);
		            	manager.updateNotebook(nb_to_rename, isOnline());
		            	list_adapter.notifyDataSetChanged();
		            }					
		        }
		    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            // Do nothing.
		        }
		    }).show();
	}

	public void deleteNotebook(int position){
		final ArrayAdapter<Notebook> list_adapter = (ArrayAdapter<Notebook>)list.getAdapter();
		final Notebook nb_to_delete = list_adapter.getItem(position);
		new AlertDialog.Builder(NotebooksActivity.this)
	    .setMessage("Delete " + nb_to_delete.get_name() +" notebook?")
	    .setCancelable(true)
	    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
            	manager.deleteNotebook(nb_to_delete, isOnline());
				list_adapter.remove(nb_to_delete);
				checkNotebooks();				            
	        }
	    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            // Do nothing.
	        }
	    }).show();
	}
	
	
	public void initializeAddButton(){
		Button b = (Button)findViewById(R.id.addNotebookButton);
		b.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				final EditText input = new EditText(NotebooksActivity.this);
				input.setHint("Name");
				new AlertDialog.Builder(NotebooksActivity.this)
				    .setMessage("Add new notebook")
				    .setView(input)
				    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {
				            String name = input.getText().toString().trim();
				            Notebook new_nb = new Notebook();
				            new_nb.set_name(name);
				            
				            if (!(Notebook.isValidName(name))){
				            	allertMessage("Name is not valid.\nLength must be between 0 and 51");				            
				            } else if(isNameTaken(name)){
				            	allertMessage("Name is already taken!");
				            } else {
				            	addNotebook(new_nb);
				            }
				        }
				    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {
				            // Do nothing.
				        }
				    }).show();
				
			}
		});
	}

	public boolean isNameTaken(String s){
		ArrayList<Notebook> notebooks = (ArrayList<Notebook>)manager.getNotebooks();
		for(int i = 0; i< notebooks.size();i++){
			if(notebooks.get(i).get_name().equals(s)){
				return true;
			}
		}
		return false;
	}
	
	public void allertMessage(String s){
		new AlertDialog.Builder(NotebooksActivity.this)
			.setMessage(s).setCancelable(true).show();
	}
	
	private void initializeSearchButton() {		

		Button searchButton = (Button) findViewById(R.id.searchButton);
		final EditText searchEditText = (EditText) findViewById(R.id.searchEditText);

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
	
	public void addNotebook(Notebook nb){
		int c_num = manager.getConflicts().size();
		manager.createNotebook(nb, isOnline());
		checkNotebooks();
		((ArrayAdapter<Notebook>)list.getAdapter()).notifyDataSetChanged();
		if(c_num < manager.getConflicts().size()){				
			AlertDialog.Builder builder = new AlertDialog.Builder(NotebooksActivity.this);
			builder.setMessage("Synchronization conflict!\nResolve in Options->Conlficts")
			       .setCancelable(true);
			AlertDialog dialog = builder.create();
			dialog.show();
		}		 
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
		AlertDialog.Builder builder = new AlertDialog.Builder(NotebooksActivity.this);
		builder.setMessage(e.toString())
		       .setCancelable(true);
		AlertDialog error_dialog = builder.create();
		error_dialog.show();
	}
}
