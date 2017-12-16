package it.sm.exception;

public class PasswordCheckFailedException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;
	
	public PasswordCheckFailedException(String errmsg) {
		this.message = errmsg;
	}
	
	public String getMessage() {
		return this.message;
	}

}
