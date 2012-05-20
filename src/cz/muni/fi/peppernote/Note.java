package cz.muni.fi.peppernote;

public class Note {
	
	//private variables
    private int _id;
    private int _server_id;
    private int _notebook_id;
    private int _version;
    private String _title;
    private String _content;
    
    
    // Empty constructor
    public Note(){
 
    }
    // constructor
    public Note(int _id, int _server_id, int _notebook_id, int _version, String _title, String _content) {
		this._id = _id;
		this._server_id = _server_id;
		this._notebook_id = _notebook_id;
		this._version = _version;
		this._title = _title;
		this._content = _content;
	}
	// constructor
    public Note(int _server_id, int _notebook_id, int _version, String _title, String _content) {
		this._server_id = _server_id;
		this._notebook_id = _notebook_id;
		this._version = _version;
		this._title = _title;
		this._content = _content;
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
	public int get_notebook_id() {
		return _notebook_id;
	}
	public void set_notebook_id(int _notebook_id) {
		this._notebook_id = _notebook_id;
	}
	public int get_version() {
		return _version;
	}
	public void set_version(int _version) {
		this._version = _version;
	}
	public String get_title() {
		return _title;
	}
	public void set_title(String _title) {
		this._title = _title;
	}
	public String get_content() {
		return _content;
	}
	public void set_content(String _content) {
		this._content = _content;
	}
	
	public String toString(){
		return _title;
	}
	
	public static boolean isValidTitle(String title){
		if((title.length() > 0) && title.length() <= 51 ){
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isValid(){
		if((_title.length() > 0) && _title.length() <= 51 ){
			return true;
		} else {
			return false;
		}
	}
}
