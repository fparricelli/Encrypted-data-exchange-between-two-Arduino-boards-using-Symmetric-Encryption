package it.chat.helpers;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import it.chat.activechat.ActiveChat;
import it.chat.gui.ChatFrame;
import it.chat.threads.ListenerThread;
import it.chat.threads.MessageListenerThread;
import it.chat.threads.UpdaterThread;
import it.sm.messages.Messaggio;


/* Classe SINGLETON che si occupa di gestire la messaggistica dell'utente corrente.
 * Mantiene una lista delle chat attive per l'utente, e un insieme di metodi
 * che permettono di avviare/interrompere l'ascolto, gli aggiornamenti, e di inviare messaggi.
 * 
 */
public class MessagingHelper {
	
	//Tiene traccia di tutte le chat attive per l'utente.
	//Quando una chatFrame viene chiusa, andremo a rimuovere anche la corrispondente entry da questo ArrayList.
	//(Nonostante abbiamo previsto una sola chat per volta, se dovessimo avere tempo rimanente provo a realizzarlo multichat)
	private ArrayList<ActiveChat> chats;
	
	private static MessagingHelper instance;
	
	private ServerSocket listeningSocket;
	
	private int listeningPort;
	
	
	

	public static MessagingHelper getInstance() {
		if (instance == null) {

			instance = new MessagingHelper();

		}

	return instance;

	}
	
	private MessagingHelper() {
		this.listeningPort = 0;
		this.chats = new ArrayList<ActiveChat>();
	}
	
	
	
	//Metodo che viene chiamato all'apertura del LandingFrame, con il quale il soggetto si mette in ascolto
	//sul proprio "numero di telefono", ovvero sulla porta specificata come parametro di input.
	public boolean startListening(int port) {
		
		try {
		
			//Creo ServerSocket 
			this.listeningSocket = new ServerSocket();
			this.listeningSocket.bind(new InetSocketAddress("localhost",port));
			this.listeningPort = port;
			
			//Avvio thread di ascolto sulla socket precedentemente creata
			ListenerThread lt = new ListenerThread(this.listeningSocket);
			//Avvio il thread
			lt.start();
			
			//Restituisco true, visto che la procedura di ascolto è stata inizializzata correttamente
			return true;
			
		}catch(IOException e) {
			e.printStackTrace();
			
			//In caso di errori (ad es: porta occupata), restituisco false
			return false;
		}
		
	}
	
	//Metodo che viene chiamato quando chiudiamo il Landing Frame: si occupa quindi di interrompere l'ascolto,
	//andando a chiudere la listening Socket che avevamo creato all'apertura del Landing Frame
	//con il metodo startListening.
	public void stopListening() {
		
		this.listeningPort = 0;
		try {
			this.listeningSocket.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
		
}
	
	
	
	/* Metodo che viene chiamato ogniqualvolta, dal chatframe, clicchiamo su invia per inviare un messaggio
	 * all'interlocutore.
	 * Si occupa di inizializzare la chat con l'interlocutore richiesto, e di avviare la ricezione
	 * messaggi da quello stesso interlocutore.
	 */
	
	public boolean sendMessage(String sender, int destinationPort, String msg, ChatFrame cf) {
		
		try {
			
			//Controllo se ci sono chat già attive con l'interlocutore richiesto (destinationPort)
			ActiveChat ac = findActiveChat(destinationPort);
			
			//Se non trovo chat con l'interlocutore vuol dire che sto scambiando con lui
			//il primo messaggio
			if(ac==null) {
				
				System.out.println("[Send Message] Non ho trovato chat attive con la porta:"+destinationPort);
				
				//Creo la socket che verrà usata per la comunicazione con l'interlocutore
				Socket s = new Socket("localhost",destinationPort);
				s.setKeepAlive(true);
				
				//Inizializzo un nuovo oggetto ActiveChat, che rappresenta la chat che sto iniziando
				//A questo oggetto fornisco innanzitutto la socket appena creata, e la porta del destinatario con cui
				//voglio comunicare
				ac = new ActiveChat(destinationPort,s);
				//Dopodichè inizializzo gli stream della socket, settando le variabili membro specifiche
				ac.setOOS(new ObjectOutputStream(s.getOutputStream()));
				ac.setOIS(new ObjectInputStream(s.getInputStream()));
				
				//Inizializzo il chatFrame di riferimento per questa chat, passato come input
				ac.setFrame(cf);
				
				//Aggiungo l'oggetto ActiveChat alla lista di chat attive
				chats.add(ac);
				
				//Dopo aver creato l'oggetto ActiveChat, devo mettermi in ascolto dei messaggi che
				//saranno inviati dall'interlocutore sulla socket che ho creato precedentemente
				//Passo tutte le informazioni precedentemente settate ad un MessageListenerThread,
				//Che si occuperà di ricevere i messaggi dall'interlocutore 
				MessageListenerThread mlt = new MessageListenerThread(ac);
				mlt.start();
				
				
				//Avvio gli aggiornamenti, che mi permetteranno di visualizzare i messaggi ricevuti sul chatFrame
				//Che ho aperto per parlare con l'interlocutore 
				startUpdates(destinationPort);
				
			}else {
				//Ho già una chat attiva con l'interlocutore, quindi evito l'inizializzazione
				System.out.println("[Send Message] Ho trovato una chat attiva con la porta:"+destinationPort);
				
			}
			
			
			//A questo punto sono sicuro di avere una active chat con l'interlocutore.
			
			//Creo l'oggetto messaggio da inviare, passando la mia identità (sender string e mia porta di origine) insieme
			//al messaggio da inviare
			Messaggio msgg = new Messaggio(this.listeningPort,sender,msg);
			
			//Invio il messaggio 
			ac.getOOS().reset();
			ac.getOOS().writeObject(msgg);
			ac.getOOS().flush();
			
			//Restituisco true se tutto va a buon fine
			return true;
			
		
		}catch (IOException e) {
			e.printStackTrace();
			//restituisco false se si verificano errori
			return false;
		}
		
	}


	/* Metodo che viene invocato quando scambio il primo messaggio con l'interlocutore.
	 * Si occupa di inizializzare un UpdaterThread, al quale vengono inoltrati in pipeline i messaggi ricevuti
	 * dall'interlocutore e che si occupa di spedire tali messaggi sul chatBox per la visualizzazione.
	 */
	public void startUpdates(int destPort) {
		//Prima verifico che ci sia una chat attiva col destinatario specificato
		ActiveChat ac = findActiveChat(destPort);
		if(ac!=null) {
			//Se trovo una chat attiva, setto l'updater thread per quella active chat e lo avvio
			ac.setUpdaterThread(new UpdaterThread(ac.getFrame().getChatBox(),ac.getUpdatesPipe(),ac.getChatSem()));
			ac.getUpdaterThread().start();
		}else {
			//Altrimenti, messaggio di errore
			System.out.println("[startUpdates] Active chat non trovata con porta:"+destPort+", non posso iniziare updates!");
		}
		
	}
	
	/* Metodo che viene chiamato quando chiudiamo il chat frame che è attivo:
	 * se chiudo la chat devo infatti interrompere l'updater thread che spara i messaggi ricevuti sul chat box.
	 * Si occupa di chiamare un metodo interrupt() sull'updater thread associato alla chat.
	 */
	public void stopUpdates(int destPort) {
		//prima verifico che ci sia una active chat con l'interlocutore
		ActiveChat ac = findActiveChat(destPort);
		if(ac!=null) {
			//Se trovo una chat attiva, allora chiamo interrupt() sull'updater thread
			ac.getUpdaterThread().interrupt();
			//Dopodichè rimuovo l'active chat dalla lista delle chat attive, poichè lo stopUpdates()
			//è l'ultimo metodo che viene chiamato in caso di chiusura della chat
			//e di conseguenza è responsabile di 'chiudere la porta' rimuovendo la entry dell'arraylist
			chats.remove(ac);
		}else {
			System.out.println("[stopUpdates] Active chat non trovata per porta:"+destPort+", non posso fermare gli updates!");
		}
	}
	
	
	
	
			
	/* Metodo che viene invocato tutte le volte che chiudiamo la chatFrame della chat attualmente attiva.
	 * Si occupa di chiudere/rilasciare tutte le risorse associate alla active chat.
	 * Utilizza un parametro booleano nullSend che specifica se l'azione di chiusura chat è intenzionale
	 * (ovvero: ho chiuso io il chat frame, e in quel caso nullSend = true poichè devo inviare un messaggio
	 * speciale all'interlocutore per dirgli che ho chiuso la chat) oppure se è 'passiva' (quindi nullSend = false,
	 * il che vuol dire che non ho chiuso io la chat ma ho ricevuto un messaggio speciale che mi notificava
	 * la chiusura della chat da parte dell'altro interlocutore).
	 * 	
	 */
	public void closeChat(int dest, boolean nullSend) {
		
		//Cerco tra tutte le active chat..
		for(int i = 0;i<chats.size();i++) {
			
			//Quando trovo quella che mi interessa
			if(chats.get(i).getDest() == dest) {
				
				try {
				
				//Se sono io a chiudere la chat, allora devo inviare un messaggio speciale (null)
				//All'interlocutore, per notificarlo della chiusura della chat
				if(nullSend) {
					chats.get(i).getOOS().reset();
					chats.get(i).getOOS().writeObject(null);
					chats.get(i).getOOS().flush();
				}
				
				//Chiudo e rilascio le risorse utilizzate
				chats.get(i).getOOS().close();
				chats.get(i).getSocket().close();
				chats.get(i).getUpdatesPipe().close();
				
				//Controllo se il chatFrame associato alla acive chat che sto chiudendo è ancora
				//visibile: in caso affermativo, lo chiudo mostrando un messaggio (vedi dettagli
				//metodo interruptCommunication())
				if(chats.get(i).getFrame().isVisible()) {
					chats.get(i).getFrame().interruptCommunication();
				}
				
				
				
				}catch(Exception e) {
					e.printStackTrace();
					chats.remove(i);
				}
			}
		}
		
		
	}
			
	
	
	//metodo di utility, permette di aggiungere una ActiveChat alla lista.
	public void addActiveChat(ActiveChat ac) {
		
		this.chats.add(ac);
		
	}
	
	//Permette di cercare tra la lista delle active chats dell'utente corrente.
	public ActiveChat findActiveChat(int dest) {
		
		for(int i = 0;i<chats.size();i++) {
			
			if(chats.get(i).getDest() == dest) {
				return chats.get(i);
			}
		}
		return null;
	}
	
		
	//metodo di utility che verifica se ci sono chat attive nella lista dell'utente.
	public boolean hasActiveChats() {
		return this.chats.size() > 0 ? true : false;
	}
	
	
		
	}
	
	
	


