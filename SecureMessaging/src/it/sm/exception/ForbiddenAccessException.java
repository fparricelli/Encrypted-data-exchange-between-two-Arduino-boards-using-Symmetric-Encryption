package it.sm.exception;

public class ForbiddenAccessException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String message = "Forbidden Access";
	
	public ForbiddenAccessException() {
		
	}

	public String getMessage() {
		return this.message;
	}
	
	
}


