package it.test.sender;


import it.sm.keystore.aeskeystore.AESHardwareKeystore;
import it.sm.keystore.aeskeystore.MyAESKeystore;
import it.sm.keystore.rsakeystore.MyRSAKeystore;
import it.sm.keystore.rsakeystore.RSASoftwareKeystore;


public class MainSender {
	
	public static void main(String[] args) {
		
		try {
		//Inizializzo il mio hardware keystore AES (arduino)
		MyAESKeystore mAES = new AESHardwareKeystore();
		
		//Definisco il path in cui conservo il mio RSA software keystore
		String rsaPath = "certificates/ClientSender/keystore.jks";
		
		//Inizializzo il mio software keystore RSA
		MyRSAKeystore mRSA = new RSASoftwareKeystore(rsaPath, "clientsend", "password");
		
		//Inizializzo l'oggetto SenderHelper che mi consentirà di contattare il destinatario
		SenderHelper sh = new SenderHelper("Aldo Strofaldi", mRSA, mAES);
		
		//Avvio l'handshake con il destinatario
		sh.startHandshake("Lorenzo Insigne");
		
		//Invio il messaggio
		sh.sendMessage("Hello World!");
		
		//Chiudo la comunicazione
		sh.closeActiveCommunication();		
		
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}
