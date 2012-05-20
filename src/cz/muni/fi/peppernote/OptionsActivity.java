package cz.muni.fi.peppernote;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import cz.muni.fi.peppernote.PepperNoteActivity.SignInTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class OptionsActivity extends Activity{

	private PepperNoteManager manager;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.options);
        PepperNote appState = (PepperNote)getApplicationContext();
        manager = appState.manager();
        manager.getChangesFromDB();

        initializeSyncButton();
        initializeConflictsButton();
        initializeClearDatabaseButton();
    }
	
	public void initializeSyncButton(){
		Button b = (Button)findViewById(R.id.syncButton);
		b.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				new AlertDialog.Builder(OptionsActivity.this)
			    .setTitle("Synchronizing")
				.setMessage("Synchronize with server?")
			    .setPositiveButton("Sync", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			        	if(isOnline()){
			        		if(manager.getConflicts().size() == 0){
			        			new SendChangesTask().execute();
			        		} else {
			        			confirmSyncDialog();
			        		}
						} else {
							errorDialog("No internet connection");
							return;
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
	
	public void confirmSyncDialog(){
		new AlertDialog.Builder(OptionsActivity.this)
	    .setTitle("Unresolved conflicts")
		.setMessage("Ignore conflicts?")
	    .setPositiveButton("Sync", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {	        		
					new SendChangesTask().execute();				
	        }
	    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            // Do nothing.
	        }
	    }).show();
	}
	
	public void initializeConflictsButton(){
		Button b = (Button)findViewById(R.id.conflictsButton);
		b.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(OptionsActivity.this,
						ConflictsActivity.class);
				startActivity(intent);				
			}
		});
		
	}

	public void initializeClearDatabaseButton(){
		Button b = (Button)findViewById(R.id.clearDatabaseButton);
		b.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				new AlertDialog.Builder(OptionsActivity.this)
			    .setTitle("Clear Database")
				.setMessage("Clearing database will cause losing all locally saved notebooks and notes.")
			    .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			        	manager.clearDatabase();
			        }
			    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			            // Do nothing.
			        }
			    }).show();				
			}
		});		
	}
	
	public class SendChangesTask extends AsyncTask<Void, Void, Void>{
    	private Dialog dialog;
    	
    	@Override
    	protected void onPreExecute(){
    		dialog = ProgressDialog.show(OptionsActivity.this, "", "Sending changes",true,false);
    	}

		@Override
		protected Void doInBackground(Void... params) {
			manager.getConflicts().clear();
			manager.sendChanges();
			return null;
		}
		
		@Override
    	protected void onPostExecute(Void v){
    		dialog.cancel();
    		if(manager.getConflicts().size() != 0){
    			AlertDialog.Builder builder = new AlertDialog.Builder(OptionsActivity.this);
    			builder.setMessage("Synchronization conflicts!\nResolve using Conflicts button")
    			       .setCancelable(true);
    			dialog = builder.create();
    			dialog.show();
    		} else if(manager.getChanges().size() != 0){
    			AlertDialog.Builder builder = new AlertDialog.Builder(OptionsActivity.this);
    			builder.setMessage("Unresolved changes try synchronizing again.")
    			       .setCancelable(true);
    			dialog = builder.create();
    			dialog.show();
    		} else {
    			new SynchronizingTask().execute();
    		}
    	}    	
    }

	public class SynchronizingTask extends AsyncTask<Void, Void, String>{
    	private Dialog dialog;
    	
    	@Override
    	protected void onPreExecute(){
    		dialog = ProgressDialog.show(OptionsActivity.this, "", "Synchronizing",true,false);
    	}

		@Override
		protected String doInBackground(Void... params) {
			try {
				manager.synchronize();
			} catch (IOException e) {
				return "Synchronization failed\nServer connection problem";
			} catch (JSONException e) {
				return "Synchronization failed.\nTry Sign out and Sign in\nbefore next synchronization";
			}
			return null;
		}
		
		@Override
    	protected void onPostExecute(String s){
    		dialog.cancel();
    		
    		if(s != null){
    			errorDialog(s);
    		} else {
    			String message;
    			if(manager.getConflicts().size() == 0){
    				message = "Synchronization successful";
    			} else {
    				message = "Synchronization conflicts appeared!\nResolve using Conflicts button";
    			}
    			AlertDialog.Builder builder = new AlertDialog.Builder(OptionsActivity.this);
    			builder.setMessage(message)
    			       .setCancelable(true);
    			dialog = builder.create();
    			dialog.show();
    		}
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
		AlertDialog.Builder builder = new AlertDialog.Builder(OptionsActivity.this);
		builder.setMessage(e.toString())
		       .setCancelable(true);
		AlertDialog error_dialog = builder.create();
		error_dialog.show();
	}
	
	public void errorDialog(String s){
		AlertDialog.Builder builder = new AlertDialog.Builder(OptionsActivity.this);
		builder.setMessage(s)
		       .setCancelable(true);
		AlertDialog error_dialog = builder.create();
		error_dialog.show();
	}
}
