package it.debug;

import java.sql.SQLException;

import it.exception.registration.UserAlreadyExistsException;
import it.registration.RegistrationLogic;

public class DebugRegistrationLogic {

	public static void main(String[] args) throws SQLException, UserAlreadyExistsException {
		RegistrationLogic.store("wewe4", "questačunapassword4");

	}

}
