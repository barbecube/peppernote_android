package cz.muni.fi.peppernote;

public class Notebook {

	//private variables
    private int _id;
    private int _server_id; // id ktore ma notebook na servery
    private int _user_id;
    private int _version;
    private String _name;
    private int _server;
    
    
    
    public Notebook() {
	
	}

	public Notebook(int _id, int _server_id, int _user_id, int _version, String _name, int _server) {
		this._id = _id;
		this._server_id = _server_id;
		this._user_id = _user_id;
		this._version = _version;
		this._name = _name;
		this._server = _server;
	}
	
	public Notebook(int _server_id, int _user_id, int _version, String _name, int _server ) {
		this._server_id = _server_id;
		this._user_id = _user_id;
		this._version = _version;
		this._name = _name;
		this._server = _server;
	}

	//
	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public int get_server_id() {
		return _server_id;
	}

	public void set_server_id(int _server_id) {
		this._server_id = _server_id;
	}

	public int get_user_id() {
		return _user_id;
	}

	public void set_user_id(int _user_id) {
		this._user_id = _user_id;
	}

	public int get_version() {
		return _version;
	}

	public void set_version(int _version) {
		this._version = _version;
	}

	public String get_name() {
		return _name;
	}

	public void set_name(String _name) {
		this._name = _name;
	}

	public int get_server() {
		return _server;
	}

	public void set_server(int _server) {
		this._server = _server;
	}
    
	public String toString(){
		return this._name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Notebook other = (Notebook) obj;
		if (_id != other._id)
			return false;
		return true;
	}
    
	public static boolean isValidName(String name){
		if((name.length() > 0) && name.length() <= 51 ){
			return true;
		} else {
			return false;
		}
	}
	
}
