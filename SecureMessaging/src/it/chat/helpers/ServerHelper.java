package it.chat.helpers;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

import it.sm.exception.AccessDeniedException;
import it.sm.exception.CertificateNotFoundException;
import it.sm.exception.InvalidParametersException;
import it.sm.exception.PolicyConflictException;
import it.sm.exception.ServerErrorException;
import it.sm.utility.network.HTTPCodesClass;

public class ServerHelper {
	
	//Da rendere sicuro?
	private final String contactListDownloadURL = "https://localhost:8443/CertificateServer/contact-lists/";
	private final String contactListPath = "./contact-lists";
	
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
		String httpsURL = this.contactListDownloadURL+listType+"/";

		URL myurl = new URL(httpsURL);
		HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
		con.setRequestMethod("POST");
			
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("list=");
		stringBuilder.append(URLEncoder.encode(listType.toLowerCase()));
		stringBuilder.append("&ruolo=");
		stringBuilder.append(URLEncoder.encode(currentRole.toLowerCase()));
		
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
		File fc = null;
		
		
			System.out.println("Certificato non trovato, lo scarico!");
			String httpsURL = "https://localhost:8443/CertificateServer/getCertificate";

			URL myurl = new URL(httpsURL);
			HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
			con.setRequestMethod("POST");
		
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("nome=");
			stringBuilder.append(URLEncoder.encode(nome.toLowerCase()));
			stringBuilder.append("&cognome=");
			stringBuilder.append(URLEncoder.encode(cognome.toLowerCase()));
		
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
	
	
	
	

}
