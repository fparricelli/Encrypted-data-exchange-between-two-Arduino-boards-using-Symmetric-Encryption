package it.sm.exception;

public class RedirectToLoginException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String message = "Parameter Error, redirect to login";

	public RedirectToLoginException() {
		
	}
	
	
	public String getMessage() {
		return this.message;
	}
	
}