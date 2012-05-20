package cz.muni.fi.peppernote;

public class UnauthorizedException extends Exception {
	String exception;

	public UnauthorizedException() {
		super();
		exception="Unknown";
	}
	
	public UnauthorizedException(String ex) {
		super();
		this.exception = ex;
	}
	
	public String getMessage(){
		return this.exception;
	}
}
