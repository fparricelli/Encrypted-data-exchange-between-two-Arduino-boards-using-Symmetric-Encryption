package it.registration;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mindrot.jbcrypt.BCrypt;

public class RegistrationServlet extends HttpServlet {

	/**
	 * Vi si accede : https://localhost:8443/CertificateServer/register/
	 */
	private static final long serialVersionUID = 3287889096339954784L;
	private ServletConfig config;

	public RegistrationServlet() {
		super();
	}

	public void init(ServletConfig config) throws ServletException {
		this.config = config;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		System.out.println("Debug ricevuta richiesta");
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		RegistrationLogic.store(username, password);
	}

}
