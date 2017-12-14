package it.sm.exception;

public class InvalidParametersException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String message = "Invalid Parameter forwarded with POST request";
	private String listp;
	private String rolep;
	
	public InvalidParametersException() {
		
	}
	
	public InvalidParametersException(String l, String c) {
		this.listp = l;
		this.rolep = c;
	}
	
	public String getMessage() {
		return this.message;
	}
	
	public String getListParameter() {
		return this.listp;
	}
	
	public String getRoleParameter() {
		return this.rolep;
	}

}
