package it.chat.threads;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import it.chat.activechat.ActiveChat;
import it.chat.helpers.MessagingHelper;

/* Thread che viene avviato quando avviamo il LandingFrame, si occupa di avviare l'ascolto sulla porta
 * corrispondente al 'numero di telefono' dell'utente, e di gestire le richieste di comunicazione.
 */
public class ListenerThread extends Thread{
	
	private ServerSocket ss;
	
	
	public ListenerThread(ServerSocket s) {
		this.ss = s;
	}
	
	public void run() {
		
		try {
		
		System.out.println("[Listener Thread] In ascolto sulla porta:"+this.ss.getLocalPort());
		
		//Ciclo..
		while(true) {
		
		
			//In attesa di connessioni
			Socket s = ss.accept();
			s.setKeepAlive(true);
			
			//Ricevuta richiesta di connessione
			
			//Creo una nuova ActiveChat, passando la socket che ho appena ottenuto dalla accept()
			//Passo inoltre un valore di destinationPort (ovvero la porta dell'utente con cui sto parlando, il suo 'numero di telefono')
			//pari a 0:
			//lo faccio perchè in questo momento non conosco la sua identità in termini di 'numero di telefono'
			//da cui mi sta chiamando, ma successivamente quando riceverò il primo messaggio questo porterà
			//con se anche la porta (numero di telefono) del mittente, e a quel punto setterò il campo
			//della active chat corrispondente.
			ActiveChat ac = new ActiveChat(0,s);
			
			//Inizializzo gli stream della socket
			ac.setOOS(new ObjectOutputStream(s.getOutputStream()));
			ac.setOIS(new ObjectInputStream(s.getInputStream()));
			
			//Aggiungo la active chat alla lista
			MessagingHelper mh = MessagingHelper.getInstance();
			mh.addActiveChat(ac);
			
			System.out.println("[ListenerThread] Aggiunto nuova active chat!");
			
			//Dopo aver creato la nuova active chat, avvio il MessageListenerThread che si occuperà di gestire
			//la ricezione dei messaggi sulla active chat appena creata
			MessageListenerThread t = new MessageListenerThread(ac);
			t.start();
			
			
			
		}	
			
			
		} catch (SocketException e) {
			//Quando chiamo il metodo stopListening, chiuderò la socket su cui è in ascolto il thread,
			//e questo causerà una SocketException, che gestisco normalmente
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
