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
	
	private volatile static CertificateHelper instance;
	
	
	public static CertificateHelper getInstance() {
		if (instance == null) {
			
			synchronized(CertificateHelper.class) {
				
				if(instance == null) {
					instance = new CertificateHelper();
				}
			
			}

		}

	return instance;

	}
	
	private CertificateHelper() {}
	
	public void init(String userName) {
		this.currentUser = userName;
		this.certificatesPath = "."+File.separator+"secure_place_"+this.currentUser.toLowerCase()+File.separator+"certificates"+File.separator+"";
		this.truststorePath = "."+File.separator+"secure_place_"+this.currentUser.toLowerCase()+File.separator+this.currentUser.toLowerCase()+"_truststore"+File.separator+this.currentUser.toLowerCase()+"_truststore.keystore";
		this.keystorePath = "."+File.separator+"secure_place_"+this.currentUser.toLowerCase()+File.separator+this.currentUser.toLowerCase()+"_keystore.jks";
		this.dataFilePath = "."+File.separator+"secure_place_"+this.currentUser.toLowerCase()+File.separator+this.currentUser.toLowerCase()+"data.txt";
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
			    new ReloadableX509TrustManager(this.truststorePath, this.certificatesPath) 
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
	
	

	
	
	
	public void addCertificateFromCert(Certificate cert, String subjectAlias) {
		
		try {
			String [] p = extractParameters();
			FileInputStream fis1 = new FileInputStream(this.keystorePath);
			this.trustStore.load(fis1,p[1].toCharArray());
			
			this.trustStore.setCertificateEntry(subjectAlias, cert);
			
			FileOutputStream fos = new FileOutputStream(this.truststorePath);
			
			this.trustStore.store(fos,p[1].toCharArray());
			fos.close();
			
			
			System.out.println("[Add Certificate] Certificato aggiunto per l'alias:"+subjectAlias);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	

	public void removeCertificate(String alias) {
		try {
			
			String [] p = extractParameters();
			FileInputStream fis1 = new FileInputStream(this.keystorePath);
			this.trustStore.load(fis1,p[1].toCharArray());
			
			this.trustStore.deleteEntry(alias);
			
			FileOutputStream fos = new FileOutputStream(this.truststorePath);
			
			this.trustStore.store(fos,p[1].toCharArray());
			fos.close();
			
			System.out.println("[Add Certificate] Certificato rimosso per l'alias:"+alias);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public String getCertificatesPath() {
		return certificatesPath;
	}

	public void setCertificatesPath(String certificatesPath) {
		this.certificatesPath = certificatesPath;
	}

	public String getTruststorePath() {
		return truststorePath;
	}

	public void setTruststorePath(String truststorePath) {
		this.truststorePath = truststorePath;
	}

	public String getKeystorePath() {
		return keystorePath;
	}

	public void setKeystorePath(String keystorePath) {
		this.keystorePath = keystorePath;
	}

	public String getDataFilePath() {
		return dataFilePath;
	}

	public void setDataFilePath(String dataFilePath) {
		this.dataFilePath = dataFilePath;
	}
	
	
	
	
	
	

}
