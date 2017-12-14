package it.testpackage;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.apache.commons.lang3.StringUtils;

public class MainTest {
	
	public static void main(String[] args) {
		
		//KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		try {
		
		BufferedReader br = new BufferedReader(new FileReader("./secure_place_bob/bobdata.txt"));
		String line1 = br.readLine();
		String line2 = br.readLine();
		
		String [] p1 = line1.split("=");
		String alias = p1[1];
		
		String [] p2 = line2.split("=");
		String password = p2[1];
		
		System.out.println(alias);
		System.out.println(password);
		
		br.close();
		
		
		
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

		char[] bytePassword = password.toCharArray();
		ks.load(null, bytePassword);

		// Store away the keystore.
		FileOutputStream fos = new FileOutputStream("./secure_place_bob/bob_truststore/bob_truststore.keystore");
		ks.store(fos, bytePassword);
		
		
		/*File cer = new File("./secure_place_bob/TomcatServerCertificate.cer");
		CertificateFactory fact = CertificateFactory.getInstance("X.509");
	    FileInputStream fis = new FileInputStream (cer);
	    X509Certificate cert = (X509Certificate) fact.generateCertificate(fis);
		
		ks.setCertificateEntry("tomcat", cert);
		
		
		System.out.println("Certificato?"+ks.containsAlias("tomcat"));
		ks.store(fos, bytePassword);
		fos.close();*/
		
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
		/*String cm = System.getenv("CLIENT_SM");
		System.setProperty("javax.net.ssl.trustStore",cm+"\\ciao.keystore");
		System.setProperty("javax.net.ssl.trustStorePassword", "pass");
		System.setProperty("javax.net.ssl.trustAnchors",cm+"\\ciao.keystore");
		
		System.out.println(System.getProperty("javax.net.ssl.trustStore"));
		System.out.println(System.getProperty("javax.net.ssl.trustAnchors"));*/
		
		
	}

}
