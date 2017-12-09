package it.authentication;

import org.mindrot.jbcrypt.BCrypt;

import it.dao.DAOUsers;

public class AuthenticationLogic {

	public static boolean authenticate(String username, String password) {
		boolean authenticated = false;
		String bcrypted = DAOUsers.load_hash(username);
		if (bcrypted != null) {
			if (BCrypt.checkpw(password, bcrypted)) {
				authenticated = true;
			}
		}

		return authenticated;
	}

}
