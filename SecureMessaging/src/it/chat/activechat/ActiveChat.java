package it.chat.activechat;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import javax.net.ssl.SSLSocket;

import it.chat.gui.ChatFrame;
import it.chat.threads.UpdaterThread;

public class ActiveChat {

	/* Classe che mantiene tutte le informazioni di stato associate
	 * alla chat attiva.
	 * 
	 */
	
	//Indica il nome dell'utente locale.
	private String currentIdentity;
	
	//Indica il 'numero di telefono' dell'interlocutore con cui sto parlando.
	private int dest;
	
	//Socket di comunicazione, con gli stream associati.
	private SSLSocket s;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	
	//ChatFrame associato alla chat attiva
	private ChatFrame activeFrame;
	
	//Risorse usate dall'updater thread.
	private Semaphore sem;
	private PipedInputStream updatesPipe;
	private UpdaterThread ut;
	
	public ActiveChat(int d, SSLSocket so) {
		
		this.dest = d;
		this.s = so;
		
		this.sem = new Semaphore(0);
		this.updatesPipe = new PipedInputStream();
	}
	

	public int getDest() {
		return dest;
	}

	public void setDest(int dest) {
		this.dest = dest;
	}

	public SSLSocket getSocket() {
		return s;
	}

	public void setSocket(SSLSocket s) {
		this.s = s;
	}
	
	public ObjectOutputStream getOOS() {
		return this.oos;
	}
	
	public void setOOS(ObjectOutputStream oos) {
		this.oos = oos;
	}
	
	public void setFrame(ChatFrame f) {
		this.activeFrame = f;
	}
	
	public ChatFrame getFrame() {
		return this.activeFrame;
	}
	
	public Semaphore getChatSem() {
		return this.sem;
	}
	
	public PipedInputStream getUpdatesPipe() {
		return this.updatesPipe;
	}
	
	public UpdaterThread getUpdaterThread() {
		return this.ut;
	}
	
	public void setUpdaterThread(UpdaterThread ut) {
		this.ut = ut;
	}
	
	public void setOIS(ObjectInputStream ois) {
		this.ois = ois;
	}
	
	public ObjectInputStream getOIS() {
		return this.ois;
	}
	
	public String getCurrentIdentity() {
		return this.currentIdentity;
	}
	
	public void setCurrentIdentity(String c) {
		this.currentIdentity = c;
	}

}
