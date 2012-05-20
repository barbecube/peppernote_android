package cz.muni.fi.peppernote;

public class PepperNoteServer {

	private int _id;
    private String address;
	
    
    
    public PepperNoteServer() {
	
	}    
    
	public PepperNoteServer(int _id, String address) {
		this._id = _id;
		this.address = address;
	}
	
	
	
	
	
	public int get_id() {
		return _id;
	}
	public void set_id(int _id) {
		this._id = _id;
	}
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String toString(){
		return address;
	}
    
}
