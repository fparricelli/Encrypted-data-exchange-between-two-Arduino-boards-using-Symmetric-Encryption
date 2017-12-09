package it.registration;

import org.mindrot.jbcrypt.BCrypt;

import it.dao.DAOUsers;

public class RegistrationLogic {
	//Rounds determina il numero di passaggi hash da effettuare. 10 � considerato abbastanza sicuro, 12 molto. 
	//Il massimo � 30 ma il tempo aumenta esponenzialmente ad ogni passaggio: gi� 15 richiede molto tempo, 20 tantissimo.
	private static Integer rounds = 12;
	
	public static void store(String username, String pasword)
	{
		String bcrypted = BCrypt.hashpw(pasword, BCrypt.gensalt(rounds));
		DAOUsers.store(username,bcrypted);
	}

}