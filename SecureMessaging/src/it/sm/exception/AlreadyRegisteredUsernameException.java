package it.sm.exception;

public class AlreadyRegisteredUsernameException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String message = "Username already taken";
	
	public AlreadyRegisteredUsernameException() {
		
	}

	public String getMessage() {
		return this.message;
	}
	
}
