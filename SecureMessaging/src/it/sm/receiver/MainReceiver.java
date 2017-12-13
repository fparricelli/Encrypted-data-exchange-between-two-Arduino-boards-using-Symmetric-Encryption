package it.sm.receiver;

import it.sm.keystore.aeskeystore.AESHardwareKeystore;
import it.sm.keystore.aeskeystore.MyAESKeystore;
import it.sm.keystore.rsakeystore.MyRSAKeystore;
import it.sm.keystore.rsakeystore.RSASoftwareKeystore;

public class MainReceiver {
	
	public static void main(String[] args) {
		
		try {
			
			//Inizializzo il mio keystore hardware AES
			MyAESKeystore mAES = new AESHardwareKeystore(2);
			
			//Definisco il path del mio RSA software keystore
			String rsaPath = "./certificates/ClientReceiver/keystore.jks";
			
			//Inizializzo il mio RSA software keystore
			MyRSAKeystore mRSA = new RSASoftwareKeystore(rsaPath, "clientreceiver", "password");
			
			//Inizializzo l'oggetto ReceiverHelper che mi permetter� di ricevere messaggi
			ReceiverHelper rh = new ReceiverHelper(mRSA, mAES, 3456);
			
			//Mi metto in attesa di essere contattato per l'handshake
			rh.startHandshake();
			
			//Mi metto in attesa di ricevere un messaggio, dopo l'handshake
			rh.receiveMessage();
			
			//Chiudo la comunicazione attiva
			rh.closeActiveCommunication();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
		
		
	}

}
