package it.exception.registration;

import it.exception.ServletException;
import it.utility.network.HTTPCodesClass;

public class UserAlreadyExistsException extends ServletException {
	private Integer httpCode = HTTPCodesClass.CONFLICT;
	private String message = "Username already taken";

}
