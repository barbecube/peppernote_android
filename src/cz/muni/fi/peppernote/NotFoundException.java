package cz.muni.fi.peppernote;

public class NotFoundException extends Exception {
	String exception;

	public NotFoundException() {
		super();
		exception="Unknown";
	}
	
	public NotFoundException(String ex) {
		super();
		this.exception = ex;
	}
	
	public String getMessage(){
		return this.exception;
	}
}
