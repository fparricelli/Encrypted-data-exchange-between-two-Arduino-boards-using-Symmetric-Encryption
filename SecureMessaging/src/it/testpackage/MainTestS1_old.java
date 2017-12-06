package it.testpackage;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.security.Key;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.bouncycastle.util.encoders.Base64;

import it.sm.messages.SecureKey;

import java.util.Date;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DigestAlgorithmIdentifierFinder;
import javax.crypto.Cipher;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;

public class MainTestS1_old {

	public static void main(String[] args) {
		
		try {
			
		/* NOTE SULL'UTILIZZO
		 * 
		 * cartella ClientSender: contiene il certificato del Sender e il keystore (che contiene la sua chiave privata)
		 * cartella ClientReceiver : contiene il certificato del Receiver e il keystore (che contiene la sua chiave privata)
		 * Le due cartelle hanno senso solo per l'esempio; nel caso reale supponiamo che il client mittente
		 * abbia una cartella in cui mantiene il proprio certificato e una cartella in cui mantiene il certificato
		 * del receiver.
		 * Inoltre supporremo che il client mittente utilizzi un keystore hardware (l'arduino) mentre il ricevente
		 * utilizzi un keystore software (il file keystore.jks); nell'esempio entrambi i keystore sono di tipo software 
		 * e sono contenuti nelle rispettive cartelle, come accennato sopra.
		 * 
		 * Nell'esempio supponiamo inoltre che i certificati vengano scaricati direttamente dal server, se non sono già
		 * presenti nelle cartella ClientReceiver e ClientSender (e se non sono scaduti).
		 * Eventualmente è possibile prendere manualmente i certificati dal progetto Eclipse della servlet e 
		 * metterli nelle rispettive cartelle.
		 * 
		 */
		
		
		//****************************************************************
		//Questa porzione di codice serve soltanto per ricavare la chiave privata da usare negli esempi
		//Nella realtà non la useremo mai visto che la chiave privata non uscirà mai dall'Arduino (lato Sender).
			
		FileInputStream is = new FileInputStream("certificates\\ClientSender\\keystore.jks");

	    KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
	    keystore.load(is, "password".toCharArray());

	    String alias = "clientsend";

	    Key key = keystore.getKey(alias, "password".toCharArray());
	    
	   //*****************************************************************
	    
	    
	    
	    
	    //1 - Prendo il Certificato del mittente (mio,locale)
	    
	    CertificateFactory factSender = CertificateFactory.getInstance("X.509");
	    File fSender = new File("certificates\\ClientSender\\ClientSendCertificate.cer");
	    boolean wasNotPresentSender = false;
	    
	    //Se il certificato non è presente, lo devo scaricare dal server(e avrò la versione più aggiornata)
	     if(!fSender.exists()) {
	    	
	    	System.out.println("Certificato mittente non trovato!\n");
	    	URL url = new URL("http://localhost:8080/CertificateServer/getCertificate?id=1");
			File file = new File("certificates\\ClientSender\\ClientSendCertificate.cer");
			FileUtils.copyURLToFile(url, file);
			wasNotPresentSender = true;
	    }
	    
	    FileInputStream fisSender = new FileInputStream ("certificates\\ClientSender\\ClientSendCertificate.cer");
	    X509Certificate cerSender = (X509Certificate) factSender.generateCertificate(fisSender);
	    
	    
	    //2 - Controllo se il certificato è ancora valido
	    
	    Date myCerDate = cerSender.getNotAfter();
	    Date actDate = new Date();
	    
	    //Se il certificato che ho è scaduto E se non l'ho già scaricato (perchè in quel caso è già aggiornato), lo riscarico
	    if(actDate.after(myCerDate) && !wasNotPresentSender ) {
	    	
	    	
			URL url = new URL("http://localhost:8080/CertificateServer/getCertificate?id=1");
			File file = new File("certificates\\ClientSender\\ClientSendCertificate.cer");
			FileUtils.copyURLToFile(url, file);
			
			fisSender = new FileInputStream ("certificates\\ClientSender\\ClientSendCertificate.cer");
		    cerSender = (X509Certificate) factSender.generateCertificate(fisSender);
	    
	    }
	    	
	    
	    //..Altrimenti possiamo procedere con il certificato che abbiamo
	    	
	    	PublicKey publicKey = cerSender.getPublicKey();
	    	
	    	//Questa non la avremo mai: la prendiamo solo per mostrare l'uso della classe Signature.
	    	PrivateKey pk = (PrivateKey)key;
	    	System.out.println("Byte della chiave:"+pk.getEncoded());
	    	
	    	String chiave = new String(Base64.encode(pk.getEncoded()));
	    	System.out.println("La mia chiave è (con string encode):"+chiave);
	    	byte [] newkey = Base64.decode(chiave);
	    	System.out.println("La mia chiave è (rifacendo il decode):"+new String(Base64.encode(newkey)));
	    	
	    	
	    	
	    	
	    	String message = "Hello, world!";
	    	
	    	//3. A questo punto dobbiamo ottenere la firma del messaggio.
	    	
	    	//Mostriamo due approcci:
	    	//3.1 - Uso della classe java Signature 
	    	
	    	/* Questo approccio 'nasconde' e semplifica le operazioni: basta infatti fornire il messaggio, 
	    	*  il tipo di hash da applicare e la chiave privata e la classe ci 
	    	*  restituirà i bytes che corrispondono alla firma (già crittata con la chiave privata fornita).
	    	*  Nota: è importante che l'invocazione del metodo getInstance della classe Signature avvenga 
	    	*  specificando l'algoritmo corrispondente alle chiavi in nostro possesso (RSA, nel nostro caso).
	    	*/
	    	
	    	Signature signatureProvider = Signature.getInstance("MD5withRSA");
	    	signatureProvider.initSign(pk);
	    	
	    	signatureProvider.update(message.getBytes());
	    	byte [] java_signature = signatureProvider.sign();
	    	
	    	System.out.println("Signature prodotta dalla classe java Signature:");
	    	System.out.println(new String(Base64.encode(java_signature)));
	    	
	    	
	    	/* Attraverso il metodo verify offerto dalla classe Signature, mostriamo la validità
	    	 * della firma ottenuta. 
	    	 */
	    	
	    	signatureProvider.initVerify(publicKey);
	    	signatureProvider.update(message.getBytes());
	    	boolean verifyResultJava = signatureProvider.verify(java_signature);
	    	
	    	System.out.println("Esito verifica firma CON java Signature:"+verifyResultJava);
	    	System.out.println("\n");
	    	
	    	
	    	
	    	
	    	//3.2 - Approccio 'grezzo' senza classe Java Signature
	    	
	    	/* Dal momento che noi avremo la chiave privata contenuta nell'Arduino, non possiamo usare l'approccio
	    	 * che sfrutta la classe Signature, poichè i requisiti di sicurezza prevedono che la chiave privata
	    	 * non esca mai dall'arduino.
	    	 * Di conseguenza procederemo secondo l'approccio classico: a partire dal messaggio, ne ricaviamo l'impronta
	    	 * (digest) usando un algoritmo di hash, dopodichè crittiamo l'hash ottenuto usando la chiave privata e abbiamo
	    	 * la nostra firma.
	    	 */
	    	
	    	/* Si introduce però una complicazione: una volta calcolato l'hash del messaggio, non 
	    	 * possiamo direttamente applicare la chiave privata: osservando il RFC 3447 notiamo infatti che l'hash
	    	 * del messaggio deve essere concatenato con un identificatore (opportunamente codificato) dell'algoritmo
	    	 * di hashing che abbiamo scelto.
	    	 * Questo particolare ci veniva 'nascosto' dall'uso della classe Signature, che ci restituiva direttamente
	    	 * i bytes della firma.
	    	
	    	/* Nota sull'Arduino: le operazioni appena descritte dovrebbero essere realizzate sull'Arduino.
	    	 * In questo modo noi invieremmo direttamente il messaggio da crittare all'Arduino, e quest'ultimo si 
	    	 * occuperebbe di calcolare l'hash, effettuare la concatenazione sopra citata, e crittare il tutto
	    	 * con la chiave privata; al termine, invierà la firma completa.
	    	 * Non sono però sicuro che lato Arduino tutto il discorso relativo alla concatenazione sopra citato
	    	 * sia effettivamente realizzabile: se fosse questo il caso, dovremmo procedere così: inviamo il messaggio
	    	 * da crittare all'Arduino, questo calcola l'hash del messaggio e ce lo restituisce (senza fare concatenazione).
	    	 * Lato client utilizziamo la libreria BouncyCastle che possiede metodi ad-hoc per 
	    	 * identificare l'algoritmo di hashing e concatenarlo all'hash precedentemente ottenuto dall'Arduino.
	    	 * Una volta ottenuto l'hash completo lato client, contattiamo l'Arduino e glielo inviamo, cosicchè
	    	 * possa usare la sua chiave privata per crittarlo, per poi restituirci quindi la firma completa.
	    	 * (Nell'esempio che sto riportando, ho seguito quest'ultimo approccio) 
	    	 */
	    	
	    	
	    	//Prima di tutto è necessario inviare il messaggio da crittare all'Arduino.
	    	//Quest'ultimo svolgerà le operazioni riportate sotto usando il messaggio che invieremo.
	    	
	    	//*************Inizio Operazioni da svolgere su Arduino*******************
	    	
	    	String hashingAlgorithm = "MD5"; //algoritmo di hashing scelto
	    	
	    	MessageDigest msgDigestProvider = MessageDigest.getInstance(hashingAlgorithm);
	    	msgDigestProvider.update(message.getBytes());
	    	
	    	byte [] partialHash = msgDigestProvider.digest(); //Hash del messaggio
	    	
	    	//*************Fine Operazioni da svolgere su Arduino**********************
	    	
	    	/* Al termine delle operazioni, assumiamo che l'Arduino ci invii un array di byte
	    	 * che corrisponde all'hash del messaggio che gli avevamo inviato precedentemente.
	    	 * (nel nostro caso, corrisponderebbe alla variabile partialHash).
	    	 */
	    	
	    	
	    	/* Qui entra il gioco il discorso della concatenazione: sfuttando le librerie BouncyCastle,
	    	 * possiamo ricavare l'identificatore dell'algoritmo di hashing scelto e concatenarlo con
	    	 * l'hash del messaggio che ci è stato precedentemente restituito dall'Arduino.
	    	 */
	    	
	    	DigestAlgorithmIdentifierFinder hashAlgorithmFinder = new DefaultDigestAlgorithmIdentifierFinder();
	        AlgorithmIdentifier hashingAlgorithmIdentifier = hashAlgorithmFinder.find(hashingAlgorithm);
	    	
	        DigestInfo digestInfo = new DigestInfo(hashingAlgorithmIdentifier, partialHash);
	        
	        byte [] messageHash = digestInfo.getEncoded(); //Hash completo, che rispetta le specifiche RFC 3447
	        
	        
	        /* Una volta ottenuto l'hash completo, usiamo la chiave privata per crittarlo.
	         * In tal caso assumiamo di inviare l'hash completo (messageHash) all'Arduino, che
	         * userà la propria chiave privata per crittarlo per poi restituirci la firma completa (ovvero l'hash crittato).
	         * 
	         */
	        
	        //**************Inizio Operazioni da svolgere sull'Arduino********************
	        
	        Cipher encryptCipher = Cipher.getInstance("RSA");
	        encryptCipher.init(Cipher.ENCRYPT_MODE, pk);
	        
	        byte [] encryptedHash = encryptCipher.doFinal(messageHash);
	        
	        //**************Fine Operazioni da svolgere sull'Arduino********************
	        
	        //Assumiamo che l'Arduino ci restituirà l'hash crittato che ha calcolato (nel nostro caso corrisponde
	        //alla variabile encryptedHash).
	        
	        
	        System.out.println("Signature prodotta da hashing + encrypting 'grezzo':");
	        System.out.println(new String(Base64.encode(encryptedHash)));
	        
	        /* Con lo stesso approccio adottato precedentemente, verifichiamo la validità della nostra firma
	         * (utilizzando stavolta la classe Signature).
	         */
	        
	        Signature sigVerifier = Signature.getInstance("MD5withRSA");
	        sigVerifier.initVerify(publicKey);
	        sigVerifier.update(message.getBytes());
	        boolean verifyResultNoJava = sigVerifier.verify(encryptedHash);
	        
	        System.out.println("Esito verifica firma ottenuta SENZA java Signature:"+verifyResultNoJava);
	        System.out.println("\n");
	        
	        
	        /* A questo punto assumiamo di avere disponibile il messaggio (in chiaro) e la firma.
	         * Dobbiamo quindi soltanto inviare tali informazioni al ricevente: ma prima di farlo, dobbiamo crittare anche 
	         * il messaggio da inviare.\\
	         * Per fare questo mi servirà il certificato del ricevente, da cui dovrò estrarre la chiave pubblica che 
	         * utilizzerò per crittare il messaggio.
	         */
	        
	        
	        //4. Prelevo (localmente) il certificato del ricevente
	        
	        CertificateFactory factRec = CertificateFactory.getInstance("X.509");
	        File fReceiver = new File("certificates\\ClientReceiver\\ClientReceiverCertificate.cer");
	        boolean wasNotPresentReceiver = false;
	        
	        //Se non ho il certificato del ricevente, lo scarico
	        if(!fReceiver.exists()) {
	        	
	        	System.out.println("Certificato ricevente non trovato!\n");
		    	URL url = new URL("http://localhost:8080/CertificateServer/getCertificate?id=2");
				File file = new File("certificates\\ClientReceiver\\ClientReceiverCertificate.cer");
				FileUtils.copyURLToFile(url, file);
				wasNotPresentReceiver = true;
	        }
	        
	        FileInputStream fisRec = new FileInputStream ("certificates\\ClientReceiver\\ClientReceiverCertificate.cer");
		    X509Certificate certRec = (X509Certificate) factRec.generateCertificate(fisRec);
		    
		    //5. Controllo se il certificato del ricevente è scaduto
		    
		    Date recCerDate = certRec.getNotAfter();
		    
		    //Se il certificato è scaduto E se non ho già scaricato il certificato (in quel caso sarebbe già aggiornato), lo riscarico
		    if(actDate.after(recCerDate) && !wasNotPresentReceiver) {
		    	
		    	System.out.println("Certificato del ricevente scaduto!");
		    	URL url = new URL("http://localhost:8080/CertificateServer/getCertificate?id=2");
				File file = new File("certificates\\ClientReceiver\\ClientReceiverCertificate.cer");
				FileUtils.copyURLToFile(url, file);
				
				fisRec = new FileInputStream ("certificates\\ClientReceiver\\ClientReceiverCertificate.cer");
			    certRec = (X509Certificate) factSender.generateCertificate(fisRec);
		    	
		    	
		    }
		    	
		    
		    //Altrimenti possiamo procedere con quello che abbiamo
		    	
		    	
		    	//6. Utilizzo la chiave pubblica per crittare il messaggio da inviare
		    	
		    	PublicKey recPublicKey = certRec.getPublicKey();
		    	
		    	Cipher enCipher = Cipher.getInstance("RSA");
		    	enCipher.init(Cipher.ENCRYPT_MODE, recPublicKey);
		    	
		    	byte [] encryptedMessage = enCipher.doFinal(message.getBytes());
		    	
		    	
		    	System.out.println("Messaggio in chiaro:"+message);
		    	System.out.println("Messaggio crittato (con chiave pubblica):"+new String(Base64.encode(encryptedMessage)));
		    	
		    	
		    	//Ricavo la chiave privata del receiver per verificare che applicando la chiave privata al messaggio crittato, ottengo di nuovo l'originale.
		    	
		    	FileInputStream fis_rec = new FileInputStream("certificates\\ClientReceiver\\keystore.jks");

			    KeyStore keystore_rec = KeyStore.getInstance(KeyStore.getDefaultType());
			    keystore_rec.load(fis_rec, "password".toCharArray());

			    String alias_rec = "clientreceiver";

			    Key rec_key = keystore_rec.getKey(alias_rec, "password".toCharArray());
			    
			    PrivateKey pk_rec = (PrivateKey)rec_key;
			    
			    Cipher deCipher = Cipher.getInstance("RSA");
			    deCipher.init(Cipher.DECRYPT_MODE, pk_rec);
			    byte [] decryptedMessage = deCipher.doFinal(encryptedMessage);
			    
			    System.out.println("Messaggio decrittato (con chiave privata):"+new String(decryptedMessage,"UTF-8"));
			    
			    
		    	
		    	
		    	Socket socket = new Socket("localhost",500);
		    	
		    	ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
		    	
		    	SecureKey m = new SecureKey(encryptedMessage,encryptedHash);
		    	
		    	oos.writeObject(m);
		    	
		    	
		    	oos.close();
		    	
		    	socket.close();
	        
	        
	    	
	    
	    
	    
	  
	    
	    
	    
	    
	    
	 
	    
	}catch(Exception e) {
		e.printStackTrace();
	}
	    
		
	}
	
	
	
	
	
		
}
	


