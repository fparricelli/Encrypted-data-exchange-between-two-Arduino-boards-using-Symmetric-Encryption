package it.chat.helpers;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.security.auth.login.FailedLoginException;
import it.sm.exception.AccessDeniedException;
import it.sm.exception.CertificateNotFoundException;
import it.sm.exception.InvalidParametersException;
import it.sm.exception.PolicyConflictException;
import it.sm.exception.ServerErrorException;
import it.sm.utility.network.HTTPCodesClass;

public class ServerHelper {
	
	//Da rendere sicuro
	private final String contactListPath = "./contact-lists";
	private final String configPath = "./configs/config.dat";
	
	public ServerHelper() {
		initializeContactListPath();
	}
	
	
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
	
	
	
	
	public File getContactList(String listType, String currentRole) throws AccessDeniedException, PolicyConflictException, InvalidParametersException, ServerErrorException, IOException {
		
		//Per accettare localhost come trusted durante la sessione SSL con il server
		trustLocalhost();
		File f = null;

		//Costruisco la stringa di download in base alla lista richiesta
		String httpsURL = readContactListURL()+listType+"/";

		URL myurl = new URL(httpsURL);
		HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
		con.setRequestMethod("POST");
			
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("list=");
		stringBuilder.append(URLEncoder.encode(listType.toLowerCase(),"UTF-8"));
		stringBuilder.append("&ruolo=");
		stringBuilder.append(URLEncoder.encode(currentRole.toLowerCase(),"UTF-8"));
		
		//Costruisco la query string in base ai parametri forniti
		String query = stringBuilder.toString();

		con.setDoOutput(true); 
		con.setDoInput(true);
			
		DataOutputStream output = new DataOutputStream(con.getOutputStream());  
		//Invio i parametri
		output.writeBytes(query);
		output.flush();
		output.close();
			
		//Recupero il response code
		int responseCode = con.getResponseCode();
			
			
		if(responseCode == HTTPCodesClass.SUCCESS) {
				
			//Definisco dove salvare la lista
			f = new File(this.contactListPath+"/"+listType+"-list.xml");
			
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
			    
			System.out.println("[getContactList] Lista "+listType+" scaricata!");
			System.out.println("[getContactList] Percorso:"+this.contactListPath+"/"+listType+"-list.xml");
			    
			return f;
			
		}else if(responseCode == HTTPCodesClass.UNAUTHORIZED) {
				
			throw new AccessDeniedException(httpsURL);
				
		}else if(responseCode == HTTPCodesClass.CONFLICT) {
				
			throw new PolicyConflictException();
				
		}else if(responseCode == HTTPCodesClass.BAD_REQUEST){
				
			throw new InvalidParametersException(listType, currentRole);
				
		}else {
			throw new ServerErrorException();
		}
			
			
	}
	
	
	
	
	private void initializeContactListPath() {
		
		File f = new File(this.contactListPath);
		if(!f.exists()) {
			f.mkdirs();
		}
		
		
	}
	
	
	public File getCertificate(String nome, String cognome, String downloadPath) throws IOException, CertificateNotFoundException, ServerErrorException {
		
		trustLocalhost();
		
		
		
			System.out.println("Certificato non trovato, lo scarico!");
			String httpsURL = readCertificateURL();

			URL myurl = new URL(httpsURL);
			HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
			con.setRequestMethod("POST");
		
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("nome=");
			stringBuilder.append(URLEncoder.encode(nome.toLowerCase(),"UTF-8"));
			stringBuilder.append("&cognome=");
			stringBuilder.append(URLEncoder.encode(cognome.toLowerCase(),"UTF-8"));
		
			String query = stringBuilder.toString();

			con.setDoOutput(true); 
			con.setDoInput(true);
		
			DataOutputStream output = new DataOutputStream(con.getOutputStream());  

			output.writeBytes(query);
			output.flush();
			output.close();

			int respCode =  con.getResponseCode();
		
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
			}else{
				throw new ServerErrorException();
			}
	}
	
	
	public String authenticate(String username, String password) throws FailedLoginException {
		
		String token = null;
		
	try {
		HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
		URL url = new URL(readAuthenticationURL());
		
		Map<String, Object> params = new LinkedHashMap<>();
		
		params.put("username", username);
		params.put("password", password);
		
		
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
		
		if(conn.getResponseCode()==200){
		
			Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			token = "";
			for (int c; (c = in.read()) >= 0;) {
				token = token.concat(String.valueOf((char)c));
			}
		
		}
		
	}catch(Exception e) {
		e.printStackTrace();
	}
		
		if(token != null) {
			return token;
		}else {
			throw new FailedLoginException();
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
	

}
