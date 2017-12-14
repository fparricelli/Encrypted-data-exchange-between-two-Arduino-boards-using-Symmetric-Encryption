package it.sm.exception;

public class PolicyConflictException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String message = "Policy Conflict detected!";

	public PolicyConflictException() {
		
	}
	
	public String getMessage() {
		return this.message;
	}
	
	
	
}
