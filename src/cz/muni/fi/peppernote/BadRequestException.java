package cz.muni.fi.peppernote;

public class BadRequestException extends Exception {
	String exception;

	public BadRequestException() {
		super();
		exception="Unknown";
	}
	
	public BadRequestException(String ex) {
		super();
		this.exception = ex;
	}
	
	public String getMessage(){
		return this.exception;
	}
}
