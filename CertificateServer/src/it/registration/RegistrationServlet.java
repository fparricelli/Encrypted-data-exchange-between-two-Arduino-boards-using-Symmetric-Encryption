package it.registration;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.xml.internal.ws.client.SenderException;

import it.exception.registration.MailAlreadyExistsException;
import it.exception.registration.UserAlreadyExistsException;
import it.utility.network.HTTPCodesClass;
import it.utility.network.HTTPCommonMethods;

public class RegistrationServlet extends HttpServlet {

	/**
	 * Vi si accede : https://localhost:8443/CertificateServer/register/
	 */
	private static final long serialVersionUID = 3287889096339954784L;

	
	public RegistrationServlet() {
		super();
	}


	/*
	 * Il do post restituisce:
	 * 
	 * 200 - Registrazione a buon fine
	 * 409 - Username o mail già presente
	 * 500 - errore interno al server
	 * NIENTE: IOException
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */

	protected void doPost(HttpServletRequest request, HttpServletResponse response)  {
		Integer httpCode = null;
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String name = request.getParameter("name");
		String surname = request.getParameter("surname");
		String email = request.getParameter("email");
		Integer telephone  = Integer.valueOf(request.getParameter("telephone"));
		try {
		RegistrationLogic.store(username, password,email,name,surname,telephone);
		httpCode = HTTPCodesClass.SUCCESS;
		HTTPCommonMethods.sendReplyHeaderOnly(response, httpCode);
		}
		
		catch (UserAlreadyExistsException e)
		{
			httpCode = HTTPCodesClass.CONFLICT;
			try {
				HTTPCommonMethods.sendReplyHeaderOnly(response, httpCode);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		catch(MailAlreadyExistsException e)
		{
			httpCode = e.getHttpCode();
			try {
				HTTPCommonMethods.sendReplyHeaderOnly(response, httpCode);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		
		catch (SQLException e)
		{
			httpCode = HTTPCodesClass.SERVER_ERROR;
			try {
				HTTPCommonMethods.sendReplyHeaderOnly(response, httpCode);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
