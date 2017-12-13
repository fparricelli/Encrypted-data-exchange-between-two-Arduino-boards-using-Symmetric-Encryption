package it.exception.authorizaton;

public class InvalidRoleException extends Exception{

	private static final long serialVersionUID = 1L;
	private final String message = "[InvalidRoleException] Invalid Role";
	private final int httpCode = 400;
	
	public InvalidRoleException() {
		
	}

	public String getMessage() {
		return message;
	}

	public Integer getHttpCode() {
		return httpCode;
	}

	
	
	
	
	
}
