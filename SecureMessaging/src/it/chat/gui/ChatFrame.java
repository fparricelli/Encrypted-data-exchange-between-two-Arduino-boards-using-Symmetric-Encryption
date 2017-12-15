package it.chat.gui;


import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import it.chat.gui.utility.LookAndFeelUtility;
import it.chat.helpers.MessagingHelper;
import it.chat.threads.MessageListenerThread;
import it.sm.keystore.aeskeystore.AESHardwareKeystore;
import it.sm.keystore.aeskeystore.MyAESKeystore;
import it.sm.messages.Messaggio;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class ChatFrame {

	private JFrame frame;
	private JTextField messageBox;
	
	private JTextPane chatBox;
	
	private JPanel topPanel;
	private JButton btnInvia;
	private JButton btnChiudi;
	private JLabel chatWith;
	
	private String currentIdentity;
	private String chattingTarget;
	private int destPort;
	
	private MyAESKeystore aesKeyStore;
	private boolean handshake_res;
	private static final int STARTER = 1; //type = 1 starter

	private int client_type; //type = 1 starter

	//Main di test, da usare per lanciare autonomamente il frame
	/*
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					//testing
					ChatFrame window = new ChatFrame("ME","Me",400);
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	*/


	//Costruttore: prende il current user, il soggetto con cui chattare, e il numero di porta del soggetto da contattare
	public ChatFrame(String cu, String c, int p, int type) {
		this.currentIdentity = cu;
		this.chattingTarget= c;
		this.destPort = p;
		this.client_type = type;
		aesKeyStore = new AESHardwareKeystore(client_type);
		initialize();
	}

	
	private void initialize() {
		
		LookAndFeelUtility.setLookAndFeel(LookAndFeelUtility.GRAPHITE);
		initializeFrame();
		initializeTopPanel();
		initializeChatBox();
		initializeButtons();
		
		if(client_type == STARTER) {
			handshake();
		}
	}
	
	//Inizializza il frame.
	private void initializeFrame() {
		
		frame = new JFrame();
		frame.setTitle("Chat");
		frame.setBounds(100, 100, 451, 312);
		frame.getContentPane().setLayout(null);
		frame.setResizable(false);
		frame.getContentPane().setBackground(Color.DARK_GRAY);
		
		
		frame.addWindowListener(new WindowAdapter() {
		      
			public void windowClosing(WindowEvent e) {
				
				//Chiudo il frame
				frame.dispose();
				
				//Quando chiudo la chat..
				MessagingHelper mh = MessagingHelper.getInstance();
				//Chiamo closeChat per bloccare la ricezione di messaggi e rilasciare tutte le risorse
				//NOTA: il parametro true indica che ho chiuso io la chat, e quindi devo occuparmi di
				//notificare l'altro interlocutore (vedi dettagli metodo closeChat)
				mh.closeChat(getDestPort(),true);
				//Chiamo stopUpdates per terminare l'apparizione dei messaggi sul chatBox
				mh.stopUpdates(getDestPort());
				
				
				
		      }
		});

	}
	
	
	
	//Inizializza il pannello superiore.
	private void initializeTopPanel() {
		
		topPanel = new JPanel();
		topPanel.setBounds(10, 11, 414, 26);
		frame.getContentPane().add(topPanel);
		topPanel.setLayout(null);
		
		chatWith = new JLabel("Chatti con: "+this.chattingTarget);
		chatWith.setHorizontalAlignment(SwingConstants.CENTER);
		chatWith.setBounds(0, 0, 414, 25);
		topPanel.add(chatWith);
	}
	
	
	//Inizializza i bottoni inferiori.
	private void initializeButtons() {
		
		btnInvia = new JButton("Invia");
		
		btnInvia.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
			//Se non ho inserito alcun messaggio, mostro un dialog
			if(messageBox.getText().length() == 0) {
				
				JOptionPane.showMessageDialog(frame.getContentPane(), "Inserisci un messaggio da inviare!","Errore!",JOptionPane.ERROR_MESSAGE);
			
		    //Altrimenti procedo..
			}else{
				
				MessagingHelper mh = MessagingHelper.getInstance();
				
				//Invio il messaggio chiamando il metodo sendMessage
				boolean res = mh.sendMessage(currentIdentity,getDestPort(), messageBox.getText(), getChatFrame());	
				
				//Se l'invio va a buon fine, mostro il messaggio all'interno del chat box
				if(res) {
					chatBox.setText(chatBox.getText().concat("[Me] "+messageBox.getText()+"\n"));
					messageBox.setText("");
				
				//Altrimenti mostro un dialog di errore, nel caso in cui sia impossibile contattare l'utente specificato (ad es: non � online)
				}else {
	    			JOptionPane.showMessageDialog(frame.getContentPane(), "Errore: impossibile contattare l'utente specificato, riprova!","Errore!",JOptionPane.ERROR_MESSAGE);
	    			mh.closeChat(getDestPort(),true);
	    			frame.dispose();
				}
					
					
				}
				
			}
			
			
		});
		
		
		btnInvia.setBounds(335, 228, 89, 24);
		frame.getContentPane().add(btnInvia);
		frame.getRootPane().setDefaultButton(btnInvia);
		btnInvia.requestFocus();

		
		btnChiudi = new JButton("Chiudi");
		btnChiudi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				
				//Quando clicco su chiudi, l'effetto � lo stesso di quello definito per la chiusura della finestra
				
				frame.dispose();
				
				MessagingHelper mh = MessagingHelper.getInstance();
				mh.closeChat(getDestPort(),true);
				mh.stopUpdates(getDestPort());
				
				
			}
		});
		btnChiudi.setBounds(236, 228, 89, 24);
		frame.getContentPane().add(btnChiudi);
		
		
		
	}
	
	//Inizializza il chatBox centrale.
	private void initializeChatBox() {
		
		chatBox = new JTextPane();
		chatBox.setBounds(10, 48, 414, 138);
		chatBox.setEditable(false);
		frame.getContentPane().add(chatBox);
		
		JScrollPane scrollPane = new JScrollPane(chatBox);
		scrollPane.setBounds(10, 48, 414, 138);
		frame.getContentPane().add(scrollPane);
		
		messageBox = new JTextField();
		messageBox.setBounds(10, 197, 414, 20);
		frame.getContentPane().add(messageBox);
		messageBox.setColumns(10);
	}
	
	
	
	
	/**
	 * Funzione che si occupa di effettuare l'handshke per lo scambio della chiave 
	 * */
	
	private void handshake() {
		
		
		MessagingHelper mh = MessagingHelper.getInstance();
		
		String token_to_send = "PO";//aesKeyStore.requireTokenToShare();
		
		System.out.println("[ChatFrame - STARTER] Avvio Handshake con token:"+token_to_send);
		
		mh.sendMessage(currentIdentity,getDestPort(), token_to_send, getChatFrame());
		
		
	}
		
	
	
	private int getDestPort() {
		return this.destPort;
	}
	
	public void setVisible(boolean f) {
		this.frame.setVisible(true);
	}
	
		public void handshakeError() {
			JOptionPane.showMessageDialog(frame.getContentPane(), "Errore nella fase di handshake!","Attenzione!",JOptionPane.INFORMATION_MESSAGE);
			this.frame.dispose();
		}
	
	//Metodo utilizzato quando l'altro interlocutore interrompe la comunicazione.
	public void interruptCommunication() {
		JOptionPane.showMessageDialog(frame.getContentPane(), "Comunicazione interrotta dall'interlocutore!","Attenzione!",JOptionPane.INFORMATION_MESSAGE);
		this.frame.dispose();
	}
	
	private ChatFrame getChatFrame() {
		return this;
	}
	
	public boolean isVisible() {
		return this.frame.isVisible();
	}
	
	public JTextPane getChatBox() {
		return this.chatBox;
	}
	
}
