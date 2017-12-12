package it.chat.threads;
import java.io.DataOutputStream;
import java.io.PipedOutputStream;
import java.net.SocketException;
import it.chat.activechat.ActiveChat;
import it.chat.gui.ChatFrame;
import it.chat.helpers.MessagingHelper;
import it.sm.messages.Messaggio;

public class MessageListenerThread extends Thread{
	
	/* Thread che viene attivato quando avviamo la comunicazione: è specifico per una singola chat.
	 * Si occupa di gestire la ricezione dei messaggi dall'interlocutore e di spedirli
	 * all'updater thread per la visualizzazione.
	 * 
	 */
	
	
	private ActiveChat actChat;
	private int countMsg;
	
	//Costruttore: prende in ingresso la ActiveChat a cui il thread deve essere associato.
	public MessageListenerThread(ActiveChat ac) {
		this.countMsg = 0;
		this.actChat = ac;
		
	}
	
	public void run() {
		
		System.out.println("[MsgListenerThread] Attivato MsgListener\n");
		
		PipedOutputStream pos = null;
		DataOutputStream dos = null;
		
		Messaggio msg = null;
		
		
		try {
		
			//inizializzo le strutture dati che ci serviranno per spedire i dati all'updater thread
			pos = new PipedOutputStream(this.actChat.getUpdatesPipe());
			dos = new DataOutputStream(pos);
		
			MessagingHelper mh = MessagingHelper.getInstance();
		 
			//Ciclo..
			while(true) {
		
				//Mi metto in attesa di messaggi sulla socket settata nella active chat
				msg = (Messaggio)this.actChat.getOIS().readObject();
		
				//Se il messaggio che ricevo è diverso da null..
				if(msg!=null) {
					
					//Aumento il conteggio di messaggi
					countMsg++;
		
					//Se il messaggio che ho ricevuto è il primo messaggio..
					if(countMsg == 1) {
				
						//Prendo la porta (numero di telefono) del mittente
						//e la setto nella active chat
						int destPort = msg.getSenderPort();
						this.actChat.setDest(destPort);
				
						//Essendo il primo messaggio che ricevo, devo anche attivare 
						//il chat frame
						if(this.actChat.getFrame()==null) {
							
							//"" da sostituire con il currentuser, informazione da prendere sul DB
							ChatFrame cf = new ChatFrame("",msg.getSender(),this.actChat.getDest());
							this.actChat.setFrame(cf);
							cf.setVisible(true);
						}
				
					//Avendo inizializzato il chat frame, devo anche attivare gli updates per poter
					//visualizzare i messaggi nel chatBox
					mh.startUpdates(this.actChat.getDest());
				
			}
			
				//Scrivo il contenuto del messaggio sulla pipe che è connessa all'updater thread, cosi
				//che possa mostrarli sul chatBox
				dos.writeUTF(msg.getMsg());
		
				//Risveglio l'updater thread, che era in attesa sul semaforo
				this.actChat.getChatSem().release();
			
		
		}else{
			//Se ricevo un msg null, vuol dire che l'interlocutore ha chiuso la chat
			//Quindi devo chiuderla anche io
			System.out.println("[MsgListenerThread] Msg = null, chiudo!");
			
			//CloseChat lo chiamo con nullSend = false, perchè come detto
			//effettuo una chiusura 'passiva', e non c'è bisogno che invio
			//un messaggio = null all'interlocutore per notificargli la chiusura della chat
			mh.closeChat(this.actChat.getDest(), false);
			
			//interrompo gli updates sul chatbox
			mh.stopUpdates(this.actChat.getDest());
			
			break;
			
		}
		
		}
		
		}catch(SocketException e) {
			
			//Quando il chatframe viene chiuso, automaticamente andiamo a chiudere anche la socket
			//su cui il thread sta facendo readObject: di conseguenza verrà lanciata una SocketException,
			//che farà uscire dal while(true) il thread
			if(e.getMessage().contains("closed")) {
				System.out.println("[MsgListenerThread] Esco, socket chiusa");
			}else {
				e.printStackTrace();
			}
			
			//Rilascio le risorse
			try {
				pos.close();
				dos.close();
			}catch(Exception ex) {
				ex.printStackTrace();
			}
			
		//Tutte le altre eccezioni le registro normalmente
		}catch(Exception ex) {
			ex.printStackTrace();
			
			//Rilascio le risorse
			try {
				pos.close();
				dos.close();
			}catch(Exception exx) {
				exx.printStackTrace();
			}
		}
		
		
	}

}
