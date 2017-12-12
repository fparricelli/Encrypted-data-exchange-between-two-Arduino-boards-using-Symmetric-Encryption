package it.chat.gui;


import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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

import it.chat.helpers.MessagingHelper;

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
	
	private String currentUser;
	private String chatter;
	private int destPort;

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
	public ChatFrame(String cu, String c, int p) {
		this.currentUser = cu;
		this.chatter = c;
		this.destPort = p;
		initialize();
	}

	
	private void initialize() {
		
		setLookAndFeel();
		initializeFrame();
		initializeTopPanel();
		initializeChatBox();
		initializeButtons();
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
		
		chatWith = new JLabel("Chatti con: "+this.chatter);
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
				boolean res = mh.sendMessage(getCurrentUser(),getDestPort(), messageBox.getText(), getChatFrame());	
				
				//Se l'invio va a buon fine, mostro il messaggio all'interno del chat box
				if(res) {
					chatBox.setText(chatBox.getText().concat("[Me] "+messageBox.getText()+"\n"));
					messageBox.setText("");
				
				//Altrimenti mostro un dialog di errore, nel caso in cui sia impossibile contattare l'utente specificato (ad es: non è online)
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
				
				
				//Quando clicco su chiudi, l'effetto è lo stesso di quello definito per la chiusura della finestra
				
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
	
	
	//Inizializza il lookAndFeel del frame.
	private void setLookAndFeel() {
		
		try {
			
			UIManager.setLookAndFeel("com.jtattoo.plaf.graphite.GraphiteLookAndFeel");
			
			}catch(Exception e) {
				e.printStackTrace();
			}
	}
	
	
	private int getDestPort() {
		return this.destPort;
	}
	
	public void setVisible(boolean f) {
		this.frame.setVisible(true);
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
	
	private String getCurrentUser() {
		return this.currentUser;
	}
}
