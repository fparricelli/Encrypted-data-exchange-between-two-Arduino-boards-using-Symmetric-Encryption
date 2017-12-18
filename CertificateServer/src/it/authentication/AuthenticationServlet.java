package it.authentication;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.accessControl.IDS;
import it.authentication.twosteps.TwoStepsManager;
import it.exception.authentication.InvalidHopException;
import it.exception.authentication.LockedIP;
import it.exception.authentication.LockedUser;
import it.exception.authentication.NoSuchUserException;
import it.utility.MutableBoolean;
import it.utility.MutableInteger;
import it.utility.network.HTTPCodesClass;
import it.utility.network.HTTPCommonMethods;

/*
 * TODO L'intera struttura di autenticazione è ancora in uno stato intermedio. 
 * Per quanto HTTPS\SSL dovrebbe garantirci sicurezza, sarebbe meglio aggiungere un altro strato di sicurezza
 */

public class AuthenticationServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3997194962473053994L;
	private static final Integer HTTP_SUCCESS = HTTPCodesClass.SUCCESS;
	private static final Integer HTTP_UNAUTHORIZED = HTTPCodesClass.UNAUTHORIZED;
	private static final Integer HTTP_SERVER_INTERNAL_ERROR = HTTPCodesClass.SERVER_ERROR;

	/*
	 * In maniera sintetica:
	 * 
	 * Il client manda (user,pwd)
	 * 
	 * Il server manda:
	 * 
	 * 200 OK + token <=> user e password sono corretti 401 unauthorized ->
	 * combinazione user e password non corretti 403 forbidden -> c'è un account
	 * lockdown su (user,ip) 500 INTERNAL_SERVER_ERROR se si è verificato un errore
	 * interno al server 400 BAD_REQUEST se si usa un token non valido NIENTE:
	 * errore IOException, non posso aprire l'OutputStream e quindi non posso
	 * comunicare (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		Integer httpCode = null;
		String current = null;

		try {
			OutputStream out = response.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(out);
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			MutableBoolean needsUpdate = new MutableBoolean(false);
			MutableBoolean lockTimeout = new MutableBoolean(false);
			MutableInteger failed_account_attempts = new MutableInteger();
			HashMap<String,String> returnParameters = new HashMap<String,String>();
			byte[] bytes;
			synchronized (AuthenticationServlet.class) {
			if(IDS.isIPLocked(request.getRemoteAddr(), needsUpdate, lockTimeout, failed_account_attempts))
			{
				throw new LockedIP();
			}
			if (IDS.isLockedOut(username, request.getRemoteAddr())) {
				System.out.println("Account is locked");
				throw new LockedUser();
			}
			Boolean authenticated = AuthenticationLogic.authenticate(username, password);
			if (authenticated) {
				if (AuthenticationLogic.isTrusted(username, request.getRemoteAddr())) {
					AuthenticationLogic.deleteFailedLogins(username, request.getRemoteAddr());
					String authenticationToken = AuthenticationLogic.generateAuthenticationToken(username,request.getRemoteAddr());
					System.out.println("[Server]Auth token:"+authenticationToken);
					returnParameters.put("token", authenticationToken);
					returnParameters.putAll(AuthenticationLogic.getUserDetails(username));
					int roleNumber = extractRoleNumber(returnParameters.get("role"));
					returnParameters.put("roleN", String.valueOf(roleNumber));
					httpCode = HTTP_SUCCESS;
					response.setContentType("application/octet-stream");
					oos.writeObject(returnParameters);
					response.setStatus(httpCode);
					oos.flush();
				} else {
					TwoStepsManager.sendMail(username,request.getRemoteAddr());
					HTTPCommonMethods.sendReplyHeaderOnly(response,HTTPCodesClass.TEMPORARY_REDIRECT);
				}

			} else {
			
					AuthenticationLogic.handleFailedLogin(username, request.getRemoteAddr(),needsUpdate,lockTimeout,failed_account_attempts);
			
				
				httpCode = HTTP_UNAUTHORIZED;
				HTTPCommonMethods.sendReplyHeaderOnly(response, httpCode);
			}
			
			}

		}

		// codice di risposta
		catch(LockedIP e)
		{
			httpCode = e.getHttpCode();
			try {
				HTTPCommonMethods.sendReplyHeaderOnly(response, httpCode);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		catch (LockedUser e) {
			httpCode = e.getHttpCode();
			try {
				HTTPCommonMethods.sendReplyHeaderOnly(response, httpCode);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (NoSuchUserException e) {
			httpCode = e.getHttpCode();
			try {
				HTTPCommonMethods.sendReplyHeaderOnly(response, httpCode);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch (IllegalArgumentException e) {
			httpCode = HTTP_SERVER_INTERNAL_ERROR;
			try {
				HTTPCommonMethods.sendReplyHeaderOnly(response, httpCode);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		} catch (InvalidHopException e) {
			httpCode = e.getHttpCode();
			try {
				HTTPCommonMethods.sendReplyHeaderOnly(response, httpCode);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			httpCode = HTTP_SERVER_INTERNAL_ERROR;
			try {
				HTTPCommonMethods.sendReplyHeaderOnly(response, httpCode);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
	
	private int extractRoleNumber(String role) {
		
		if(role.equals("tecnico")) {
			return 2;
		}else if(role.equals("utente")) {
			return 3;
		}else{
			return 1;
		}
	}
	
	
	
	

}