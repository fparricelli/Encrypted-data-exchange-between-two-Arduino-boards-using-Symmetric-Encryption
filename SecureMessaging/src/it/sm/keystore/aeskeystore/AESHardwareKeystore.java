package it.sm.keystore.aeskeystore;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.Semaphore;

import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import it.sm.exception.Base64EncodedError;
import it.sm.exception.OutOfBoundEncrypt;
import it.sm.messages.EncryptedMessage;


/* Classe che si occupa di astrarre un keystore HARDWARE di tipo AES,
 * fornendo metodi per effettuare cifratura, de-cifratura, estrazione e 
 * iniezione della chiave segreta.
 * 
 * L'idea � fare in modo che la classe non mantenga alcuna informazione interna sulla chiave segreta:
 * per questo la variabile SecretKey sKey deve essere rimossa.
 * L'utilizzo della variabile sKey presuppone infatti che il costruttore agisca in questo modo:
 * 		
 * 		> se loadFromHardware == true, allora vuol dire che la chiave AES � gi� presente sul dispositivo hardware
 *   	e quindi chiamando initSecretKey() andrei a prelevarla e assegnarla alla variabile sKey; 
 * 		Tuttavia questo presupporrebbe salvare la chiave segreta in una variabile.
 * 
 * 		> se loadFromHardware == false, allora vuol dire che la chiave AES non � presente sul dispositivo e deve
 * 		quindi essere prima iniettata al suo interno tramite il metodo injectSecretKey: questo si occuper� poi di salvare
 * 		la chiave segreta nella variabile sKey (con le stesso problematiche di cui sopra).
 * 
 * L'idea � quindi quella di fare in modo che i metodi non usino volta per volta la variabile membro sKey ma che
 * invece comunichino sempre e direttamente col dispositivo: ad es. il metodo getSecretKey prender� la chiave dal dispositivo
 * e la restituir� ma SENZA salvarla nella variabile membro, allo stesso modo il metodo injectSecretKey inietter� la chiave 
 * nel dispositivo ma SENZA salvarla nella variabile membro sKey.
 * Chiaramente il metodo initSecretKey non esister�.
 * Tutto quello che va eliminato � stato messo sotto i commenti 'TEST', ed � stato lasciato per testing.
 */
public class AESHardwareKeystore implements MyAESKeystore{
	
	private static final int MAX_SIZE=32;
	
	private ArduinoSerial uno;
	
	@SuppressWarnings("unused")
	private int client_type;
	
	private static Semaphore mutex;
	
	static private AESHardwareKeystore instance;
	
	static public AESHardwareKeystore getInstance(int c_type) {
		if (instance == null)
				return new AESHardwareKeystore(c_type);
		
		return instance;
	}

	private AESHardwareKeystore(int client_type) {
		this.client_type = client_type;
		uno = new ArduinoSerial(client_type);
		mutex = new Semaphore(1);
	};
	
	@Override
	public String requireTokenToShare(Integer c_type) {

		/* Send Command -- Command ref da ridefinire */
		initialize();

		uno.writeData("1");
		System.out.println("[Arduino] - Requested Token...");

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		uno.writeData(c_type.toString());

		while(!uno.available());
		String token = uno.readData();	
		closeConnection();
		return token;
		
	}
	
	@Override
	public boolean setTokenShared(String token) {
		/* Send Command */
		
		initialize();
		uno.writeData("2");
		System.out.println("[Arduino] - Setting Token...");

		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Già in base64
		uno.writeData(token);
		
		while(!uno.available());
		String ris = new String(Base64.getDecoder().decode(uno.readData()));	
		closeConnection();

		if(ris.contains("OK")) 
			return true;
		return false;
	}
	
	@Override
	public EncryptedMessage encrypt(String message) throws OutOfBoundEncrypt  {

		if(message.length() > MAX_SIZE-1) throw new OutOfBoundEncrypt();
		/* Send Command */
		initialize();

		uno.writeData("3");
		System.out.println("[Arduino] - Requested Encryption...");

		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		/* Invio Messaggio */
		
		uno.writeData(message);
		
		while(!uno.available());
		String msg_key = uno.readData();	
		
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		while(!uno.available());
		String msg = uno.readData();	
		closeConnection();
		
		System.out.println("KEY : "+msg_key+"\nMSG: "+msg);
		return new EncryptedMessage(msg_key, msg);
	}

	
	@Override
	public String decrypt(String message, String msg_key) throws Base64EncodedError {

		if(!msg_key.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$")||!message.matches("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$"))
		throw new Base64EncodedError();
		/* Send Command */
		initialize();
		System.out.println("[Arduino] - Requested Decryption...");
		uno.writeData("4");
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		uno.writeData(msg_key);
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		/* Invio Messaggio */
		
		uno.writeData(message);
		
		while(!uno.available());
		String msg = uno.readData();
		closeConnection();
		return new String(Base64.getDecoder().decode(msg));
	}

	
	public void closeConnection() {
		uno.close();
		mutex.release();
	}
	
	public void initialize() {
		try {
			mutex.acquire();
			uno.initialize();
		} catch (PortInUseException | UnsupportedCommOperationException | IOException | InterruptedException e) {
			closeConnection();
			e.printStackTrace();
		}
		/* Waiting for Ready - DA COMMENTARE SE SI APPLICA CAPACITORE */
		//while(!uno.available());
		//System.out.println("[DEBUG] "+uno.readData());
	}


}
