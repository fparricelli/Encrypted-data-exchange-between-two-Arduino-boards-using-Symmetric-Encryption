package it.chat.threads;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.net.Socket;
import java.net.SocketException;
import it.chat.activechat.ActiveChat;
import it.chat.gui.ChatFrame;
import it.chat.helpers.CertificateHelper;
import it.chat.helpers.MessagingHelper;
import it.sm.exception.CertificateNotFoundException;
import it.sm.keystore.aeskeystore.AESHardwareKeystore;
import it.sm.keystore.aeskeystore.MyAESKeystore;
import it.sm.messages.Messaggio;

public class MessageListenerThread extends Thread{
	
	/* Thread che viene attivato quando avviamo la comunicazione: ï¿½ specifico per una singola chat.
	 * Si occupa di gestire la ricezione dei messaggi dall'interlocutore e di spedirli
	 * all'updater thread per la visualizzazione.
	 * 
	 */
	
	
	private ActiveChat actChat;
	private int countMsg;
	private MyAESKeystore aesKeystore;
	private static final int STARTER = 1;
	private static final int NO_STARTER = 2;
	private int c_type;

	
	//Dati per l'handshake
	
	private String first_token;
	
	//Costruttore: prende in ingresso la ActiveChat a cui il thread deve essere associato.
	public MessageListenerThread(ActiveChat ac, int c) {
		this.c_type = c;
		this.actChat = ac;
		if(c_type == STARTER) {
			this.countMsg = 1;
		}else {
			this.countMsg = 0;
		}
			
		
	}
	
	public MessageListenerThread(ActiveChat ac) {
		this.countMsg = 0;
		this.actChat = ac;
		
	}
	
	public void run() {
		
		System.out.println("[MsgListenerThread] Attivato MsgListener " +c_type);
		
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
		
				//Se il messaggio che ricevo diverso da null..
				if(!(msg.getSender().equals("END_COMMUNICATION") && msg.getMsg().length() == 0)) {
					
					//Aumento il conteggio di messaggi
					countMsg++;
					
					//Se è il primo messaggio che ricevo allora devo inviare il mio token
					//Se ho ricevuto il messaggio, vuol dire che il soggetto con cui sto parlando è trusted.
					//Di conseguenza, devo inserire il suo certificato nel mio trust-store
					if(countMsg == 1) {
												
						int destPort = msg.getSenderPort();
						
						this.actChat.setDest(destPort);
						CertificateHelper ch = CertificateHelper.getInstance();
						String [] ids = msg.getSender().split(" ");
						
						String targetNome = ids[0];
						String targetCognome = ids[1];
						
						ch.getCertificate(targetNome, targetCognome);
						
						if(this.actChat.getFrame()==null) {
							
							ChatFrame cf = new ChatFrame(this.actChat.getCurrentIdentity(),msg.getSender(),this.actChat.getDest(),NO_STARTER);
							this.actChat.setFrame(cf);
							
						}
						
						System.out.println("[MsgListenerThread] Ricevuto primo token:"+msg.getMsg()+", avvio handshake");

						String token_to_send = "POPO";// aesKeystore.requireTokenToShare();
						
						first_token = msg.getMsg();

						mh.sendMessage(this.actChat.getCurrentIdentity(), this.actChat.getDest(), token_to_send, this.actChat.getFrame());
					
						System.out.println("[MsgListenerThread] Token "+token_to_send+" inviato");
						
						System.out.println("[MsgListenerThread] Derivo chiave...");

						/*if(aesKeystore.setTokenShared(first_token)) {
							mh.startUpdates(this.actChat.getDest());
							this.actChat.getFrame().setVisible(true);
						};*/
						
					}else if (countMsg == 2 && c_type == STARTER) {
												
						System.out.println("[MsgListenerThread] Ricevuto Secondo token:"+msg.getMsg());
						
						first_token = msg.getMsg();
						
						System.out.println("[MsgListenerThread] Derivo chiave..");

						/*if(aesKeystore.setTokenShared(first_token)) {
							mh.startUpdates(this.actChat.getDest());
						};*/
						
					}else{
						
						if(c_type == NO_STARTER) {
							mh.startUpdates(this.actChat.getDest());	
							this.actChat.getFrame().setVisible(true);
						}

						//Scrivo il contenuto del messaggio sulla pipe che ï¿½ connessa all'updater thread, cosi
						//che possa mostrarli sul chatBox
						dos.writeUTF(msg.getMsg());
		
						//Risveglio l'updater thread, che era in attesa sul semaforo
						this.actChat.getChatSem().release();
					}
		
				}else{
					
					//Se ricevo un msg speciale, vuol dire che l'interlocutore ha chiuso la chat
					//Quindi devo chiuderla anche io
					System.out.println("[MsgListenerThread] Msg finale, chiudo!");
			
					//CloseChat lo chiamo con nullSend = false, perchï¿½ come detto
					//effettuo una chiusura 'passiva', e non c'ï¿½ bisogno che invio
					//un messaggio = null all'interlocutore per notificargli la chiusura della chat
					mh.closeChat(this.actChat.getDest(), false);
			
					//interrompo gli updates sul chatbox
					mh.stopUpdates(this.actChat.getDest());
			
					break;
			
				}
		
		}
		
	}catch(SocketException e) {
			
		//Quando il chatframe viene chiuso, automaticamente andiamo a chiudere anche la socket
		//su cui il thread sta facendo readObject: di conseguenza verrï¿½ lanciata una SocketException,
		//che farï¿½ uscire dal while(true) il thread
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
