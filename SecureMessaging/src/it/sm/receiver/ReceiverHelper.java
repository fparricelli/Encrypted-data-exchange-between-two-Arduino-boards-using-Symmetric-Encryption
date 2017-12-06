package it.sm.receiver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.util.encoders.Base64;

import it.sm.keystore.aeskeystore.MyAESKeystore;
import it.sm.keystore.rsakeystore.MyRSAKeystore;
import it.sm.messages.EncryptedMessage;
import it.sm.messages.SecureKey;

/* Classe che si occupa di gestire il protocollo di comunicazione lato ricevente 
 * e di verificare tutte le relative dipendenze.
 * Useremo un ReceiverHelper per ricevere uno (o piï¿½) messaggi da un sender; la classe
 * si occuperï¿½ di gestire anche tutte le problematiche legate al download dei certificati
 * e alla cifratura.
 * 
 * Lato Ricevente supponiamo quindi di avere due keystore:
 * 		
 * 		> RSA SOFTWARE KEYSTORE: contiene la chiave privata RSA.
 * 	 	La corrispondente chiave pubblica RSA sarï¿½ contenuta nel certificato del ricevente.
 * 
 * 		> AES HARDWARE KEYSTORE: conterrï¿½ la chiave segreta (AES) che useremo per la cifratura dei
 * 		messaggi.
 * 		Da notare che il ricevente deve prima ricevere la chiave AES dal mittente (in fase di handshake),
 * 		dopodichï¿½ tale chiave verrï¿½ iniettata all'interno dell'hardware keystore e usata per la cifratura
 * 		di tutti i messaggi.
 * 
 * Quindi lo scambio della chiave AES verrï¿½ gestito tramite crittografia asimmetrica (RSA),
 * mentre la ricezione del messaggio sarï¿½ gestita mediante crittografia simmetrica (AES).
 * 
 * La dinamica ï¿½ la seguente: la prima cosa da fare ï¿½ avviare l'Handshake, che si occuperï¿½
 * innanzitutto di attendere l'arrivo delle informazioni da parte del mittente: quest'ultimo
 * invierï¿½ la chiave AES (cifrata con la chiave pubblica del ricevente) e la firma della chiave AES
 * (ottenuta usando la chiave privata del mittente), insieme all'identitï¿½ del mittente.
 * Con tali informazioni andiamo innanzitutto a recuperare il certificato del mittente (scaricandolo da un server
 * ad-hoc, a meno che non sia giï¿½ presente sul file-system).
 * Con le altre informazioni (chiave AES cifrata e firma), procediamo innanzitutto a decifrare la chiave AES
 * usando la chiave privata del ricevente (iniettandola subito dopo nel nostro hardware keystore); 
 * dopodichï¿½ sfrutteremo il certificato del mittente per ricavare la sua chiave pubblica e 
 * verificare la firma della chiave AES.
 * Al termine di queste attivitï¿½, invieremo un ACK al mittente, che indica il concludersi dell'handshake.
 * Dopodichï¿½ ci metteremo in attesa di ricevere il messaggio cifrato con la chiave AES da parte del mittente; una volta
 * ricevuto useremo la chiave AES precedentemente concordata per decifrarlo.
 * 
 */

public class ReceiverHelper{
	
	
	//Software Keystore di tipo RSA, contiene la chiave privata del ricevente
	private MyRSAKeystore RSAKeystore;
	
	//Hardware Keystore di tipo AES, conterrï¿½ la chiave AES usata per decifrare i messaggi
	private MyAESKeystore AESKeystore;
	
	//URL al quale ï¿½ possibile scaricare il certificato del mittente, se necessario
	private final String senderCertDownloadURL = "http://localhost:8080/CertificateServer/getCertificate?id=";
	
	//Path al quale verrï¿½ salvato il certificato del mittente, se necessario - Decomment for Windows
	//private final String defaultDownloadPath = "certificates//ClientReceiver//senderCertificate//ClientSenderCertificate";
	//For MacOS
	private final String defaultDownloadPath = "./certificates/ClientReceiver/senderCertificate/ClientSenderCertificate";

	
	//Certificato del mittente, contiene la sua chiave pubblica
	private X509Certificate senderCertificate;
	
	//ServerSocket utilizzata dal ricevente per accettare comunicazioni dall'esterno
	private ServerSocket commSocket;
	
	//Socket che viene associata al client con cui ï¿½ in corso la comunicazione corrente
	private Socket clientSocket;
	

	
	public ReceiverHelper(MyRSAKeystore rsa, MyAESKeystore aes, int listeningPort) throws Exception {
		this.RSAKeystore = rsa;
		this.AESKeystore = aes;
		this.commSocket = new ServerSocket(listeningPort);
	}
	
	
	/* Procedura che si occupa di inizializzare il certificato del mittente.
	 * Se il certificato ï¿½ giï¿½ presente sul filesystem (al path defaultDownloadURL+id) e non ï¿½ scaduto,
	 * allora viene prelevato e utilizzato per inizializzare la variabile membro corrispondente,
	 * altrimenti il certificato viene scaricato dal server usando la procedura downloadCertificate(senderName).
	 */
	private void initCertificate(String senderName) throws Exception{
		
		int id = getIDByName(senderName);
		String downloadPath = defaultDownloadPath+id+".cer";
		
		File f = new File(downloadPath);
		
		if(f.exists()) {
			
			System.out.println("[Ricevente - Handshake] Certificato mittente giï¿½ presente sul file system!");
			
			CertificateFactory fact = CertificateFactory.getInstance("X.509");
		    FileInputStream fis = new FileInputStream (f);
		    X509Certificate cert = (X509Certificate) fact.generateCertificate(fis);
		    
		    Date actualDate = new Date();
		    
		    if(actualDate.after(cert.getNotAfter())) {
		    	System.out.println("[Ricevente - Handshake] Certificato scaduto!");
		    	downloadCertificate(senderName);
		    
		    }else {
		    	System.out.println("[Ricevente - Handshake] Certificato valido!");
		    	this.senderCertificate = cert;
		    	printCertificateInfo();
		    }
			
			
		}else {
			
			System.out.println("[Ricevente - Handshake] Certificato non trovato, devo scaricarlo dal server.");
			downloadCertificate(senderName);
		}
		
	
	}
	
	/* Procedura che si occupa di scaricare il certificato del mittente dal server.
	 * Utilizza l'URL e il path definiti come variabili membro, e setta la variabile membro
	 * receiverCert una volta scaricato il certificato.
	 * Da notare che l'URL al quale scaricare il certificato viene ottenuto mediante concatenazione
	 * del defaultDownloadURL e dell'ID (ottenuto a partire dal senderName attraverso il metodo di lookup
	 * getIDByName).
	 */
	private void downloadCertificate(String senderName) throws Exception {
		
		int id = getIDByName(senderName);
		
		String downloadURL = senderCertDownloadURL+id;
		String downloadPath = defaultDownloadPath+id+".cer";
		
		System.out.println("[Ricevente - Handshake] Procedo al download del certificato per il mittente: "+senderName);
		System.out.println("[Ricevente - Handshake] URL: "+downloadURL);
		System.out.println("[Ricevente - Handshake] Path di salvataggio: "+downloadPath+"\n");
		
		URL url = new URL(downloadURL);
		File file = new File(downloadPath);
		FileUtils.copyURLToFile(url, file);
		
		FileInputStream fis = new FileInputStream(file);
		CertificateFactory fact = CertificateFactory.getInstance("X.509");
		this.senderCertificate = (X509Certificate) fact.generateCertificate(fis);
		printCertificateInfo();
	
	}
	
	
	
	/* Procedura che dï¿½ il via all'handshake lato ricevente.
	 * Si occupa di accettare eventuali richieste di connessione in entrata (assegnando la variabile membro
	 * clientSocket).
	 * In piï¿½, aspetta di ricevere dal mittente l'oggetto SecureKey contenente chiave AES cifrata
	 * e firma della chiave AES.
	 * Una volta ricevuto tale oggetto, si occupa innanzitutto di recuperare il certificato
	 * del mittente (chiamando il metodo initCertificate(name), il nome del mittente viene preso dall'oggetto
	 * SecureKey), poi si occupa di decifrare la chiave AES (cifrata con
	 * la chiave pubblica del ricevente) usando la chiave privata del ricevente, e poi di verificare
	 * la firma di tale chiave: se la firma non corrisponde, il mittente viene notificato con un messaggio di
	 * ERROR che pone fine alla comunicazione; se invece la verifica va a buon fine, il mittente viene notificato
	 * con un messaggio di ACK.
	 * Infine (sempre in caso di verifica corretta) la chiave AES ottenuta viene iniettata nel keystore hardware AES.
	 */
	public void startHandshake() throws Exception {
		
		
		
		System.out.println("[Ricevente - Handshake] Handshake avviato, attendo client..");
		//Mi metto in attesa di essere contattato..
		this.clientSocket = this.commSocket.accept();
		
		System.out.println("[Ricevente - Handshake] Client connesso.");
		
		//Una volta connesso ad un client, dichiaro gli stream necessari
		OutputStream os = this.clientSocket.getOutputStream();
		InputStream is = this.clientSocket.getInputStream();
		
		DataOutputStream dos = new DataOutputStream(os);
		ObjectInputStream ois = new ObjectInputStream(is);
		
		System.out.println("[Ricevente - Handshake] In attesa della SecureKey...");
		
		//Leggo l'oggetto SecureKey inviatomi dal client mittente, in cui sarà contenuta
		//l'identità del mittente (necessaria per ricavarne il certificato), la firma e la cifratura della chiave AES.
		SecureKey secKey = (SecureKey)ois.readObject();
		
		System.out.println("[Ricevente - Handshake] Ho ricevuto la SecureKey.");
		System.out.println("[Ricevente - Handshake] Procedo alla gestione dei certificati per il mittente: "+secKey.getFrom());
		
		//Inizializzo i certificati per il mittente del messaggio
		initCertificate(secKey.getFrom());
		
		System.out.println("[Ricevente - Handshake] Operazioni sui certificati completate.");
		
		//Utilizzo la mia chiave privata per decifrare la chiave AES ricevuta
		byte [] decryptedAESKey = RSAKeystore.decrypt(secKey.getEncryptedKey());
		
		//Verifico la firma della chiave AES 
		boolean result = verifySignature(decryptedAESKey, secKey.getKeySignature());
		
		//Se la verifica NON va a buon fine...
		if(!result) {
			//Avviso il mittente (che era in attesa di ACK) con un msg di ERROR
			dos.writeUTF("ERROR");
			//Chiudo la comunicazione e lancio un'eccezione
			closeActiveCommunication();
			throw new Exception("[Ricevente - Handshake] Verifica fallita!");
		//altrimenti...
		}else {
			System.out.println("[Ricevente - Handshake] La verifica ï¿½ andata a buon fine.");

			System.out.println("[Ricevente - Handshake] Chiave AES ottenuta dal mittente: "+new String(Base64.encode(decryptedAESKey)));
			System.out.println("[Ricevente - Handshake] Handshake terminato.");
			
			//Se la verifica è andata a buon fine, posso fidarmi della chiave AES che ho ricevuto
			//E iniettarla nel mio hardware keystore AES, in modo da usarla per le successive
			//comunicazioni con quel mittente
			SecretKey sKey = new SecretKeySpec(decryptedAESKey, "AES");
			AESKeystore.injectSecretKey(sKey);
			
			//Invio il messaggio di ACK al mittente che lo attendeva
			dos.writeUTF("ACK");
			System.out.println("[Ricevente - Handshake] ACK inviato.");
		}
		
		
	}
	
	/* Procedura che si occupa di verificare la FIRMA della chiave AES.
	 * La firma che viene inviata dal mittente ï¿½ stata ottenuta usando la chiave privata (del mittente),
	 * di conseguenza per la verifica viene utilizzata la chiave pubblica del mittente (disponibile
	 * dal suo certificato).
	 */
	private boolean verifySignature(byte [] decryptedInput, byte [] signature) throws Exception {
		
		//Per verificare la firma, devo decifrare la firma inviatami dal mittente 
		//(cifrata con la sua chiave privata) usando la sua chiave pubblica, che ricavo dal certificato
		Signature sig = Signature.getInstance("MD5withRSA");
		sig.initVerify(senderCertificate.getPublicKey());
		sig.update(decryptedInput);
		return sig.verify(signature);
		
	}
	
	
	/* Procedura che viene utilizzata per effettuare la ricezione di un messaggio, a fronte dell'esecuzione (corretta)
	 * dell'handshake.
	 * Si occupa di attendere l'arrivo del messaggio (cifrato con la chiave AES) dal mittente, dopodichï¿½
	 * una volta ricevuto viene inviato l'ACK al mittente e si procede a decifrare il messaggio.
	 */
	public void receiveMessage() throws Exception {
		
		//Se la socket di comunicazione non è attiva..
		if(clientSocket == null) {
			//Lancia una nuova eccezione
			throw new Exception("[Ricevente] Socket non inizializzata, effettuare Handshake!");
		}
		
		//Definisco gli stream necessari alla comunicazione
		OutputStream os = this.clientSocket.getOutputStream();
		InputStream is = this.clientSocket.getInputStream();
		
		System.out.println("[Ricevente] In attesa dell'EncryptedMessage..");
		
		ObjectInputStream ois = new ObjectInputStream(is);
		
		//Leggo il messaggio cifrato proveniente dall'input stream
		EncryptedMessage m = (EncryptedMessage) ois.readObject();
		
		System.out.println("[Ricevente] Ricevuto EncryptedMessage.");
		System.out.println("[Ricevente] Messaggio cifrato (AES): "+m.getEncryptedMessage());
		
		//Invio l'ACK al mittente che lo attendeva, usando l'output stream
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeUTF("ACK");
		
		System.out.println("[Ricevente] ACK finale inviato.");
		
		//Decifro il messaggior ricevuto usando la chiave AES precedentemente concordata
		String decryptedMessage = AESKeystore.decrypt(m.getEncryptedMessage());

		System.out.println("[Ricevente] Messaggio decifrato (AES): "+decryptedMessage);
		
	}
	
	
	/* Procedura utilizzata per chiudere la socket associata al client con cui ï¿½ attiva la 
	 * comunicazione corrente.
	 * Da notare che una volta che ï¿½ stato effettuato l'handshake, per ricevere messaggi da un determinato
	 * client mittente basta chiamare esclusivamente il metodo receiveMessage().
	 * L'invocazione del metodo sottostante causa la chiusura della socket associata al client mittente (corrente),
	 * di conseguenza per effettuare una nuova comunicazione sarebbe necessario effettuare nuovamente l'handshake.
	 */
	public void closeActiveCommunication() throws Exception{
		this.commSocket.close();
	}
	
	
	
	
	//Stampa le info del certificato.
	private void printCertificateInfo() {
		
		System.out.println("[Ricevente - Handshake] Informazioni Certificato mittente\n");
		System.out.println("\t*****Certificato a nome di: " + this.senderCertificate.getSubjectDN()+"*****");
	    System.out.println("\t*****Certificato fornito da: " + this.senderCertificate.getIssuerDN()+"*****");
	    System.out.println("\t*****Valido da " + this.senderCertificate.getNotBefore() + " a "
	        + this.senderCertificate.getNotAfter()+"*****");
	    System.out.println("\t*****Serial Number: " + this.senderCertificate.getSerialNumber()+"*****");
	    System.out.println("\t*****Tipo Certificato: " + this.senderCertificate.getType()+"*****");
	    System.out.println("\t*****Versione: " + this.senderCertificate.getVersion()+"*****");
	    System.out.println("\n");
	    
	}

	
	
	/* Procedura che si occupa, dato il nome del mittente, di ricavare l'ID che deve 
	 * essere concatenato al defaultURL (variabile membro) per ottenere l'URL completo
	 * al quale scaricare il certificato del mittente.
	 * Nel caso reale, si avrebbe una procedura piï¿½ complessa; nel nostro caso piï¿½ semplice restituiamo direttamente
	 * l'ID (noto) del ricevente.
	 */ 
	private int getIDByName(String name) {
		return 1;
	}

}
