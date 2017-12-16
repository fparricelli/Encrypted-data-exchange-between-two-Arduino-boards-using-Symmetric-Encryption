package it.chat.threads;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import it.chat.activechat.ActiveChat;
import it.chat.helpers.MessagingHelper;

/* Thread che viene avviato quando avviamo il LandingFrame, si occupa di avviare l'ascolto sulla porta
 * corrispondente al 'numero di telefono' dell'utente, e di gestire le richieste di comunicazione.
 */
public class ListenerThread extends Thread{
	
	int client_type;
	
	private SSLServerSocket ss;
	private String currentIdentity;
	
	public ListenerThread(SSLServerSocket s, String ci) {
		this.ss = s;
		this.currentIdentity = ci;
		
	}
	
	public void run() {
		
		try {
		
		System.out.println("[Listener Thread] "+this.currentIdentity+" in ascolto sulla porta:"+this.ss.getLocalPort());
		
		//Ciclo..
		while(true) {
		
		
			//In attesa di connessioni
			SSLSocket s = (SSLSocket) ss.accept();
			s.setKeepAlive(true);
		
			
			//Ricevuta richiesta di connessione
			
			//Creo una nuova ActiveChat, passando la socket che ho appena ottenuto dalla accept()
			//Passo inoltre un valore di destinationPort (ovvero la porta dell'utente con cui sto parlando, il suo 'numero di telefono')
			//pari a 0:
			//lo faccio perch� in questo momento non conosco la sua identit� in termini di 'numero di telefono'
			//da cui mi sta chiamando, ma successivamente quando ricever� il primo messaggio questo porter�
			//con se anche la porta (numero di telefono) del mittente, e a quel punto setter� il campo
			//della active chat corrispondente.
			ActiveChat ac = new ActiveChat(0,s);
			
			//Inizializzo gli stream della socket
			ac.setOOS(new ObjectOutputStream(s.getOutputStream()));
			ac.setOIS(new ObjectInputStream(s.getInputStream()));
			
			
			ac.setCurrentIdentity(this.currentIdentity);
			
			//Aggiungo la active chat alla lista
			MessagingHelper mh = MessagingHelper.getInstance();
			mh.addActiveChat(ac);
			
			System.out.println("[ListenerThread] Aggiunto nuova active chat!");
			
			//Dopo aver creato la nuova active chat, avvio il MessageListenerThread che si occuper� di gestire
			//la ricezione dei messaggi sulla active chat appena creata
			MessageListenerThread t = new MessageListenerThread(ac, 2);
			t.start();
			
			
			
		}	
			
			
		} catch (SocketException e) {
			//Quando chiamo il metodo stopListening, chiuder� la socket su cui � in ascolto il thread,
			//e questo causer� una SocketException, che gestisco normalmente
			if(e.getMessage().contains("closed")) {
				System.out.println("[ListenerThread] Ascolto terminato (socket chiusa)");
			}else {
				e.printStackTrace();
			}
		//Tutte le altre eccezioni verranno invece registrate	
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		
		
		
		
		
		
	}

}
