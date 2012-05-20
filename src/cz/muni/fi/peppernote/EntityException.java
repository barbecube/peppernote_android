package cz.muni.fi.peppernote;

public class EntityException extends Exception {
	String exception;

	public EntityException() {
		super();
		exception="Unknown";
	}
	
	public EntityException(String ex) {
		super();
		this.exception = ex;
	}
	
	public String getMessage(){
		return this.exception;
	}
}
