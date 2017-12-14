package it.authentication;

import java.security.interfaces.RSAKey;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;

import org.mindrot.jbcrypt.BCrypt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import it.dao.DAOUsers;
import it.exception.authentication.InvalidHopException;
import it.exception.authentication.NoSuchUserException;
import it.sm.keystore.rsakeystore.RSADevice;

public class AuthenticationLogic {
	private final static Integer tokenDurationMinutes = 15;
	private final static String issuer = "secure_messaging";
	private final static Integer maximumHops = 10;

	public static boolean authenticate(String username, String password) throws NoSuchUserException, SQLException {
		boolean authenticated = false;
		
	
		String bcrypted = DAOUsers.load_hash(username);

		if (BCrypt.checkpw(password, bcrypted)) {
			authenticated = true;

		}

		return authenticated;
	}

	public static String generateToken(String username, Integer hops)
			throws IllegalArgumentException, InvalidHopException, Exception {
		if (hops > maximumHops || hops < 0) {
			throw new InvalidHopException();
		}
		Builder tokenBuilder = JWT.create();
		String token = null;
		Integer interval = tokenDurationMinutes * 1000 * 60;
		Date issuedAt, expiresAt;
		tokenBuilder.withIssuer(issuer);
		tokenBuilder.withClaim("username", username);
		tokenBuilder.withClaim("hops", hops);
		issuedAt = new Date(System.currentTimeMillis());
		tokenBuilder.withIssuedAt(issuedAt);
		expiresAt = new Date(issuedAt.getTime() + interval);
		tokenBuilder.withExpiresAt(expiresAt);
		RSADevice rsa = RSADevice.getInstance();
		token = rsa.signToken(tokenBuilder);
		return token;
	}
	
	public static String generateAuthenticationToken(String username) throws IllegalArgumentException, InvalidHopException, Exception
	{
		return generateToken(username, maximumHops);
	}

	public static boolean isValidToken(String token, HashMap<String, Object> parameters) {
		boolean valid = false;
		long issuedAt, expiresAt;
		Integer hops;

		try {
			Algorithm algorithm = Algorithm.RSA512((RSAKey) RSADevice.getInstance().extractPublicKey());
			JWTVerifier verifier = JWT.require(algorithm).withIssuer(issuer).build();
			DecodedJWT tokenJWT = verifier.verify(token);
			issuedAt = tokenJWT.getIssuedAt().getTime();
			expiresAt = tokenJWT.getExpiresAt().getTime();
			hops = tokenJWT.getClaims().get("hops").asInt();
			
			if (expiresAt > issuedAt && (System.currentTimeMillis() < expiresAt) && hops>0 && hops<=maximumHops) {
				parameters.put("username", tokenJWT.getClaims().get("username").asString());
				parameters.put("hops", hops);
				valid = true;
			}

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return valid;
	}
	
	public static String regenToken (String token) throws IllegalArgumentException, InvalidHopException, Exception
	{
		String newToken = null, username = null;
		Integer newHops;
		HashMap<String, Object> parameters = new HashMap<String,Object>();
		if(isValidToken(token, parameters))
		{
			newHops =(Integer) parameters.get("hops") - 1;
		    if(newHops > 0)
		    {
		    	username = (String) parameters.get("username");
		    	token = generateToken(username, newHops);
		    }
			
		}
		return newToken;
	}

}
