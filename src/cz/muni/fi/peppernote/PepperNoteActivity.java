package cz.muni.fi.peppernote;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class PepperNoteActivity extends Activity {
    
	public static final String USER_PREFS = "userPrefs";
	public static final String USER_ID_PREF = "userId";
	public static final String SERVER_PREF = "server";
	public static final String TOKEN_PREF = "token";
	public static final String CONFLICTS_PREF = "conflicts";
	
	PepperNoteManager manager;
	List<PepperNoteServer> servers;
    Spinner serversSpinner;
	SharedPreferences user_prefs;
	
	/** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);
        PepperNote appState = (PepperNote)getApplicationContext();
        manager = appState.manager();
        manager.setHttpRequestManager(new HttpRequestManager());        
        manager.setDatabaseManager(new DatabaseManager(getApplicationContext()));        
        servers = manager.getAllServers();
        if(servers.size() == 0){
        	PepperNoteServer PNS = new PepperNoteServer();
        	PNS.setAddress("https://peppernote.herokuapp.com");
        	PNS.set_id(manager.createServer(PNS));
        	servers.add(PNS);
        }        
        user_prefs = getSharedPreferences(USER_PREFS, MODE_PRIVATE);
        checkPreferences();
        
        serversSpinner = (Spinner)findViewById(R.id.serversListSpinner);
        ArrayAdapter<PepperNoteServer> serversAdapter = new ArrayAdapter<PepperNoteServer>(this,
        		android.R.layout.simple_spinner_item, servers);
        serversAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serversSpinner.setAdapter(serversAdapter);
        
         initializeSignInButton();
         initializeAddServerButton();
    }
    
    public void checkPreferences(){
    	if(user_prefs.contains(USER_ID_PREF)){
    		manager.setUser_id(user_prefs.getInt(USER_ID_PREF, 0));
    		PepperNoteServer PNS = getServerById(user_prefs.getInt(SERVER_PREF, 0));
    		manager.setPepperNoteServer(PNS);    		
    		manager.getHttpRequestManager().setServer(PNS.getAddress());
    		manager.getHttpRequestManager().setToken(user_prefs.getString(TOKEN_PREF, null));
    		if(user_prefs.contains(CONFLICTS_PREF)){
    			String json = user_prefs.getString(CONFLICTS_PREF, null);
    			List<ServerConflict> list = ServerConflictJSON.jsonServersToList(json);
    			manager.setConflicts(list);
    		}
    		Intent intent = new Intent(PepperNoteActivity.this,
					NotebooksActivity.class);
			startActivity(intent);
			finish();    		
    	}
    }
    
    public PepperNoteServer getServerById(int id){
    	for(int i = 0; i < servers.size();i++){
    		if(servers.get(i).get_id() == id){
    			return servers.get(i); 
    		}
    	}
    	return null;
    }
    
    public void initializeAddServerButton(){
    	Button b = (Button)findViewById(R.id.addServerButton);
        b.setEnabled(true);
        b.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				final EditText input = new EditText(PepperNoteActivity.this);
				input.setHint("Url");
				new AlertDialog.Builder(PepperNoteActivity.this)
				    .setMessage("Add new server")
				    .setView(input)
				    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {
				            Editable url = input.getText();
				            PepperNoteServer new_s = new PepperNoteServer();
				            new_s.setAddress(url.toString());
				            new_s.set_id(manager.createServer(new_s));
				            servers.add(new_s);
				        }
				    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {
				            // Do nothing.
				        }
				    }).show();
			}
		});
    }
    
    public void initializeSignInButton(){
    	Button b = (Button)findViewById(R.id.signInButton);
        b.setEnabled(true);
        b.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				EditText emailEdit = (EditText)findViewById(R.id.emailEditText);
				EditText passwordEdit = (EditText)findViewById(R.id.passwordEditText);
				
				if(isOnline()){
					new SignInTask().execute(emailEdit.getText().toString(),passwordEdit.getText().toString());
				} else {
					new AlertDialog.Builder(PepperNoteActivity.this).setMessage("No internet connection!").show();
				}
			}
		});
    }
    
    public class SignInTask extends AsyncTask<String, Void, String>{
    	private Dialog dialog;
    	
    	@Override
    	protected void onPreExecute(){
    		dialog = ProgressDialog.show(PepperNoteActivity.this, "", "Signing in",true,false);
    		manager.setPepperNoteServer((PepperNoteServer)serversSpinner.getSelectedItem());
    	}

		@Override
		protected String doInBackground(String... params) {
			String email = params[0];
			String password = params[1];
			try {
				manager.signIn(email, password);
			} catch (ClientProtocolException e) {
				return "Server communication error";
			} catch (JSONException e) {
				return "JSON error occured";
			} catch (IOException e) {
				return "Server communication error. Check server address.";
			} catch (EntityException e) {
				return "Entity error occured";
			} catch (UnauthorizedException e) {
				return "Invalid email or password";
			} catch (BadRequestException e) {
				return e.toString();
			} catch (NotFoundException e) {
				return e.toString();
			}
			return null;
		}
		
		@Override
    	protected void onPostExecute(String s){
    		dialog.cancel();
    		if(s != null){
    			AlertDialog.Builder builder = new AlertDialog.Builder(PepperNoteActivity.this);
    			builder.setMessage(s)
    			       .setCancelable(true);
    			dialog = builder.create();
    			dialog.show();
    		} else {
    			
    			SharedPreferences.Editor editor = user_prefs.edit();
    			editor.putInt(USER_ID_PREF, manager.getUser_id());
    			editor.putInt(SERVER_PREF, manager.getPepperNoteServer().get_id());
    			editor.putString(TOKEN_PREF, manager.getHttpRequestManager().getToken());
    			editor.commit();
    			Intent intent = new Intent(PepperNoteActivity.this,
						NotebooksActivity.class);
				startActivity(intent);
				finish();
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
}