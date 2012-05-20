package cz.muni.fi.peppernote;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ServerConflictJSON {
	private int type_of_entity;
	private int type_of_change;
	private int conflict;
	private String entityJSON;
	
	public static ServerConflict convertFromJSON(ServerConflictJSON s){
		ServerConflict serverConflict = new ServerConflict();
		Gson gson = new Gson();
		serverConflict.setConflict(s.getConflict());
		serverConflict.setType_of_change(s.getType_of_change());
		serverConflict.setType_of_entity(s.getType_of_entity());
		switch(s.getType_of_entity()){
			case PepperNoteManager.NOTE_ENTITY: serverConflict.setEntity(gson.fromJson(s.getEntityJSON(),Note.class)); break;
			case PepperNoteManager.NOTEBOOK_ENTITY: serverConflict.setEntity(gson.fromJson(s.getEntityJSON(),Notebook.class)); break;
		}
		return serverConflict;
	}
	
	public static ServerConflictJSON convertFromObject(ServerConflict s){
		ServerConflictJSON serverConflictJSON = new ServerConflictJSON();
		
		Gson gson = new Gson();
		serverConflictJSON.setType_of_entity(s.getType_of_entity());
		serverConflictJSON.setType_of_change(s.getType_of_change());
		serverConflictJSON.setConflict(s.getConflict());
		switch(s.getType_of_entity()){
			case PepperNoteManager.NOTE_ENTITY: serverConflictJSON.setEntityJSON(gson.toJson(s.getEntity(),Note.class)); break;
			case PepperNoteManager.NOTEBOOK_ENTITY: serverConflictJSON.setEntityJSON(gson.toJson(s.getEntity(),Notebook.class)); break;
		}		
		return serverConflictJSON;
	}

	public static String serversToString(List<ServerConflict> l){
		List<ServerConflictJSON> list = new ArrayList<ServerConflictJSON>();
		for(int i = 0; i < l.size();i++){
			list.add(convertFromObject(l.get(i)));
		}
		Gson gson = new Gson();
		Type collectionType = new TypeToken<List<ServerConflictJSON>>(){}.getType();
		String json = gson.toJson(list, collectionType);
		return json;
	}
	
	public static List<ServerConflict> jsonServersToList(String s){
		List<ServerConflict> list = new ArrayList<ServerConflict>();
		
		Gson gson = new Gson();
		Type collectionType = new TypeToken<List<ServerConflictJSON>>(){}.getType();
		List<ServerConflictJSON> jsonConflicts = gson.fromJson(s, collectionType);
		
		for(int i = 0; i < jsonConflicts.size();i++){
			list.add(convertFromJSON(jsonConflicts.get(i)));
		}
		return list;
	}
	
	public int getType_of_entity() {
		return type_of_entity;
	}

	public void setType_of_entity(int type_of_entity) {
		this.type_of_entity = type_of_entity;
	}

	public int getType_of_change() {
		return type_of_change;
	}

	public void setType_of_change(int type_of_change) {
		this.type_of_change = type_of_change;
	}

	public int getConflict() {
		return conflict;
	}

	public void setConflict(int conflict) {
		this.conflict = conflict;
	}

	public String getEntityJSON() {
		return entityJSON;
	}

	public void setEntityJSON(String entityJSON) {
		this.entityJSON = entityJSON;
	}
	
}
