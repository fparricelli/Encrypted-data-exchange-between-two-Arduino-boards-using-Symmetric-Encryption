package it.authentication;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * TODO L'intera struttura di autenticazione è ancora in uno stato intermedio. 
 * Per quanto HTTPS\SSL dovrebbe garantirci sicurezza, sarebbe meglio aggiungere un altro strato di sicurezza
 */

public class AuthenticationServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3997194962473053994L;
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	{
		try {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		Boolean authenticated = AuthenticationLogic.authenticate(username, password);
		if(authenticated) {
		String authenticationToken = AuthenticationLogic.generateToken(username);
		}}
		catch (Exception e){
			e.printStackTrace();
		}
		//codice di risposta 
	}

}
