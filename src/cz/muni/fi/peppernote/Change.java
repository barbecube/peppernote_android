package cz.muni.fi.peppernote;

public class Change {
	
	public static int CREATE_CHANGE = 1;
	public static int UPDATE_CHANGE = 2;
	public static int DELETE_CHANGE = 3;	
	
	private int _id;
	private int _entity_id;
	private int _version;
	private int _user_id;
	private int _type_of_change; // 1-create 2-update 3-delete
 	private int _type_of_entity; // 1-notebook 2-note
	private int _server;
	
	public Change() {
		
	}


	public Change(int _id, int _entity_id, int _version, int _user_id, int _type_of_change, int _type_of_entity, int _server) {
		this._id = _id;		
		this._version = _version;
		this._entity_id = _entity_id;
		this._user_id = _user_id;
		this._type_of_change = _type_of_change;
		this._type_of_entity = _type_of_entity;
		this._server = _server;
	}


	public Change(int _entity_id, int _version, int _user_id, int _type_of_change, int _type_of_entity, int _server) {
		this._entity_id = _entity_id;
		this._version = _version;
		this._user_id = _user_id;
		this._type_of_change = _type_of_change;
		this._type_of_entity = _type_of_entity;
		this._server = _server;		
	}


	
	public int get_version() {
		return _version;
	}


	public void set_version(int _version) {
		this._version = _version;
	}


	public int get_type_of_change() {
		return _type_of_change;
	}


	public void set_type_of_change(int _type_of_change) {
		this._type_of_change = _type_of_change;
	}


	public int get_id() {
		return _id;
	}


	public void set_id(int _id) {
		this._id = _id;
	}

	public int get_entity_id() {
		return _entity_id;
	}


	public void set_entity_id(int _entity_id) {
		this._entity_id = _entity_id;
	}
	
	public int get_type_of_entity() {
		return _type_of_entity;
	}


	public void set_type_of_entity(int _type_of_entity) {
		this._type_of_entity = _type_of_entity;
	}


	public int get_server() {
		return _server;
	}


	public void set_server(int _server) {
		this._server = _server;
	}


	public int get_user_id() {
		return _user_id;
	}


	public void set_user_id(int _user_id) {
		this._user_id = _user_id;
	}

	
	
	
	
}
