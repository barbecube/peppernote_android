package cz.muni.fi.peppernote;

public class ConflictException extends Exception {
	String exception;

	public ConflictException() {
		super();
		exception="Unknown";
	}
	
	public ConflictException(String ex) {
		super();
		this.exception = ex;
	}
	
	public String getMessage(){
		return this.exception;
	}
}
