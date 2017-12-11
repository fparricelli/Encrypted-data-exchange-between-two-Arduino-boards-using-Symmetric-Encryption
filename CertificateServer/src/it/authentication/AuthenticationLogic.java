package it.authentication;

import java.security.interfaces.RSAKey;
import java.util.Date;
import java.util.HashMap;

import org.mindrot.jbcrypt.BCrypt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sun.org.apache.bcel.internal.generic.IASTORE;

import it.dao.DAOUsers;
import it.sm.keystore.rsakeystore.RSADevice;

public class AuthenticationLogic {
	private final static Integer tokenDurationMinutes = 15;
	private final static String issuer = "secure_messaging";

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

	public static String generateToken(String username) throws Exception {
		Builder tokenBuilder = JWT.create();
		String token = null;
		Integer interval = tokenDurationMinutes * 1000 * 60;
		Date issuedAt, expiresAt;
		// TODO codice di building del token
		tokenBuilder.withIssuer(issuer);
		tokenBuilder.withClaim("username", username);
		issuedAt = new Date(System.currentTimeMillis());
		tokenBuilder.withIssuedAt(issuedAt);
		expiresAt = new Date(issuedAt.getTime() + interval);
		tokenBuilder.withExpiresAt(expiresAt);
		RSADevice rsa = RSADevice.getInstance();
		token = rsa.signToken(tokenBuilder);
		return token;
	}

	public static boolean isValidToken(String token, HashMap<String, String> parameters) {
		boolean valid = false;
		long interval = tokenDurationMinutes * 60 * 1000;
		long issuedAt, expiresAt;

		try {
			Algorithm algorithm = Algorithm.RSA512((RSAKey) RSADevice.getInstance().extractPublicKey());
			JWTVerifier verifier = JWT.require(algorithm).withIssuer(issuer).build();
			DecodedJWT tokenJWT = verifier.verify(token);
			issuedAt = tokenJWT.getIssuedAt().getTime();
			expiresAt = tokenJWT.getExpiresAt().getTime();
			if (expiresAt > issuedAt && (System.currentTimeMillis() < expiresAt)) {

				parameters.put("username", tokenJWT.getClaims().get("username").asString());
				valid = true;
			}

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return valid;
	}

}
