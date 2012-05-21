package cz.muni.fi.peppernote;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import android.widget.ArrayAdapter;

public class PepperNoteManager {
	
	public static final int NOTEBOOK_ENTITY = 1;
	public static final int NOTE_ENTITY = 2;
	
	private HttpRequestManager httpRequestManager;
	private DatabaseManager databaseManager;
	private PepperNoteServer pepperNoteServer;
	private int user_id;
	private List<Notebook> notebooks;
	//private List<Change> changes;
	private List<ServerConflict> conflicts;
	
	public void signIn(String email, String password) throws ClientProtocolException, JSONException, IOException, EntityException, UnauthorizedException, BadRequestException, NotFoundException{
		httpRequestManager.setServer(pepperNoteServer.getAddress());
		user_id = httpRequestManager.signIn(email, password);
	};
	
	public void signOut() throws UnauthorizedException, IOException, JSONException, NotFoundException{
		httpRequestManager.signOut(user_id);
	}
	
	public void saveChangesFromConflicts(){
		for(int i = 0; i < conflicts.size(); i++){
			switch(conflicts.get(i).getType_of_entity()){
				case NOTEBOOK_ENTITY:
					Notebook nb = (Notebook)conflicts.get(i).getEntity();
					if(conflicts.get(i).getType_of_change() != Change.DELETE_CHANGE){
						createChange(nb.get_id(), nb.get_version(), 
								conflicts.get(i).getType_of_change(),conflicts.get(i).getType_of_entity());
					}
					break;
					
				case NOTE_ENTITY:
					Note note = (Note)conflicts.get(i).getEntity();
					if(conflicts.get(i).getType_of_change() != Change.DELETE_CHANGE){
						createChange(note.get_id(), note.get_version(), 
								conflicts.get(i).getType_of_change(),conflicts.get(i).getType_of_entity());
					}
					break;
			}
			
		}
		conflicts.clear();
	}
	
	public List<PepperNoteServer> getAllServers(){
		return databaseManager.getAllServers();
	};
	public int createServer(PepperNoteServer PNS){
		return databaseManager.addServer(PNS);
	};
	public void removeServer(PepperNoteServer PNS){
		databaseManager.deleteServer(PNS);
	};
	public void updateServer(PepperNoteServer PNS){
		databaseManager.updateServer(PNS);
	};
	
	// Synchronization
	public void sendChanges(){
		List<Change> changes = getChangesFromDB();
		for(int i=0; i < changes.size(); i++){
			Change current_change = changes.get(i); 
			if(current_change.get_type_of_change() == Change.CREATE_CHANGE){
				if(current_change.get_type_of_entity() == NOTEBOOK_ENTITY){
					Notebook nb = databaseManager.getNotebook(current_change.get_entity_id());
					databaseManager.deleteChange(current_change);
					//changes.remove(current_change);
					try {
						 Notebook server_nb = createNotebookOnServer(nb);
						 server_nb.set_id(nb.get_id());
						 server_nb.set_server(pepperNoteServer.get_id());
						 databaseManager.updateNotebook(server_nb);
						 notebooks.add(server_nb);
					} catch (JSONException e) {
						createChange(current_change.get_entity_id(), current_change.get_version(), 
								current_change.get_type_of_change(), current_change.get_type_of_entity());
					}
				} else if(current_change.get_type_of_entity() == NOTE_ENTITY){
					Note note = databaseManager.getNote(current_change.get_entity_id());
					databaseManager.deleteChange(current_change);
					//changes.remove(current_change);
					try {						
						Notebook nb = databaseManager.getNotebook(note.get_notebook_id());
						Note returned = createNoteOnServer(note, nb);
						returned.set_id(note.get_id());
						returned.set_notebook_id(nb.get_id());
						databaseManager.updateNote(returned);
					} catch (JSONException e) {
						createChange(current_change.get_entity_id(), current_change.get_version(), 
								current_change.get_type_of_change(), current_change.get_type_of_entity());
					}
				}
			} else if(current_change.get_type_of_change() == Change.DELETE_CHANGE){
				if(current_change.get_type_of_entity() == NOTEBOOK_ENTITY){
					Notebook nb_ghost = new Notebook();
					nb_ghost.set_server_id(current_change.get_entity_id());
					nb_ghost.set_version(current_change.get_version());
					databaseManager.deleteChange(current_change);
					//changes.remove(current_change);
					try {
						deleteNotebookOnServer(nb_ghost, null);
					} catch (JSONException e) {
						createChange(current_change.get_entity_id(), current_change.get_version(), 
								current_change.get_type_of_change(), current_change.get_type_of_entity());
					}
				} else if(current_change.get_type_of_entity() == NOTE_ENTITY){
					Note note_ghost = new Note();
					note_ghost.set_server_id(current_change.get_entity_id());
					note_ghost.set_version(current_change.get_version());
					databaseManager.deleteChange(current_change);
					//changes.remove(current_change);
					try {
						deleteNoteOnServer(note_ghost, null);
					} catch (JSONException e) {
						createChange(current_change.get_entity_id(), current_change.get_version(), 
								current_change.get_type_of_change(), current_change.get_type_of_entity());
					}
				}
			} else if(current_change.get_type_of_change() == Change.UPDATE_CHANGE){
				if(current_change.get_type_of_entity() == NOTEBOOK_ENTITY){
					Notebook nb = databaseManager.getNotebook(current_change.get_entity_id());
					databaseManager.deleteChange(current_change);
					//changes.remove(current_change);
					try {
						updateNotebookOnServer(nb, null);
					} catch (JSONException e) {
						createChange(current_change.get_entity_id(), current_change.get_version(), 
								current_change.get_type_of_change(), current_change.get_type_of_entity());
					}
				} else if(current_change.get_type_of_entity() == NOTE_ENTITY){
					Note note = databaseManager.getNote(current_change.get_entity_id());
					databaseManager.deleteChange(current_change);
					//changes.remove(current_change);
					try {
						updateNoteOnServer(note, null);
					} catch (JSONException e) {
						createChange(current_change.get_entity_id(), current_change.get_version(), 
								current_change.get_type_of_change(), current_change.get_type_of_entity());
					}
				}
			}
		}
	};
	
	public void synchronize() throws IOException, JSONException{
		List<Notebook> server_notebooks = getNotebooksFromServer();
		//notes under certain index belongs to notebook on the same index in server_notebooks
		List<List<Note>> notes = new ArrayList<List<Note>>();
		//d
		for(int i = 0; i < server_notebooks.size(); i++){
			notes.add(getNotesInNotebookFromServer(server_notebooks.get(i)));
		}
		//delete all notebooks and notes(ON DELETE CASCADE)
		for(int i = 0; i < notebooks.size(); i++){
			Notebook notebook = notebooks.get(i);
			databaseManager.deleteNotebook(notebook);
		}
		notebooks.clear();
		
		for(int i = 0; i < server_notebooks.size(); i++){
			Notebook notebook = server_notebooks.get(i);
			notebook.set_user_id(user_id);
			notebook.set_server(pepperNoteServer.get_id());
			notebook.set_id(databaseManager.addNotebook(notebook));			
			notebooks.add(notebook);
			List<Note> nb_notes = notes.get(i);
			for(int j = 0; j < nb_notes.size(); j++){
				nb_notes.get(j).set_notebook_id(notebook.get_id());
				databaseManager.addNote(nb_notes.get(j));
			}
		}		
	}
	
	public void clearDatabase(){
		for(int i = 0; i < notebooks.size(); i++){
			Notebook notebook = notebooks.get(i);
			databaseManager.deleteNotebook(notebook);
		}
		List<Change> changes = getChangesFromDB();
		for(int i = 0; i < changes.size(); i++){
			Change change = changes.get(i);
			databaseManager.deleteChange(change);
		}
		notebooks.clear();
		//changes.clear();
		conflicts.clear();
	}
	
	public List<Notebook> getNotebooksFromServer() throws ClientProtocolException, IOException, JSONException{
		 return httpRequestManager.getNotebooks();
	};
	public List<Note> getNotesInNotebookFromServer(Notebook notebook) throws ClientProtocolException, IOException, JSONException{
		return httpRequestManager.getNotes(notebook);
	};
	
	//Database
	public List<Change> getChangesFromDB(){
		return databaseManager.getAllChangesByUserIdAndServer(user_id, pepperNoteServer);
	};
	public void createChange(int entity_id, int version, int type_of_change, int type_of_entity){
		Change change = new Change(entity_id,version,user_id,type_of_change,type_of_entity,pepperNoteServer.get_id());		
		databaseManager.addChange(change);
		//changes.add(change);
	};
	public void deleteChange(Change change){
		databaseManager.deleteChange(change);
		//changes.remove(change);		
	};
	public void updateChange(Change change){
		databaseManager.updateChange(change);
	};	
	
	public void getNotebooksFromDB(){
		notebooks.clear();
		notebooks.addAll((ArrayList<Notebook>)databaseManager.getAllNotebooksByUserIdAndServer(user_id, pepperNoteServer));		
	};
	public List<Note> getNotesByNotebookFromDB(Notebook notebook){
		return databaseManager.getAllNotesByNotebookId(notebook);
	};	
	
	public Change existsChange(int entity_id, int type_of_entity){
		List<Change> changes = getChangesFromDB();
		for(int i=0; i < changes.size(); i++){
			if((changes.get(i).get_entity_id() == entity_id) && (changes.get(i).get_type_of_entity() == type_of_entity)){
				return changes.get(i);
			}
		}
		return null;
	}
	
	public void removeExistingConflict(int entity_id, int type_of_entity){
		for(int i = 0; i < conflicts.size(); i++){
			if(conflicts.get(i).getType_of_entity() == type_of_entity){
				switch(type_of_entity){
				case NOTEBOOK_ENTITY:
					Notebook nb = (Notebook)conflicts.get(i).getEntity();
					if(nb.get_id() == entity_id){
						if(conflicts.get(i).getType_of_change() == Change.CREATE_CHANGE){
							Change change = existsChange(entity_id, type_of_entity);
							if(change != null){
								change.set_type_of_change(Change.CREATE_CHANGE);
								updateChange(change);
							}
						}
						conflicts.remove(i);
						return;
					}
				case NOTE_ENTITY:
					Note note = (Note)conflicts.get(i).getEntity();
					if(note.get_id() == entity_id){
						if(conflicts.get(i).getType_of_change() == Change.CREATE_CHANGE){
							Change change = existsChange(entity_id, type_of_entity);
							if(change != null) { 
								change.set_type_of_change(Change.CREATE_CHANGE);
								updateChange(change);
							}
						}
						conflicts.remove(i);
						return;
					}
				}
			}
		}
		return;
	}
	
	public Notebook createNotebook(Notebook notebook, boolean isOnline){
		notebook.set_user_id(user_id);
		notebook.set_server(pepperNoteServer.get_id());
		notebook.set_id(databaseManager.addNotebook(notebook));	
		createChange(notebook.get_id(), 0, Change.CREATE_CHANGE, NOTEBOOK_ENTITY);
		notebooks.add(notebook);
		return notebook;	
	};
	
	public Notebook createNotebookOnServer(Notebook notebook) throws JSONException{
		try {
			return httpRequestManager.createNotebook(notebook);
		} catch (BadRequestException e) {
			conflicts.add(new ServerConflict(NOTEBOOK_ENTITY,Change.CREATE_CHANGE,ServerConflict.TAKEN_VALUE, notebook));
			
			return notebook;
		} catch (IOException e){
			createChange(notebook.get_id(), 0,Change.CREATE_CHANGE, NOTEBOOK_ENTITY);
			return notebook;
		}
	}
	public Notebook deleteNotebook(Notebook notebook, boolean isOnline){
		databaseManager.deleteNotebook(notebook);
		Change previousChange = existsChange(notebook.get_id(), NOTEBOOK_ENTITY);
		if(previousChange == null){	
			if(notebook.get_server_id() == 0){
				removeExistingConflict(notebook.get_id(), NOTEBOOK_ENTITY);
			} else {
				createChange(notebook.get_server_id(), notebook.get_version(), Change.DELETE_CHANGE, NOTEBOOK_ENTITY);
				removeExistingConflict(notebook.get_id(), NOTEBOOK_ENTITY);
			}
		} else if(previousChange.get_type_of_change() == Change.CREATE_CHANGE){
			//changes.remove(previousChange);
			deleteChange(previousChange);
		} else if(previousChange.get_type_of_change() == Change.UPDATE_CHANGE){
			previousChange.set_type_of_change(Change.DELETE_CHANGE);
			previousChange.set_entity_id(notebook.get_server_id());
			updateChange(previousChange);
		}
		return notebook;
	};
	public Notebook deleteNotebookOnServer(Notebook notebook, Change previousChange) throws JSONException {
		try {
			Notebook server_response_notebook =  httpRequestManager.deleteNotebook(notebook);
		
			return server_response_notebook;
		} catch (NotFoundException e) {
			return notebook;
		} catch (IOException e){
			if(previousChange == null){				
				createChange(notebook.get_server_id(), notebook.get_version(), Change.DELETE_CHANGE, NOTEBOOK_ENTITY);
			} else if(previousChange.get_type_of_change() == Change.CREATE_CHANGE){
				//changes.remove(previousChange);
				deleteChange(previousChange);
			} else if(previousChange.get_type_of_change() == Change.UPDATE_CHANGE){
				previousChange.set_type_of_change(Change.DELETE_CHANGE);
				previousChange.set_entity_id(notebook.get_server_id());
				updateChange(previousChange);
			}
			return notebook;
		} catch (ConflictException e){
			String result = e.getMessage();
			JSONObject json = new JSONObject(result);
			Notebook response_nb = new Notebook();
			response_nb.set_id(0);
			response_nb.set_server_id(json.getInt("id")); 
			response_nb.set_user_id(json.getInt("user_id")); 
			response_nb.set_version(json.getInt("version"));
			response_nb.set_name(json.getString("name"));
			conflicts.add(new ServerConflict(NOTEBOOK_ENTITY,Change.DELETE_CHANGE ,ServerConflict.NEW_VERSION, response_nb));
			return response_nb;
		}
	}
	
	public Notebook updateNotebook(Notebook notebook, boolean isOnline){
		databaseManager.updateNotebook(notebook);
		Change previousChange = existsChange(notebook.get_id(), NOTEBOOK_ENTITY);
		if(previousChange == null){
			createChange(notebook.get_id(), notebook.get_version(), Change.UPDATE_CHANGE, NOTEBOOK_ENTITY);
			removeExistingConflict(notebook.get_id(), NOTEBOOK_ENTITY);
		}
		return notebook;
	};
	public Notebook updateNotebookOnServer(Notebook notebook, Change previousChange) throws JSONException{
		try {
			return httpRequestManager.updateNotebook(notebook);
		} catch (NotFoundException e) {
			conflicts.add(new ServerConflict(NOTEBOOK_ENTITY, Change.UPDATE_CHANGE ,ServerConflict.ID_NOT_FOUND, notebook));
			return notebook;
		} catch (BadRequestException e) {
			conflicts.add(new ServerConflict(NOTEBOOK_ENTITY, Change.UPDATE_CHANGE ,ServerConflict.TAKEN_VALUE, notebook));
			return notebook;
		} catch (IOException e) {
			if(previousChange == null){
				createChange(notebook.get_id(), notebook.get_version(), Change.UPDATE_CHANGE, NOTEBOOK_ENTITY);
			}				
			return notebook;
		} catch (ConflictException e) {
			String result = e.getMessage();
			JSONObject json = new JSONObject(result);
			Notebook response_nb = new Notebook();
			response_nb.set_server_id(json.getInt("id")); 
			response_nb.set_user_id(json.getInt("user_id")); 
			response_nb.set_version(json.getInt("version"));
			response_nb.set_name(json.getString("name"));
			response_nb.set_id(notebook.get_id());
			response_nb.set_server(notebook.get_server());
			conflicts.add(new ServerConflict(NOTEBOOK_ENTITY,Change.UPDATE_CHANGE ,ServerConflict.NEW_VERSION, response_nb));
			return response_nb;
		}
	}
	
	public Notebook getNotebookById(int id){
		return databaseManager.getNotebook(id);
	}
	
	public Note createNote(Note note, Notebook nb, boolean isOnline){
		note.set_notebook_id(nb.get_id());
		note.set_id(databaseManager.addNote(note));
		createChange(note.get_id(), 0, Change.CREATE_CHANGE, NOTE_ENTITY);
		return note;
	};
	public Note createNoteOnServer(Note note, Notebook nb) throws JSONException{
		try {
			return httpRequestManager.createNote(note, nb);
		} catch (BadRequestException e) {
			conflicts.add(new ServerConflict(NOTE_ENTITY, Change.CREATE_CHANGE ,ServerConflict.TAKEN_VALUE, note));
			return note;
		} catch (IOException e){
			createChange(note.get_id(), 0,Change.CREATE_CHANGE, NOTE_ENTITY);
			return note;
		} catch (NotFoundException e) {
			conflicts.add(new ServerConflict(NOTE_ENTITY, Change.CREATE_CHANGE ,ServerConflict.ID_NOT_FOUND, note));
			return note;
		}
	}
	public Note deleteNote(Note note, boolean isOnline){
		databaseManager.deleteNote(note);
		Change previousChange = existsChange(note.get_id(), NOTE_ENTITY);
		if(previousChange == null){		
			if(note.get_server_id() == 0){
				removeExistingConflict(note.get_id(), NOTE_ENTITY);
			} else {
				createChange(note.get_server_id(), note.get_version(), Change.DELETE_CHANGE, NOTE_ENTITY);
				removeExistingConflict(note.get_id(), NOTE_ENTITY);
			}
		} else if(previousChange.get_type_of_change() == Change.CREATE_CHANGE){
			//changes.remove(previousChange);
			deleteChange(previousChange);
		} else if(previousChange.get_type_of_change() == Change.UPDATE_CHANGE){
			previousChange.set_type_of_change(Change.DELETE_CHANGE);
			previousChange.set_entity_id(note.get_server_id());
			updateChange(previousChange);
		}
		return note;
	};
	public Note deleteNoteOnServer(Note note, Change previousChange) throws JSONException{
		try {
			Note server_response_note = httpRequestManager.deleteNote(note);
			return server_response_note;
		} catch (NotFoundException e) {;
			return note;
		} catch (IOException e){
			if(previousChange == null){				
				createChange(note.get_server_id(), note.get_version(), Change.DELETE_CHANGE, NOTE_ENTITY);
			} else if(previousChange.get_type_of_change() == Change.CREATE_CHANGE){
				//changes.remove(previousChange);
				deleteChange(previousChange);
			} else if(previousChange.get_type_of_change() == Change.UPDATE_CHANGE){
				previousChange.set_type_of_change(Change.DELETE_CHANGE);
				previousChange.set_entity_id(note.get_server_id());
				updateChange(previousChange);
			}
			return note;
		} catch (ConflictException e) {
			String result = e.getMessage();
			JSONObject json = new JSONObject(result);
			Note response_note = new Note();
			response_note.set_id(0);
			response_note.set_server_id(json.getInt("id")); 
			response_note.set_notebook_id(json.getInt("notebook_id")); 
			response_note.set_version(json.getInt("version"));
			response_note.set_title(json.getString("title"));
			response_note.set_content(json.getString("content"));
			conflicts.add(new ServerConflict(NOTE_ENTITY, Change.DELETE_CHANGE ,ServerConflict.NEW_VERSION, response_note));
			return response_note;
		}
	}
	public Note updateNote(Note note, boolean isOnline){
		databaseManager.updateNote(note);
		Change previousChange = existsChange(note.get_id(), NOTE_ENTITY);
		if(previousChange == null){
			createChange(note.get_id(), note.get_version(), Change.UPDATE_CHANGE, NOTE_ENTITY);
			removeExistingConflict(note.get_id(), NOTE_ENTITY);
		}
		return note;
	};
	public Note updateNoteOnServer(Note note, Change previousChange) throws JSONException{
		try {
			return httpRequestManager.updateNote(note);
		} catch (NotFoundException e) {
			conflicts.add(new ServerConflict(NOTE_ENTITY, Change.UPDATE_CHANGE ,ServerConflict.ID_NOT_FOUND, note));
			return note;
		} catch (BadRequestException e) {
			conflicts.add(new ServerConflict(NOTE_ENTITY, Change.UPDATE_CHANGE ,ServerConflict.TAKEN_VALUE, note));
			return note;
		} catch (IOException e){
			if(previousChange == null){
				createChange(note.get_id(), note.get_version(), Change.UPDATE_CHANGE, NOTE_ENTITY);
			}
			return note;
		}
	}	
	
	public Note getNoteById(int id){
		return databaseManager.getNote(id);
	}
	
	public HttpRequestManager getHttpRequestManager() {
		return httpRequestManager;
	}
	public void setHttpRequestManager(HttpRequestManager httpRequestManager) {
		this.httpRequestManager = httpRequestManager;
	}
	public DatabaseManager getDatabaseManager() {
		return databaseManager;
	}
	public void setDatabaseManager(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}
	public PepperNoteServer getPepperNoteServer() {
		return pepperNoteServer;
	}
	public void setPepperNoteServer(PepperNoteServer pepperNoteServer) {
		this.pepperNoteServer = pepperNoteServer;
	}
	public int getUser_id() {
		return user_id;
	}
	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}
	public List<Notebook> getNotebooks() {
		return notebooks;
	}
	public void setNotebooks(List<Notebook> notebooks) {
		this.notebooks = notebooks;
	}

	public List<Change> getChanges() {
		return getChangesFromDB();
	}

	public List<ServerConflict> getConflicts() {
		return conflicts;
	}

	public void setConflicts(List<ServerConflict> conflicts) {
		this.conflicts = conflicts;
	}	
}
