package it.debug.authentication;

import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.interfaces.RSAKey;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;

import it.authentication.AuthenticationLogic;
import it.exception.authentication.NoSuchUserException;
import it.sm.keystore.rsakeystore.RSADevice;
import it.sm.keystore.rsakeystore.RSASoftwareKeystore;

public class DebugAuthenticationLogic {

	public static void main(String[] args) {

		try {
			
			// checkBCrypt();
			//tryToken();
			//tokenExample();
			//tokenExample2();
			String token = DebugAuthenticationServlet.testServlet("Luca", "Pirozzi");
			String token2;
			System.out.println(token);
			token2 = AuthenticationLogic.regenToken(token);
			System.out.println("TOKEN RIGENERATO: " + token);
			System.out.println("I token sono diversi? " + Objects.equals(token, token2));
			boolean result = true;
			for (int i=0; i<token.length() && i<token2.length() && result == true; i++)
			{
				if(token.charAt(i)!=token2.charAt(i))
				{
					result = false;
				}
			}
			System.out.println("MI ASPETTO FALSE : " + result);
			result = true;
			for (int i=0; i<token.length() && i<token.length() && result == true; i++)
			{
				if(token.charAt(i)!=token.charAt(i))
				{
					result = false;
				}
			}
			System.out.println("MI ASPETO TRUE : " + result);
			
	   
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public static void tokenExample2 () throws Exception
	{
		String token = AuthenticationLogic.generateToken("username",10);
		System.out.println(AuthenticationLogic.isValidToken(token, new HashMap<>()));
	}
	public static void tokenExample () throws Exception
	{
	String  token = AuthenticationLogic.generateToken("username",10);
		String alias = "secure_messaging";
		Algorithm algo = Algorithm.RSA512((RSAKey) RSADevice.getInstance().extractPublicKey());
		
		JWTVerifier verifier = JWT.require(algo).withIssuer(alias).build(); // Reusable verifier instance
		DecodedJWT jwt = verifier.verify(token);
		System.out.println((jwt.getIssuer()));
		System.out.println(jwt.getIssuedAt());
		System.out.println(jwt.getExpiresAt());
		System.out.println(jwt.getClaims().get("username").asString());
	}

	@SuppressWarnings("unchecked")
	public static void tryToken() throws Exception {
		String username = "username";
		String token = null;
		String alias = "secure_messaging";
		String password = "changeit";
		String keystorePath = ".\\secure_place\\app_keystore.keystore";
		@SuppressWarnings("rawtypes")
		Map<String,Object> attributes = new HashMap();
		FileInputStream fis = new FileInputStream(keystorePath);
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(fis, password.toCharArray());
		Key key = ks.getKey(alias, password.toCharArray());
		Algorithm algo = Algorithm.RSA512((RSAKey) key);
		attributes.put("username", username);
		Integer secMill = 1000;
		Integer minSec = 60;
		token = JWT.create().withClaim("username", username).withIssuer(alias).withIssuedAt(new Date(System.currentTimeMillis())).withExpiresAt(new Date(System.currentTimeMillis()+15*secMill*minSec)).sign(algo);
		algo = Algorithm.RSA512((RSAKey) new RSASoftwareKeystore(keystorePath, alias, password).extractPublicKey());
		JWTVerifier verifier = JWT.require(algo).withIssuer(alias).build(); // Reusable verifier instance
		DecodedJWT jwt = verifier.verify(token);
		System.out.println((jwt.getIssuer()));
		System.out.println(jwt.getIssuedAt());
		System.out.println(jwt.getExpiresAt());
		System.out.println(jwt.getClaims().get("username").asString());
		//System.out.println(token);
	}

	public static void checkBCrypt() throws SQLException {
		/*
		 * Assicurarsi di aver importato il database.sql e che sia popolato con gli
		 * stessi valori, altrimenti non funziona
		 */

		boolean bool;
		try {
			bool = AuthenticationLogic.authenticate("username", "password");
			System.out.println(bool);
			bool = AuthenticationLogic.authenticate("wewe2", "questaèunapassword2");
			System.out.println(bool);
			bool = !AuthenticationLogic.authenticate("Questo", "Questo");
			System.out.println(bool);
			bool = !AuthenticationLogic.authenticate("wewe", "passwordacaso");
			System.out.println(bool);
		} catch (NoSuchUserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
}