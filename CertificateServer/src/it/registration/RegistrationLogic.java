package it.registration;

import org.mindrot.jbcrypt.BCrypt;

import it.dao.DAOUsers;

public class RegistrationLogic {
	private static Integer rounds = 12;
	
	public static void store(String username, String pasword)
	{
		String bcrypted = BCrypt.hashpw(pasword, BCrypt.gensalt(rounds));
		DAOUsers.store(username,bcrypted);
	}

}
