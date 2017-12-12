package it.chat.helpers;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class ServerHelper {
	
	//Da rendere sicuro?
	private final String downloadURL = "https://localhost:8443/CertificateServer/contact-lists/";
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
	
	
	
	
	public int getContactList(String listType, String currentRole) {
		
		//Per accettare localhost come trusted durante la sessione SSL con il server
		trustLocalhost();

		try {
			//Costruisco la stringa di download in base alla lista richiesta
			String httpsURL = this.downloadURL+listType+"/";

			URL myurl = new URL(httpsURL);
			HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
			con.setRequestMethod("POST");
			
			//Costruisco la query string in base ai parametri forniti
			String query = "list="+listType.toLowerCase()+"&ruolo="+currentRole.toLowerCase();

			con.setDoOutput(true); 
			con.setDoInput(true);
			
			DataOutputStream output = new DataOutputStream(con.getOutputStream());  
			//Invio i parametri
			output.writeBytes(query);
			output.flush();
			output.close();
			
			//Recupero il response code
			int responseCode = con.getResponseCode();
			
			//Se response code == 200 (OK) e ho ricevuto octet-stream, vuol dire che sto scaricando la lista
			if(con.getContentType().contains("application/octet-stream") && responseCode == 200) {
				
				//Definisco dove salvare la lista
				File f = new File(this.contactListPath+"/"+listType+"-list.xml");
			
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
			    
			    return 0;
			    
			//Se ho response code = 200 (OK) ma ricevo risposta text/plain, possono essersi verificate due situazioni:
			// 1)Accesso negato (non posso accedere a quella lista contatti)
			// 2)Errore di applicazione policy (indeterminata/inapplicabile)
			// Restituisco errore in entrambi i casi, ma li distinguo.
			}else if(con.getContentType().contains("text/plain") && responseCode == 200) {
			
				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String input = new String("");
				String readInput =  new String("");

				while ((input = br.readLine()) != null){
			      readInput = input;
				}
			   
				br.close();
			   
				//Accesso Negato
				if(readInput.equals("Accesso negato!")) {
					System.out.println("[getContactList()] Accesso Negato!");
					return -1;
					
				//Errore di applicazione policy  
				}else{
					System.out.println("[getContactList()] Errore di Applicazione Policy!");
					return -2;
				   
				}
			   
			//Se ho un response code diverso da 200 OK, restituisco errore
			}else{
			 System.out.println("[getContactList()] Errore, response code:"+responseCode);
			 return -3;
			}
			
		}catch(Exception e) {
			e.printStackTrace();
			return -3;
		}
		
	}
	
	
	
	
	private void initializeContactListPath() {
		
		File f = new File(this.contactListPath);
		if(!f.exists()) {
			f.mkdirs();
		}
		
		
	}
	
	
	public String getContactListPath() {
		return this.contactListPath;
	}
	
	
	
	
	

}
