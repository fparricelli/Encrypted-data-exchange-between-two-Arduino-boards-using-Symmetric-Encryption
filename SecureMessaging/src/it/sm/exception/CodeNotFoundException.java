package it.sm.exception;

public class CodeNotFoundException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String message = "Verification code not found!";
	
	public CodeNotFoundException() {
		
	}
	
	public String getMessage() {
		return this.message;
	}
	
	

}
