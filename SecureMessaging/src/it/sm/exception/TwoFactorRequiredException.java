package it.sm.exception;

public class TwoFactorRequiredException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String message = "Two Factor authentication required";

	public TwoFactorRequiredException() {
		
	}
	
	public String getMessage() {
		return this.message;
	}
	
}
