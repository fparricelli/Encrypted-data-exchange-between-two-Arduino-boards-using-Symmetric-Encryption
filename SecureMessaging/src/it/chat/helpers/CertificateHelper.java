package it.chat.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import it.sm.exception.CertificateNotFoundException;
import it.sm.exception.ServerErrorException;

public class CertificateHelper {
	
	private String currentUser;
	private String certificatesPath;
	private String truststorePath;
	private String keystorePath;
	private String dataFilePath;
	private KeyStore trustStore;
	
	private static CertificateHelper instance;
	
	
	public static CertificateHelper getInstance() {
		if (instance == null) {

			instance = new CertificateHelper();

		}

	return instance;

	}
	
	private CertificateHelper() {
		
	}
	
	public void init(String userName) {
		this.currentUser = userName;
		this.certificatesPath = "."+File.separator+"secure_place_"+userName.toLowerCase()+File.separator+"certificates"+File.separator+"";
		this.truststorePath = "."+File.separator+"secure_place_"+userName.toLowerCase()+File.separator+userName.toLowerCase()+"_truststore"+File.separator+userName.toLowerCase()+"_truststore.keystore";
		this.keystorePath = "."+File.separator+"secure_place_"+userName.toLowerCase()+File.separator+userName.toLowerCase()+"_keystore.keystore";
		this.dataFilePath = "."+File.separator+"secure_place_"+userName.toLowerCase()+File.separator+userName.toLowerCase()+"data.txt";
		initTrustStore();
		initKeystoreProperties();
		initSSLContext();
		
	}
	
	//Assumiamo che nel trust-store sia pre-caricato il certificato del server tomcat.
	private void initTrustStore() {
	try {
		this.trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		String [] p = extractParameters();
		if(p!= null) {
			
			char[] bytePassword = p[1].toCharArray();
			FileInputStream fis = new FileInputStream(this.truststorePath);
			this.trustStore.load(fis, bytePassword);
			
			String cm = System.getProperty("user.dir");
			String path = cm+this.truststorePath.substring(1);
			System.setProperty("javax.net.ssl.trustStore",path);
			System.setProperty("javax.net.ssl.trustStorePassword", p[1]);
			System.setProperty("javax.net.ssl.trustAnchors",path);
			
			fis.close();
		}
		
	}catch(Exception e) {
		e.printStackTrace();
	}
		
	}
	
	private void initKeystoreProperties() {
		String [] p = extractParameters();
		String cm = System.getProperty("user.dir");
		String path = cm+this.keystorePath.substring(1);
		
		System.setProperty("javax.net.ssl.keyStore",path);
		System.setProperty("javax.net.ssl.keyStorePassword", p[1]);
		
	}
	
	private void initSSLContext() {
		
	try {
		KeyStore ks = KeyStore.getInstance("JKS");
	    InputStream ksIs = new FileInputStream(this.keystorePath);
	    try {
	        ks.load(ksIs, extractParameters()[1].toCharArray());
	    } finally {
	        if (ksIs != null) {
	            ksIs.close();
	        }
	    }

	    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
	    kmf.init(ks, extractParameters()[1].toCharArray());
	    
	    TrustManager[] trustManagers = new TrustManager[] { 
			    new ReloadableX509TrustManager(this.truststorePath) 
	    };
	    
	    SSLContext sslContext = SSLContext.getInstance("SSL");
	    sslContext.init(kmf.getKeyManagers(), trustManagers, null);
	    SSLContext.setDefault(sslContext);
	
	}catch(Exception e) {
		e.printStackTrace();
	}
		
	}
	
	private String [] extractParameters() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(this.dataFilePath));
			String line1 = br.readLine();
			String line2 = br.readLine();
			br.close();
			
			String [] p1 = line1.split("=");
			String alias = p1[1];
			
			String [] p2 = line2.split("=");
			String password = p2[1];
			
			
			return new String [] {alias,password};
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public SSLContext getSSLContext(String tspath) throws Exception {
		  TrustManager[] trustManagers = new TrustManager[] { 
		    new ReloadableX509TrustManager(tspath) 
		  };
		  SSLContext sslContext = SSLContext.getInstance("SSL");
		  sslContext.init(null, trustManagers, null);
		  return sslContext;
	}
	
	
	
	
	
	
	
	
	private void addCertificate(File cert, String subjectAlias) {
		
		try {
			String [] p = extractParameters();
			FileInputStream fis1 = new FileInputStream(this.keystorePath);
			this.trustStore.load(fis1, p[1].toCharArray());
			
			CertificateFactory fact = CertificateFactory.getInstance("X.509");
		    FileInputStream fis = new FileInputStream (cert);
		    X509Certificate certs = (X509Certificate) fact.generateCertificate(fis);
			
			this.trustStore.setCertificateEntry(subjectAlias, certs);
			
			FileOutputStream fos = new FileOutputStream(this.truststorePath);
			
			this.trustStore.store(fos, p[1].toCharArray());
			fos.close();
			fis.close();
			
			
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	//In questo modo stiamo assumendo che non ci possano essere omonimi
	//Considera di modificare la funzione concatenando all'alias anche un userID unico (preso dal DB in fase di login)
	public void getCertificate(String nome, String cognome) throws CertificateNotFoundException, ServerErrorException {
		
		try {
			
			//Prima di tutto, controllo se il certificato richiesto si trova già nel mio trust-store
			//L'alias che dò al certificato nel trust-store è unico, non
			//mi permette di aggiungere certificati con lo stesso alias di altri
			String aliasName = nome+cognome+"_cer";
			boolean isCertificatePresent = this.trustStore.containsAlias(aliasName);
			ServerHelper sh = new ServerHelper();
			
			if(!isCertificatePresent) {
				
				//Se il certificato non è presente nel trust-store, allora devo scaricarlo
				File f = sh.getCertificate(nome, cognome, this.certificatesPath);
				addCertificate(f,aliasName);
				//Dopo averlo aggiunto al trust-store, posso anche eliminare il certificato
				f.delete();
				
				//NOTA: quando scarico il certificato, prima di aggiungerlo al trust-store, dovrei sempre
				//verificarne la firma.
				//Essendo i certificati self-signed, non ha molto senso: in uno scenario reale, i certificati
				//sarebbero firmati da una CA, e quindi dovrei verificarne la firma digitale usando il certificato
				//di chiave pubblica della CA.
			
			}else{
				
				//Se il certificato è già presente nel trust-store, verifico la sua validità temporale
				//(Ed eventualmente anche la sua firma digitale, ma vale il discorso fatto sopra)
				X509Certificate cert = (X509Certificate) this.trustStore.getCertificate(aliasName);
				Date actualDate = new Date();
				
				if(actualDate.after(cert.getNotAfter())) {
					//Certificato scaduto, lo devo eliminare dal truststore e riscaricarlo
					//Assumendo che il certificato che scarico sia aggiornato
					//Anche qui andrebbe fatto un controllo sulla firma del certificato (vedi discorso sopra)
					this.trustStore.deleteEntry(aliasName);
					File f1 = sh.getCertificate(nome, cognome, this.certificatesPath);
					addCertificate(f1,aliasName);
					f1.delete();
				}
			}
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	public void addCertificateFromCert(Certificate cert, String subjectAlias) {
		
		try {
			String [] p = extractParameters();
			FileInputStream fis1 = new FileInputStream(this.keystorePath);
			this.trustStore.load(fis1, p[1].toCharArray());
			
			
			
			this.trustStore.setCertificateEntry(subjectAlias, cert);
			
			FileOutputStream fos = new FileOutputStream(this.truststorePath);
			
			this.trustStore.store(fos, p[1].toCharArray());
			fos.close();
			
			
			System.out.println("[Add Certificate] Certificato aggiunto per l'alias:"+subjectAlias);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
	
	
	
	
	
	

}
