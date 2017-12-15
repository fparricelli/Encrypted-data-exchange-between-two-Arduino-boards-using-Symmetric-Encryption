package it.chat.helpers;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import it.chat.activechat.ActiveChat;
import it.chat.gui.ChatFrame;
import it.chat.threads.ListenerThread;
import it.chat.threads.MessageListenerThread;
import it.chat.threads.UpdaterThread;
import it.sm.exception.ActiveChatNotFoundException;
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
	
	private SSLServerSocket listeningSocket;
	
	private int listeningPort;
	
	private static final int STARTER = 1;
	
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
	public boolean startListening(int port, String currentIdentity) {
		
		try {
		
			//Creo ServerSocket
			SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory)SSLContext.getDefault().getServerSocketFactory();
			this.listeningSocket = (SSLServerSocket)sslserversocketfactory.createServerSocket(port);
			this.listeningPort = port;
			
			//Avvio thread di ascolto sulla socket precedentemente creata
			ListenerThread lt = new ListenerThread(this.listeningSocket, currentIdentity);
			//Avvio il thread
			lt.start();
			
			//Restituisco true, visto che la procedura di ascolto � stata inizializzata correttamente
			return true;
			
		}catch(Exception e) {
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
		ActiveChat ac;
		try {
			
			ac = findActiveChat(destinationPort);
			Messaggio msgg = new Messaggio(this.listeningPort,sender,msg);
			
			ac.getOOS().reset();
			ac.getOOS().writeObject(msgg);
			ac.getOOS().flush();
			
			return true;
			
		}catch (ActiveChatNotFoundException e) {
			
			//Se non trovo chat con l'interlocutore vuol dire che sto scambiando con lui
			//il primo messaggio
			System.out.println("[sendMessage]"+e.getMessage());
			
			//Creo la socket che verra' usata per la comunicazione con l'interlocutore
			SSLSocket s;
			
			try {
				
				SSLSocketFactory sslsocketfactory = (SSLSocketFactory)SSLContext.getDefault().getSocketFactory();
			    s = (SSLSocket)sslsocketfactory.createSocket("localhost", destinationPort);
				s.setKeepAlive(true);
				
				
				//Inizializzo un nuovo oggetto ActiveChat, che rappresenta la chat che sto iniziando
				//A questo oggetto fornisco innanzitutto la socket appena creata, e la porta del destinatario con cui
				//voglio comunicare
				ac = new ActiveChat(destinationPort,s);
				ac.setOOS(new ObjectOutputStream(s.getOutputStream()));
				ac.setOIS(new ObjectInputStream(s.getInputStream()));
				
				//Inizializzo il chatFrame di riferimento per questa chat, passato come input
				ac.setFrame(cf);
				
				//Aggiungo l'oggetto ActiveChat alla lista di chat attive
				addActiveChat(ac);
				
				//Dopo aver creato l'oggetto ActiveChat, devo mettermi in ascolto dei messaggi che
				//saranno inviati dall'interlocutore sulla socket che ho creato precedentemente
				//Passo tutte le informazioni precedentemente settate ad un MessageListenerThread,
				//Che si occupera' di ricevere i messaggi dall'interlocutore 
				MessageListenerThread mlt = new MessageListenerThread(ac, STARTER);
				mlt.start();
				
				//Avvio gli aggiornamenti, che mi permetteranno di visualizzare i messaggi ricevuti sul chatFrame
				//Che ho aperto per parlare con l'interlocutore 
				startUpdates(destinationPort);
				
				//Richiamo la sendMessage, ora che la active chat � presente
				sendMessage(sender,destinationPort,msg,cf);
				
			}catch (Exception e1) {
				e1.printStackTrace();
				
			}
			
		}catch(IOException e) {
			e.printStackTrace();
			
		}
		
		return false;
		
		

		
		
		
		
	}
	
	public boolean sendChatMessage(String sender, int destinationPort, String msg, String msg_key, ChatFrame cf) {
		ActiveChat ac;
		try {
			
			ac = findActiveChat(destinationPort);
			Messaggio msgg = new Messaggio(this.listeningPort,sender,msg, msg_key);
			
			ac.getOOS().reset();
			ac.getOOS().writeObject(msgg);
			ac.getOOS().flush();
			
			return true;
			
		}catch (ActiveChatNotFoundException e) {
			
			//Se non trovo chat con l'interlocutore vuol dire che sto scambiando con lui
			//il primo messaggio
			System.out.println("[sendMessage]"+e.getMessage());
			
			//Creo la socket che verra' usata per la comunicazione con l'interlocutore
			SSLSocket s;
			
			try {
				
				SSLSocketFactory sslsocketfactory = (SSLSocketFactory)SSLContext.getDefault().getSocketFactory();
			    s = (SSLSocket)sslsocketfactory.createSocket("localhost", destinationPort);
				s.setKeepAlive(true);
				
				
				//Inizializzo un nuovo oggetto ActiveChat, che rappresenta la chat che sto iniziando
				//A questo oggetto fornisco innanzitutto la socket appena creata, e la porta del destinatario con cui
				//voglio comunicare
				ac = new ActiveChat(destinationPort,s);
				ac.setOOS(new ObjectOutputStream(s.getOutputStream()));
				ac.setOIS(new ObjectInputStream(s.getInputStream()));
				
				//Inizializzo il chatFrame di riferimento per questa chat, passato come input
				ac.setFrame(cf);
				
				//Aggiungo l'oggetto ActiveChat alla lista di chat attive
				addActiveChat(ac);
				
				//Dopo aver creato l'oggetto ActiveChat, devo mettermi in ascolto dei messaggi che
				//saranno inviati dall'interlocutore sulla socket che ho creato precedentemente
				//Passo tutte le informazioni precedentemente settate ad un MessageListenerThread,
				//Che si occupera' di ricevere i messaggi dall'interlocutore 
				MessageListenerThread mlt = new MessageListenerThread(ac, STARTER);
				mlt.start();
				
				//Avvio gli aggiornamenti, che mi permetteranno di visualizzare i messaggi ricevuti sul chatFrame
				//Che ho aperto per parlare con l'interlocutore 
				startUpdates(destinationPort);
				
				//Richiamo la sendMessage, ora che la active chat � presente
				sendMessage(sender,destinationPort,msg,cf);
				
			}catch (Exception e1) {
				e1.printStackTrace();
				
			}
			
		}catch(IOException e) {
			e.printStackTrace();
			
		}
		
		return false;
	
	}
	
	


	/* Metodo che viene invocato quando scambio il primo messaggio con l'interlocutore.
	 * Si occupa di inizializzare un UpdaterThread, al quale vengono inoltrati in pipeline i messaggi ricevuti
	 * dall'interlocutore e che si occupa di spedire tali messaggi sul chatBox per la visualizzazione.
	 */
	public void startUpdates(int destPort) {
		
		try {
			
			ActiveChat ac = findActiveChat(destPort);
			if(ac.getUpdaterThread() == null) {
				ac.setUpdaterThread(new UpdaterThread(ac.getFrame().getChatBox(),ac.getUpdatesPipe(),ac.getChatSem()));
				ac.getUpdaterThread().start();
			}
			
		} catch (ActiveChatNotFoundException e) {
			System.out.println("[startUpdates]"+e.getMessage());
		}
	}
	
	
	
	
	
	
	/* Metodo che viene chiamato quando chiudiamo il chat frame che � attivo:
	 * se chiudo la chat devo infatti interrompere l'updater thread che spara i messaggi ricevuti sul chat box.
	 * Si occupa di chiamare un metodo interrupt() sull'updater thread associato alla chat.
	 */
	public void stopUpdates(int destPort) {
		
		try {
			
			ActiveChat ac = findActiveChat(destPort);
			if(ac.getUpdaterThread() != null) {
					ac.getUpdaterThread().interrupt();
			}
			removeActiveChat(ac);
			
		} catch (ActiveChatNotFoundException e) {
			System.out.println("[stopUpdates]"+e.getMessage());
			
		}

		
		
	}
	
	
			
	/* Metodo che viene invocato tutte le volte che chiudiamo la chatFrame della chat attualmente attiva.
	 * Si occupa di chiudere/rilasciare tutte le risorse associate alla active chat.
	 * Utilizza un parametro booleano nullSend che specifica se l'azione di chiusura chat � intenzionale
	 * (ovvero: ho chiuso io il chat frame, e in quel caso nullSend = true poich� devo inviare un messaggio
	 * speciale all'interlocutore per dirgli che ho chiuso la chat) oppure se � 'passiva' (quindi nullSend = false,
	 * il che vuol dire che non ho chiuso io la chat ma ho ricevuto un messaggio speciale che mi notificava
	 * la chiusura della chat da parte dell'altro interlocutore).
	 * 	
	 */
	
	public void closeChat(int dest, boolean nullSend) {
		
		synchronized(this.chats) {
			
			try {
				
				ActiveChat a = findActiveChat(dest);
				
				//Se sono io a chiudere la chat, allora devo inviare un messaggio speciale (null)
				//All'interlocutore, per notificarlo della chiusura della chat
				if(nullSend) {
					a.getOOS().reset();
					a.getOOS().writeObject(new Messaggio(this.listeningPort,"END_COMMUNICATION",""));
					a.getOOS().flush();
				}
				
				//Chiudo e rilascio le risorse utilizzate
				a.getOOS().close();
				a.getSocket().close();
				a.getUpdatesPipe().close();
				
				//Controllo se il chatFrame associato alla acive chat che sto chiudendo � ancora
				//visibile: in caso affermativo, lo chiudo mostrando un messaggio (vedi dettagli
				//metodo interruptCommunication())
				if(a.getFrame().isVisible()) {
					a.getFrame().interruptCommunication();
				}
				
				
				
			}catch(ActiveChatNotFoundException e) {
				System.out.println("[closeChat]"+e.getMessage());
			}catch(IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	//metodo di utility, permette di aggiungere una ActiveChat alla lista.
	public void addActiveChat(ActiveChat ac) {
		
		synchronized(this.chats) {
			this.chats.add(ac);
		}
		
	}
	
	
	public void removeActiveChat(ActiveChat ac) {
		
		synchronized(this.chats) {
			try {
				
				ActiveChat a = findActiveChat(ac.getDest());
				this.chats.remove(a);
			
			}catch(ActiveChatNotFoundException e) {
				System.out.println("[removeActiveChat]"+e.getMessage());
			}
		}
	}
	
	
	//Permette di cercare tra la lista delle active chats dell'utente corrente.
	public synchronized ActiveChat findActiveChat(int dest) throws ActiveChatNotFoundException {
		
		synchronized(this.chats) {
			
			for(int i = 0;i<chats.size();i++) {
			
				if(chats.get(i).getDest() == dest) {
					return chats.get(i);
				}
			}
		}
		
		throw new ActiveChatNotFoundException();
	}
	
		
	//metodo di utility che verifica se ci sono chat attive nella lista dell'utente.
	public boolean hasActiveChats() {
		return this.chats.size() > 0 ? true : false;
	}
	
	
		
	}
	
	
	


