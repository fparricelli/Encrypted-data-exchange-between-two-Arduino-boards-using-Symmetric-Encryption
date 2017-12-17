package it.chat.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;

import it.sm.exception.CertificateNotFoundException;
import it.sm.exception.RedirectToLoginException;
import it.sm.exception.ServerErrorException;
import it.sm.messages.Messaggio;

public class SignHelper {
	
	private SignHelper() {};
	
	private static SignHelper instance;
	
	public static SignHelper getInstance() {
		if(instance == null)
				return new SignHelper();
		return instance;
	}
	
	public boolean verifySign(Messaggio msg) throws CertificateNotFoundException, ServerErrorException, RedirectToLoginException, CertificateException, FileNotFoundException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		CertificateHelper ch = CertificateHelper.getInstance();
		ServerHelper sh = new ServerHelper();
		
		File cert = sh.getCertificate(msg.getSender().substring(0, msg.getSender().indexOf(" ")).toLowerCase(), msg.getSender().substring(1 + msg.getSender().indexOf(" "), msg.getSender().length()).toLowerCase(), ch.getCertificatesPath());
		
		CertificateFactory fact = CertificateFactory.getInstance("X.509");
	    FileInputStream fis = new FileInputStream (cert);
	    X509Certificate cer = (X509Certificate) fact.generateCertificate(fis);
	    
	    Date now = new Date();
	    
	    if(now.compareTo(cer.getNotBefore())<0 || now.compareTo(cer.getNotAfter())>0)
	    		throw new CertificateNotYetValidException();
	    Signature sig = Signature.getInstance("MD5withRSA");
	    sig.initVerify(cer.getPublicKey());
	 
	    sig.update(msg.getMsg().getBytes());
	    
	   if(sig.verify(msg.getSignature())) {
		   System.out.println("OK Firma");
		   return true;
	   }
	   System.out.println("NO Firma");
	   return false;


	}
	
	public byte[] signToken(String token_to_send) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InvalidKeyException, UnrecoverableKeyException, SignatureException {
		
		CertificateHelper ch = CertificateHelper.getInstance();
		
		String ks_path = ch.getKeystorePath();
		
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		InputStream in = new FileInputStream(ks_path);
	
			ks.load(in, ch.extractParameters()[1].toCharArray());
		

			Signature sign = Signature.getInstance("MD5withRSA");
			sign.initSign((PrivateKey)ks.getKey(ch.extractParameters()[0], ch.extractParameters()[1].toCharArray()));
			sign.update(token_to_send.getBytes());
			
		return sign.sign();
		
	}

}
