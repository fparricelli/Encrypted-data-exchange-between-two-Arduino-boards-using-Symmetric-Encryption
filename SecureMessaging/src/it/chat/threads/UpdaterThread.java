package it.chat.threads;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.util.concurrent.Semaphore;

import javax.swing.JTextPane;

public class UpdaterThread extends Thread{
	
	private JTextPane j;
	private DataInputStream dis;
	private Semaphore sem;
	
	/* Thread che si occupa di ricevere da una pipe i messaggi che devono essere visualizzati
	 * sul chatbox della chat attualmente attiva.
	 * Prende in ingresso il textPane su cui scrivere i messaggi, la pipe input da cui prendere i messaggi,
	 * e il semaforo su cui aspettare l'arrivo dei messaggi (che saranno buttati sulla pipe dal message listener thread).
	 */
	
	
	public UpdaterThread(JTextPane j, PipedInputStream pis, Semaphore sem) {
		this.sem = sem;
		this.j = j;
		this.dis = new DataInputStream(pis);		
	}
	
	public void run() {
		
		
		try {
		
			//Ciclo..
			while(true) {
				
				//Mi metto in attesa di acquire()..
				sem.acquire();
				//Quando esco dall'acquire, vuol dire che c'� uno (o pi�) messaggi sulla pipe
				String msg = dis.readUTF();
				//Visualizzo tali messaggi sul textpane
				j.setText(j.getText().concat("[Ricevuto]: "+msg+"\n"));
				
			}	
				
				
		} catch (InterruptedException e) {
			
			//Quando chiudiamo la chat, viene chiamato il metodo stopUpdates
			//Che si occupa di chiamare una interrupt() su questo thread,
			//sollevando una interrupted exception che viene gestita normalmente.
			System.out.println("[UpdaterThread]Interrotto!");
			
			//Chiudo le risorse
			try {
				dis.close();
			}catch(Exception ex) {
				ex.printStackTrace();
			}
			
		//Tutte le altre eccezioni le gestisco normalmente
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
			
			
			
		
	}

}
