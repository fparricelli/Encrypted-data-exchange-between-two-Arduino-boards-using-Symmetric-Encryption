package it.debug;

import it.authentication.AuthenticationLogic;

public class DebugAuthenticationLogic {

	public static void main(String[] args) {
		/*
		 * Assicurarsi di aver importato il database.sql e che sia popolato con gli stessi valori,
		 * altrimenti non funziona
		 */
		
		boolean bool = AuthenticationLogic.authenticate("username", "password");
		System.out.println(bool);
		bool = AuthenticationLogic.authenticate("wewe2", "questaèunapassword2");
		System.out.println(bool);
		bool = !AuthenticationLogic.authenticate("Questo", "Questo");
		System.out.println(bool);
		bool = !AuthenticationLogic.authenticate("wewe", "passwordacaso");
		System.out.println(bool);
		
	}

}
