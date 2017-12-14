package it.sm.exception;

public class AccessDeniedException extends Exception{

	/**
	 * 
	 */
	private final String message = "Access Denied";
	private final String deniedURL;
	
	
	private static final long serialVersionUID = 1L;

	public AccessDeniedException(String durl) {
		this.deniedURL = durl;
	}
	
	public AccessDeniedException() {
		this.deniedURL = "";
	}

	public String getMessage() {
		return message;
	}

	public String getDeniedURL() {
		return this.deniedURL;
	}
	
	
}
