package it.debug;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLEncoder;
import java.security.interfaces.RSAKey;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import it.authentication.AuthenticationLogic;
import it.sm.keystore.rsakeystore.RSADevice;

public class DebugAuthenticationServlet {
	public static void main(String[] args) throws Exception {
		//authenticationExample();
		//blockedExample();
		//causeALockDown();
		almostLockDown();

	}
	
	public static void almostLockDown () throws Exception
	{
		for (int i=0; i<4; i++)
		{
			testServlet("username","passworddiscretamentesbagliata");
		}
		testServlet("username","password");
	}
	
	public static void causeALockDown () throws Exception
	{
		for (int i=0; i<5; i++)
		{
			testServlet("wewe","passwordanchesbagliata");
		}
	}
	
	public static void blockedExample() throws Exception
	{
		testServlet("wewe","passwordanchesbagliata");
	}
	public static void authenticationExample () throws Exception
	{
		
		HashMap<String,Object> map= new HashMap<String,Object>();
		String token;
		token = testServlet("username", "password");
		System.out.println(token);
		System.out.println(AuthenticationLogic.isValidToken(token, map ));
		System.out.println("USER: " + map.get("username") + " HOPS: " + map.get("hops"));
	testServlet("useracaso", "passwordchenondovrebbeesseregiusta");
		testServlet("wewe","passwordanchesbagliata");
	}
	
	public static void decryptToken (String token) throws IllegalArgumentException, Exception
	{
		String alias = "secure_messaging";
		Algorithm algo = Algorithm.RSA512((RSAKey) RSADevice.getInstance().extractPublicKey());
		
		JWTVerifier verifier = JWT.require(algo).withIssuer(alias).build(); // Reusable verifier instance
		DecodedJWT jwt = verifier.verify(token);
		System.out.println((jwt.getIssuer()));
		System.out.println(jwt.getIssuedAt());
		System.out.println(jwt.getExpiresAt());
		System.out.println(jwt.getClaims().get("username").asString());
	}
	



public static String testServlet (String usr, String pwd) throws Exception
{
	
	{
		HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
		URL url = new URL("https://localhost:8443/CertificateServer/authenticate");
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("username", usr);
		params.put("password", pwd);
		String token = null;
		StringBuilder postData = new StringBuilder();
		for (Map.Entry<String, Object> param : params.entrySet()) {
			if (postData.length() != 0)
				postData.append('&');
			postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
			postData.append('=');
			postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
		}
		byte[] postDataBytes = postData.toString().getBytes("UTF-8");

		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		conn.setDoOutput(true);
		conn.getOutputStream().write(postDataBytes);
		
		System.out.println("HTTP CODE :" + conn.getResponseCode());
		if(conn.getResponseCode()==200)
		{
		Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		token = "";
		for (int c; (c = in.read()) >= 0;)
		token = token.concat(String.valueOf((char)c));
		
		}
		return token;
	}

}

}