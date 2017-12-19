package it.chat.helpers;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.security.auth.login.FailedLoginException;

import it.chat.user.AuthUser;
import it.sm.exception.AccessDeniedException;
import it.sm.exception.AlreadyRegisteredUsernameException;
import it.sm.exception.CertificateNotFoundException;
import it.sm.exception.CodeNotFoundException;
import it.sm.exception.ForbiddenAccessException;
import it.sm.exception.PolicyConflictException;
import it.sm.exception.RedirectToLoginException;
import it.sm.exception.ServerErrorException;
import it.sm.exception.TwoFactorRequiredException;
import it.sm.utility.network.HTTPCodesClass;

public class ServerHelper {
	
	private final String contactListPath = "./contact-lists";
	private final String configPath = "./configs/config.dat";
	
	
	
	
	private void trustLocalhost() {
		
		HttpsURLConnection.setDefaultHostnameVerifier(
			    new HostnameVerifier(){

			        public boolean verify(String hostname, SSLSession sslSession) {
			            if (hostname.equals("localhost")) {
			                return true;
			            }
			            return false;
			        }
			    });
	}
	
	
	
	
	public File getContactList(String listType, String currentRole, String token,AuthUser u) throws AccessDeniedException, PolicyConflictException, ServerErrorException, RedirectToLoginException{
	
	initializeContactListPath();
		
	try {
		Map<String, Object> params = new LinkedHashMap<>();
		
		int numList = Integer.valueOf(listType);
		int numRole = Integer.valueOf(currentRole);
		
		
		params.put("list", numList);
		params.put("ruolo", numRole);
		params.put("token", token);
		
		String postURL = readContactListURL()+listType+"/";
		
		HttpsURLConnection con = sendPost(postURL,params);
		
		int responseCode = con.getResponseCode();
			
		System.out.println("getContList respCode:"+responseCode);
		if(responseCode == HTTPCodesClass.SUCCESS) {
				
			
			//Definisco dove salvare la lista
			File f = new File(this.contactListPath+"/"+listType+"-list.xml");
			
			FileOutputStream fos = new FileOutputStream(f);
			InputStream is = con.getInputStream();
			
			
			byte[] buffer = new byte[4096];
			int length;
			   
			while ((length = is.read(buffer)) != -1) {
			    fos.write(buffer, 0, length);
			}
			
			String myToken = con.getHeaderField("NEWTOKEN");
			
			u.setToken(myToken);
			
			
			    
			fos.flush();
			fos.close();
			is.close();
			
			if(myToken == null) {
				throw new RedirectToLoginException();
			}
			    
			System.out.println("[getContactList] Lista "+listType+" scaricata!");
			System.out.println("[getContactList] Percorso:"+this.contactListPath+"/"+listType+"-list.xml");
			    
			return f;
			
		}else if(responseCode == HTTPCodesClass.UNAUTHORIZED) {
				
			throw new AccessDeniedException(postURL);
				
		}else if(responseCode == HTTPCodesClass.CONFLICT) {
				
			throw new PolicyConflictException();
				
		}else if(responseCode == HTTPCodesClass.TEMPORARY_REDIRECT){
				
			throw new RedirectToLoginException();
				
		}else {
			throw new ServerErrorException();
		}
		
	}catch(IOException e) {
		e.printStackTrace();
		throw new ServerErrorException();
	}
	
	
	
	}
	
	
	
	
	private void initializeContactListPath() {
		
		File f = new File(this.contactListPath);
		if(!f.exists()) {
			f.mkdirs();
		}
		
		
	}
	
	
	
	public File getCertificate(String nome, String cognome, String downloadPath) throws CertificateNotFoundException, ServerErrorException, RedirectToLoginException {
		
		
	try {	
		Map<String, Object> params = new LinkedHashMap<>();
		
		params.put("nome", nome);
		params.put("cognome", cognome);
		
		String postURL = readCertificateURL();
		
		HttpsURLConnection con = sendPost(postURL,params);
		
		int respCode =  con.getResponseCode();
		System.out.println("GET certificate code:"+respCode);
		if(respCode == HTTPCodesClass.SUCCESS) {
		
			File f = new File(downloadPath+nome.toLowerCase()+"_certificate.cer");
			FileOutputStream fos = new FileOutputStream(f);
			InputStream is = con.getInputStream();
		
			byte[] buffer = new byte[4096];
			int length;
		
			while ((length = is.read(buffer)) != -1) {
				fos.write(buffer, 0, length);
			}
		
			fos.flush();
			fos.close();
			is.close();
		
			return f;
			
		}else if(respCode == HTTPCodesClass.NOT_FOUND){
			throw new CertificateNotFoundException();
		}else {
			throw new ServerErrorException();
		}
		
	}catch(IOException e) {
		e.printStackTrace();
		throw new ServerErrorException();
	}
	
	
	
	}
	
	
	public AuthUser authenticate(String username, String password) throws FailedLoginException,TwoFactorRequiredException, ForbiddenAccessException, ServerErrorException {
	
		
	try {
		
		
		Map<String, Object> params = new LinkedHashMap<>();
		
		params.put("username", username);
		params.put("password", password);
		
		String postURL = readAuthenticationURL();
		
		HttpsURLConnection con = sendPost(postURL,params);
		System.out.println("Authenticate code:"+con.getResponseCode());
		if(con.getResponseCode() == HTTPCodesClass.SUCCESS){
		
			ObjectInputStream ois = new ObjectInputStream(con.getInputStream());
			
			@SuppressWarnings("unchecked")
			HashMap<String,String> respParam = (HashMap<String,String>)ois.readObject();
			
			String token = respParam.get("token");
			String tel = respParam.get("telephone");
			String role = respParam.get("role");
			String roleNumber = respParam.get("roleN");
			String name = respParam.get("name");
			String surname = respParam.get("surname");
			AuthUser u = new AuthUser(token,name,surname,role,Integer.valueOf(tel),Integer.valueOf(roleNumber));
			ois.close();
			return u;
		
		}else if(con.getResponseCode() == HTTPCodesClass.TEMPORARY_REDIRECT) {
			throw new TwoFactorRequiredException();
		}else if(con.getResponseCode() == HTTPCodesClass.UNAUTHORIZED) {
			throw new FailedLoginException();
		}else if(con.getResponseCode() == HTTPCodesClass.FORBIDDEN) {
			throw new ForbiddenAccessException();
		}else {
			throw new ServerErrorException();
		}
		
	}catch(IOException | ClassNotFoundException e) {
		e.printStackTrace();
		throw new ServerErrorException();
	}
	
	
		
	}
	
	public boolean validateTwoFactorCode(String username, String code) throws CodeNotFoundException, ServerErrorException, IllegalArgumentException {
		
		try {
			
			String postURL = readTwoFactorURL();
			Map<String, Object> params = new LinkedHashMap<>();
			
			params.put("username", username);
			params.put("code", code);
			
			HttpsURLConnection con = sendPost(postURL,params);
			System.out.println("Validate RESP:"+con.getResponseCode());
			if(con.getResponseCode() == HTTPCodesClass.SUCCESS) {
				
				return true;
				
			}else if(con.getResponseCode() == HTTPCodesClass.UNAUTHORIZED) {
				throw new IllegalArgumentException();
			}else if(con.getResponseCode() == HTTPCodesClass.NOT_FOUND){
				throw new CodeNotFoundException();
			}else {
				throw new ServerErrorException();
			}
			
		}catch(IOException e) {
			e.printStackTrace();
			throw new ServerErrorException();
		}
		
	}
	
	
	public void register(Map<String, Object> params) throws AlreadyRegisteredUsernameException, ServerErrorException{
		
	try {
		String postURL = readRegisterURL();
		
		HttpsURLConnection con = sendPost(postURL,params);
		
		if(con.getResponseCode() == HTTPCodesClass.SUCCESS) {
			
			System.out.println("Registrazione completata");
			
		}else if(con.getResponseCode() == HTTPCodesClass.CONFLICT) {
			throw new AlreadyRegisteredUsernameException();
		}else {
			throw new ServerErrorException();
		}
		
	}catch(IOException e) {
		e.printStackTrace();
		throw new ServerErrorException();
	}
		
		
		
	}
	
	
	private String readRegisterURL() throws IOException {
		
		String registerURL = null;
	try {
		
		String line2 = Files.readAllLines(Paths.get(this.configPath)).get(4);
		String [] p = line2.split("=");
		registerURL = p[1];
		
	}catch(Exception e) {
		e.printStackTrace();
	}
	
	if(registerURL != null) {
		return registerURL;
	}else {
		throw new IOException();
	}
		
	}
	
	
	
	private String readAuthenticationURL() throws IOException {
		
		String authenticationURL = null;
	try {
		
		String line2 = Files.readAllLines(Paths.get(this.configPath)).get(1);
		String [] p = line2.split("=");
		authenticationURL = p[1];
		
	}catch(Exception e) {
		e.printStackTrace();
	}
	
	if(authenticationURL != null) {
		return authenticationURL;
	}else {
		throw new IOException();
	}
		
	}
	
	
	private String readContactListURL() throws IOException {
		
		String contactListURL = null;
		try {
			
			String line1 = Files.readAllLines(Paths.get(this.configPath)).get(0);
			String [] p = line1.split("=");
			contactListURL = p[1];
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		if(contactListURL != null) {
			return contactListURL;
		}else {
			throw new IOException();
		}
	}
	
	
	private String readCertificateURL() throws IOException {
		
		String certificateURL = null;
		try {
			
			String line3 = Files.readAllLines(Paths.get(this.configPath)).get(2);
			String [] p = line3.split("=");
			certificateURL = p[1];
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		if(certificateURL != null) {
			return certificateURL;
		}else {
			throw new IOException();
		}
	}
	
	
	private String readTwoFactorURL() throws IOException {
		
		String twoFactorsURL = null;
		try {
			
			String line3 = Files.readAllLines(Paths.get(this.configPath)).get(3);
			String [] p = line3.split("=");
			twoFactorsURL = p[1];
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		if(twoFactorsURL != null) {
			return twoFactorsURL;
		}else {
			throw new IOException();
		}
		
	}
	
	
	private HttpsURLConnection sendPost(String postUrl, Map<String, Object> params) throws IOException {
		
		trustLocalhost();
		URL url = new URL(postUrl);
		
		StringBuilder postParams = new StringBuilder();
		
		for (Map.Entry<String, Object> param : params.entrySet()) {
			
			if (postParams.length() != 0) {
				postParams.append('&');
			}
			
			postParams.append(URLEncoder.encode(param.getKey(), "UTF-8"));
			postParams.append('=');
			postParams.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
		}
		
		
		byte[] postBytes = postParams.toString().getBytes("UTF-8");
		
		HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		con.setRequestProperty("Content-Length", String.valueOf(postBytes.length));
		con.setDoOutput(true);
		con.getOutputStream().write(postBytes);
		
		return con;
		
	}
	


}
