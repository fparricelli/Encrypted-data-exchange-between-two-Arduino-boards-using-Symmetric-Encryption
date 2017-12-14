package it.sm.exception;

public class ServerErrorException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final String message = "Server Error";
	
	public ServerErrorException() {
		
	}
	
	public String getMessage() {
		return this.message;
	}
}
