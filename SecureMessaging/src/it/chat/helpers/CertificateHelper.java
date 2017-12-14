package it.chat.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

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
	}
	
	private void initTrustStore() {
	try {
		this.trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		String [] p = extractParameters();
		if(p!= null) {
			
			char[] bytePassword = p[1].toCharArray();
			FileInputStream fis = new FileInputStream(this.truststorePath);
			this.trustStore.load(fis, bytePassword);
			
			/*FileOutputStream fos = new FileOutputStream(this.truststorePath);
			this.trustStore.store(fos, bytePassword);
			fos.close();*/
			
			
			String cm = System.getProperty("user.dir");
			String path = cm+this.truststorePath.substring(1);
			System.setProperty("javax.net.ssl.trustStore",path);
			System.setProperty("javax.net.ssl.trustStorePassword", p[1]);
			System.setProperty("javax.net.ssl.trustAnchors",path);
			
			System.out.println("Proprietà 1 :"+System.getProperty("javax.net.ssl.trustStore"));
			System.out.println("Proprietà 2 :"+System.getProperty("javax.net.ssl.trustAnchors"));

			
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
		
		System.out.println("Proprietà 3 :"+System.getProperty("javax.net.ssl.keyStore"));

	}
	
	
	private String [] extractParameters() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(this.dataFilePath));
			String line1 = br.readLine();
			String line2 = br.readLine();
			
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
	
	
	private void addCertificate(File cert, String subjectAlias) {
		
		try {
			CertificateFactory fact = CertificateFactory.getInstance("X.509");
		    FileInputStream fis = new FileInputStream (cert);
		    X509Certificate certs = (X509Certificate) fact.generateCertificate(fis);
			this.trustStore.setCertificateEntry(subjectAlias, certs);
			if(this.trustStore.containsAlias(subjectAlias)) {
				System.out.println("Certificato aggiunto correttamente!");
			}else {
				System.out.println("Errore nell'aggiunta certificato!");
			}
			FileOutputStream fos = new FileOutputStream(this.truststorePath);
			String [] p = extractParameters();
			this.trustStore.store(fos, p[1].toCharArray());
			fos.close();
			fis.close();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	public void getCertificate(String nome, String cognome) throws CertificateNotFoundException, ServerErrorException {
		
		try {
			ServerHelper sh = new ServerHelper();
			File f = sh.getCertificate(nome, cognome, this.certificatesPath);
			addCertificate(f,nome.toLowerCase()+"_cert");
		}catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	
	
	
	
	
	

}
