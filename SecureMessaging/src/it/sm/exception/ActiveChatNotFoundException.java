package it.sm.exception;

public class ActiveChatNotFoundException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String message = "[ActiveChatNotFoundException] Active Chat not found!";
	
	public ActiveChatNotFoundException() {
		
	}
	
	public String getMessage() {
		return this.message;
	}

}
