package it.testpackage;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;



import it.sm.messages.SecureKey;

public class MainTestR1_old {
	
	public static void main(String[] args) {
		Socket client;
		try {
			ServerSocket server = new ServerSocket(500);
			
			System.out.println("Ricevente avviato, mi metto in attesa.");
			
			client = server.accept();
			
			
			
			ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
			
			
				
				SecureKey s = (SecureKey) ois.readObject();
				System.out.println("Ricevuto messaggio da parte del client mittente.");

				//prendo la chiave privata dal mio keystore
				
				FileInputStream fis_rec = new FileInputStream("certificates\\ClientReceiver\\keystore.jks");

			    KeyStore keystore_rec = KeyStore.getInstance(KeyStore.getDefaultType());
			    keystore_rec.load(fis_rec, "password".toCharArray());

			    String alias_rec = "clientreceiver";

			    Key rec_key = keystore_rec.getKey(alias_rec, "password".toCharArray());
			    
			    PrivateKey pk_rec = (PrivateKey)rec_key;
			    
			    //De-critto il messaggio ricevuto
			    
			    Cipher msgDecrypt = Cipher.getInstance("RSA");
			    msgDecrypt.init(Cipher.DECRYPT_MODE, pk_rec);
			    byte [] decryptedMessage = msgDecrypt.doFinal(s.getEncryptedKey());
			    
			    String message = new String(decryptedMessage,"UTF-8");
			    
			    System.out.println("Messaggio de-crittato:"+message);
				
				//Verifico la firma del mittente
				
				CertificateFactory factSender = CertificateFactory.getInstance("X.509");
				FileInputStream fisSender = new FileInputStream ("certificates\\ClientSender\\ClientSendCertificate.cer");
			    X509Certificate cerSender = (X509Certificate) factSender.generateCertificate(fisSender);
			    
			    PublicKey pk = cerSender.getPublicKey();
			    
			    //Sblocco la firma usando la chiave pubblica del mittente
			    
			   //Verifico la firma del messaggio rispetto a quella che calcolo io, usando la classe Signature
			    
			    Signature sign = Signature.getInstance("MD5withRSA");
			    sign.initVerify(pk);
			    sign.update(message.getBytes());
			    boolean verifyResults = sign.verify(s.getKeySignature());
			    
			    System.out.println("Esito verifica firma:"+verifyResults);
			    
			    

			
				
				ois.close();
				server.close();
			
			
			
			
			
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
	}

}
