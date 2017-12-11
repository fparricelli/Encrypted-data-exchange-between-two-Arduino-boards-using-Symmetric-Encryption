package it.test.sender;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.crypto.Cipher;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.util.encoders.Base64;

import it.sm.keystore.aeskeystore.MyAESKeystore;
import it.sm.keystore.rsakeystore.MyRSAKeystore;
import it.sm.messages.EncryptedMessage;
import it.sm.messages.SecureKey;


/* Classe che si occupa di gestire il protocollo di comunicazione lato sender
 * e di verificare tutte le opportune dipendenze.
 * Useremo un SenderHelper per inviare uno (o pi�) messaggi al
 * ricevente; questa classe si occuper� anche di gestire gli aspetti legati
 * al download dei certificati e alla cifratura della chiave e del messaggio.
 * 
 * Lato Sender supponiamo quindi di avere due keystore:
 * 		
 * 		> RSA SOFTWARE KEYSTORE: contiene la chiave privata RSA.
 * 	 	La corrispondente chiave pubblica RSA sar� contenuta nel certificato del sender.
 * 
 * 		> AES HARDWARE KEYSTORE: contiene la chiave segreta (simmetrica) AES
 * 		che useremo per la cifratura dei messaggi.
 * 
 * Quindi lo scambio della chiave AES verr� gestito tramite crittografia asimmetrica (RSA),
 * mentre l'invio del messaggio sar� gestito mediante crittografia simmetrica (AES).
 * 
 * 
 * La dinamica del protocollo � la seguente: lato sender innanzitutto eseguiremo la procedura
 * di Handshake: durante quest'ultima ci occuperemo di estrarre la chiave AES
 * dal nostro keystore hardware (arduino, ecc), calcolarne la firma usando la nostra chiave
 * privata (contenuta in un software keystore, di tipo RSA), e cifrarla usando
 * la chiave pubblica del ricevente (ottenuta scaricando il suo certificato da un server
 * ad-hoc).
 * Una volta ottenuta firma e chiave AES cifrata, manderemo il tutto al ricevente in modo da completare
 * lo scambio della chiave AES in maniera sicura, dopodich� attenderemo un ACK da quest'ultimo.
 * All'ottenimento del messaggio di ACK, possiamo procedere con l'invio del messaggio vero e proprio:
 * tale messaggio verr� prima cifrato usando la chiave AES (precedentemente scambiata e concordata), 
 * e poi inviato al ricevente.
 * Una volta inviato il messaggio, restiamo in attesa di un ACK dal ricevente, dopo il quale la comunicazione
 * � completata.
 */

public class SenderHelper{
	
	
	//Contiene la chiave AES cifrata (con la chiave pubblica del ricevente) e la
	//firma della chiave AES (calcolata con la nostra chiave privata)
	//Si rimanda alla classe SecureKey per dettagli.
	private SecureKey secKey;
	
	//Oggetto che rappresenta il Certificato del ricevente, contiene la sua chiave pubblica
	private X509Certificate receiverCert;
	
	
	//Path di default sul quale viene scaricato il certificato del ricevente, se necessario
	private final String defaultDownloadPath = "./certificates/ClientSender/receiverCertificate/ClientReceiverCertificate";
	
	//URL al quale � possibile scaricare il certificato del ricevente, se necessario
	private final String receiverCertDownloadURL = "http://localhost:8080/CertificateServer/getCertificate?id=";
	
	//Keystore Software che mantiene la NOSTRA chiave privata (RSA)
	private MyRSAKeystore RSAKeystore;
	
	//Keystore HARDWARE, usato per comunicare col dispositivo che mantiene la chiave segreta AES
	private MyAESKeystore AESKeystore;
	
	//Socket utilizzata per la comunicazione con il ricevente
	private Socket commSocket;
	
	//Stringa che indica l'identit� del mittente
	private String ownIdentity;
	
	
	
	public SenderHelper(String ownIdentity, MyRSAKeystore rsa, MyAESKeystore aes) {
		this.ownIdentity = ownIdentity;
		this.RSAKeystore = rsa;
		this.AESKeystore = aes;
		this.secKey = new SecureKey();
	}
	
	
	/* Procedura che si occupa di inizializzare la FIRMA della chiave AES.
	 * Tale firma verr� ottenuta usando la chiave privata contenuta nel keystore software di tipo RSA,
	 * mediante l'algoritmo di hashing md5.
	 * La firma cos� ottenuta viene utilizzata per settare la variabile membro SecureKey.
	 */ 
	private void initSign() throws Exception{
		//Inizializzo l'oggetto secKey con la firma della chiave AES,
		//ottenuta usando il mio software keystore RSA e l'algoritmo di hashing MD5
		
		/* Cambiata API */
		//secKey.setKeySignature(RSAKeystore.sign(AESKeystore.getSecretKey().getEncoded(), "MD5"));
	}
	
	/* Procedura che si occupa di inizializzare il certificato del ricevente.
	 * Prende in ingresso una stringa con il nome del ricevente con cui vogliamo comunicare.
	 * Se il certificato � gi� presente sul filesystem (al path certificatePath, variabile membro) e non � scaduto,
	 * allora viene prelevato e utilizzato per inizializzare la variabile membro corrispondente,
	 * Se ci� non accade, allora il certificato deve essere scaricato chiamando la procedura downloadCertificate(name).
	 */
	private void initCertificate(String receiverName) throws Exception{
		
		//Dal nome del ricevente fornito in input, ne ricavo l'ID
		int id = getIDByName(receiverName);
		
		//Ottengo il downloadPath concatenando l'ID appena ottenuto
		String downloadPath = defaultDownloadPath+id+".cer";
		
		File f = new File(downloadPath);
		
		//Se il certificato � gi� presente sul File System, lo utilizzo cos� com'�...
		if(f.exists()) {
			
			System.out.println("[Mittente - Handshake] Certificato ricevente presente sul file system!");
			
			CertificateFactory fact = CertificateFactory.getInstance("X.509");
		    FileInputStream fis = new FileInputStream (f);
		    X509Certificate cert = (X509Certificate) fact.generateCertificate(fis);
		    
		    //Verifico la validit� del certificato; se non � valido, lo riscarico
		    Date actualDate = new Date();
		    
		    if(actualDate.after(cert.getNotAfter())) {
		    	System.out.println("[Mittente - Handshake] Certificato scaduto!");
		    	downloadCertificate(receiverName);
		    }else {
		    	System.out.println("[Mittente - Handshake] Certificato valido!");
		    	this.receiverCert = cert;
		    	//Se il certificato esiste sul File System, allora ne stampo a video i campi
		    	printCertificateInfo();
		    }
			
		//...se il certificato non � presente, allora devo necessariamente scaricarlo
		}else {
			
			System.out.println("[Mittente - Handshake] Certificato non trovato, devo scaricarlo dal server.");
			downloadCertificate(receiverName);
		}
		
		
		
		
	}
	
	
	
	/* Procedura che si occupa di scaricare il certificato del ricevente dal server.
	 * Utilizza l'URL e il path definiti come variabili membro, e setta la variabile membro
	 * receiverCert una volta scaricato il certificato, aggiornando anche il certificatePath.
	 * Da notare che prende in ingresso il parametro String name, corrispondente al NOME del ricevente:
	 * chiamando la funzione getIDByName, si ricaver� l'ID del ricevente a partire dal suo nome che, 
	 * concatenato al defaultURL, ci dar� l'URL al quale scaricare il certificato.
	 */
	private void downloadCertificate(String receiverName) throws Exception {
		
			//Dal nome del ricevente fornito in input, ne ricavo l'ID
			int id = getIDByName(receiverName);
			
			//Calcolo URL a cui scaricare il certificato
			String downloadURL = receiverCertDownloadURL+id;
			
			//Calcolo path dove salvare il certificato (sul File System)
			String downloadPath = defaultDownloadPath+id+".cer";
		
			System.out.println("[Mittente - Handshake] Procedo al download del certificato per il ricevente: "+receiverName);
			System.out.println("[Mittente - Handshake] URL:"+downloadURL);
			System.out.println("[Mittente - Handshake] Path di salvataggio:"+downloadPath+"\n");
		
			//Scarico e copio il certificato al path specificato
			URL url = new URL(downloadURL);
			File file = new File(downloadPath);
			FileUtils.copyURLToFile(url, file);
		
			FileInputStream fis = new FileInputStream(file);
			CertificateFactory fact = CertificateFactory.getInstance("X.509");
		
			//Inizializzo variabile membro e stampo informazioni del certificato appena scaricato
			this.receiverCert = (X509Certificate) fact.generateCertificate(fis);
			printCertificateInfo();
		
	
	}
	
	
	/* Procedura che si occupa di effettuare la CIFRATURA della chiave AES.
	 * La cifratura viene effettuata utilizzando la chiave pubblica del ricevente,
	 * ricavata dal certificato che � stato precedentemente scaricato.
	 * Una volta ottenuta la chiave cifrata, questa viene usata per settare la variabile membro SecureKey.
	 */
	private void initEncryptedKey() throws Exception{
		
		//Prendo la chiave pubblica del ricevente dal suo certificato
		PublicKey pKey = this.receiverCert.getPublicKey();
		
		Cipher c = Cipher.getInstance("RSA");
		c.init(Cipher.ENCRYPT_MODE, pKey);
		
		/* Cambiata API */
		
		//byte[] s = AESKeystore.getSecretKey().getEncoded();
		
		//Cifro la chiave AES con la chiave pubblica e inizializzo la variabile membro secKey
		//System.out.println("[Mittente - Handshake] Chiave AES scelta per la comunicazione: "+new String(Base64.encode(s)));
		//secKey.setEncryptedKey(c.doFinal(s));
	}
	
	
	
	
	/* Procedura che si occupa di dare inizio all'HANDSHAKE.
	 * In ingresso accetta il NOME del ricevente con cui vogliamo comunicare: tramite la procedura getCoordinatesByName(name)
	 * si effettuer� una lookup che restituir� hostname e port, che verranno usati per inizializzare la Socket
	 * di comunicazione con il ricevente.
	 * Chiama le procedure per inizializzare certificato, firma e cifratura.
	 * All'invocazione, l'oggetto SecureKey:secKey sar� inizializzato e possieder� al proprio interno
	 * la chiave AES cifrata e la sua firma, che verranno inviate al ricevente.
	 * Dopodich�, si attende l'ACK dal ricevente per chiudere l'handshake.
	 * L'handshake deve essere effettuato soltanto quando si inizia la comunicazione col ricevente per
	 * la prima volta; tutte le comunicazioni successive avvengono invocando il solo metodo sendMessage(String).
	 */
	public void startHandshake(String receiverName) throws Exception{
		
		//Inizializzo certificato del destinatario fornito in input
		initCertificate(receiverName);
		
		//Inizializzo la firma della chiave AES nella variabile membro secKey
		initSign();
		
		//Inizializzo la chiave AES cifrata nella variabile membro secKey
		initEncryptedKey();
		
		//A tal punto la variabile membro secKey contiene il payload da mandare al destinatario.
		
		//Ricavo hostname e port per il destinatario
		String host = getCoordinatesByName(receiverName)[0];
		int port = Integer.valueOf(getCoordinatesByName(receiverName)[1]);
		
		//Apro la socket di comunicazione verso il destinatario
		this.commSocket = new Socket(host,port);
		
		//Definisco streams necessari alla comunicazione
		OutputStream os = this.commSocket.getOutputStream();
		InputStream is = this.commSocket.getInputStream();
		
		ObjectOutputStream oos = new ObjectOutputStream(os);
		
		//Inizializzo l'IDENTITA' del mittente (nella variabile membro secKey)
		secKey.setFrom(this.ownIdentity);
		
		//Mando l'oggetto secKey al destinatario usando gli stream precedentemente aperti
		oos.writeObject(secKey);
		oos.flush();
		
		System.out.println("[Mittente - Handshake] SecureKey object inviato.");
		System.out.println("[Mittente - Handshake] Attendo ACK...");
		
		
		DataInputStream dis = new DataInputStream(is);
		
		//Attendo l'ACK da parte del destinatario sullo stream di input.
		String ack = dis.readUTF();
		
		//Se il messaggio ricevuto non contiene un ACK..
		if(!ack.equals("ACK")) {
			//Chiudo la comunicazione attualmente stabilita e lancio un'eccezione
			closeActiveCommunication();
			throw new Exception("[Mittente - Handshake] Errore, messaggio di ACK non ricevuto.\n");
		//Altrimenti..
		}else {
			System.out.println("[Mittente - Handshake] Ack ricevuto.");
		}
		
		//...procedo e concludo l'handshake
		System.out.println("[Mittente - Handshake] Handshake terminato.");
		
		
	}
	
	
	/* Procedura che si occupa, a valle dell'handshake, di inviare un messaggio al ricevente
	 * cifrandolo con la chiave AES precedentemente concordata.
	 * Una volta inviato il messaggio cifrato, si attende l'ACK da part del ricevente per considerare
	 * concluso l'invio del messaggio.
	 */
	public void sendMessage(String message) throws Exception{
		
		//Se la socket di comunicazione non � inizializzata, non posso invare messaggi
		if(commSocket == null) {
			throw new Exception("[Mittente] Socket non inizializzata, effettua prima l'handshake!");
		}
		
		//Inizializzo gli stream necessari alla comunicazione
		OutputStream os = this.commSocket.getOutputStream();
		InputStream is = this.commSocket.getInputStream();
		
		System.out.println("[Mittente] Messaggio originale: "+message);
		
		//Cifro il messaggio da inviare usando la chiave AES precedentemente concordata
		/* Cambiata API */
		//String encryptedMessage = AESKeystore.encrypt(message);
		
		//System.out.println("[Mittente] Messaggio cifrato (AES): "+encryptedMessage);
		
		
		//Invio del messaggio cifrato con la chiave AES precedentemente scambiata
		//EncryptedMessage m = new EncryptedMessage(encryptedMessage);
		
		//Commentare questa dopo
		EncryptedMessage m =null;
		
		ObjectOutputStream oos = new ObjectOutputStream(os);
		
		//Invio il messaggio al destinatario
		oos.writeObject(m);
		oos.flush();
		
		System.out.println("[Mittente] EncryptedMessage object inviato.");
		System.out.println("[Mittente] Attendo ultimo Ack...");
		
		DataInputStream dis = new DataInputStream(is);
		
		//Attendo messaggio di ACK dal destinatario per il messaggio inviato
		String lastAck = dis.readUTF();
		
		//Se il messaggio ricevuto non contiene un ACK...
		if(!lastAck.equals("ACK")) {
			//...chiudo la comunicazione e lancio un'eccezione
			closeActiveCommunication();
			throw new Exception("[Mittente] Errore : Messaggio di Ack non ricevuto.");
		}else {
			//...altrimenti la comunicazione � andata a buon fine!
			System.out.println("[Mittente] Ricevuto ultimo Ack.");
			
		}
		
		
		
}
	
	
	
	/* Procedura che si occupa di chiudere la socket di comunicazione con il ricevente.
	 * Una volta chiusa la socket, per poter ricominicare la comunicazione � necessario
	 * effettuare nuovamente l'handshake.
	 */
	public void closeActiveCommunication() throws Exception{
		this.commSocket.close();
	}
	
	
	
	//Stampa le info del certificato ricevente.
	private void printCertificateInfo() {
		
		System.out.println("[Mittente - Handshake] Informazioni Certificato ricevente\n");
		System.out.println("\t*****Certificato a nome di: " + this.receiverCert.getSubjectDN()+"*****");
	    System.out.println("\t*****Certificato fornito da: " + this.receiverCert.getIssuerDN()+"*****");
	    System.out.println("\t*****Valido da " + this.receiverCert.getNotBefore() + " a "
	        + this.receiverCert.getNotAfter()+"*****");
	    System.out.println("\t*****Serial Number: " + this.receiverCert.getSerialNumber()+"*****");
	    System.out.println("\t*****Tipo Certificato: " + this.receiverCert.getType()+"*****");
	    System.out.println("\t*****Versione: " + this.receiverCert.getVersion()+"*****");
	    System.out.println("\n");
	    
	}
	
	
	/* Procedura che si occupa, dato il nome del ricevente, di ricavare
	 * hostname e port al quale questo pu� essere contattato (ad esempio, al quale possiamo
	 * contattare la sua mailbox).
	 * Nel caso reale si avr� una procedura pi� complessa; nel nostro caso restituiamo direttamente i dati
	 * che ci servono.
	 */ 
	private String [] getCoordinatesByName(String name) {
		
		return new String[] {"localhost", "3456"};
	}
	
	/* Procedura che si occupa, dato il nome del ricevente, di ricavare l'ID che deve 
	 * essere concatenato al defaultURL (variabile membro) per ottenere l'URL completo
	 * al quale scaricare il certificato del ricevente.
	 * Nel caso reale, si avrebbe una procedura pi� complessa; nel nostro caso pi� semplice restituiamo direttamente
	 * l'ID (noto) del ricevente.
	 */ 
	private int getIDByName(String name) {
		return 2;
	}
	
	
	
}
