package it.exception.authorizaton;

public class InvalidListException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String message = "[InvalidListException] Invalid List";
	private final Integer httpCode = 400;
	
	public InvalidListException() {
		
	}

	public String getMessage() {
		return message;
	}

	public Integer getHttpCode() {
		return httpCode;
	}

	
	
	
	
	

}
