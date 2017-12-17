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
import it.chat.gui.utility.MessageStringUtility;
import it.chat.helpers.MessagingHelper;
import it.chat.threads.MessageListenerThread;
import it.sm.exception.OutOfBoundEncrypt;
import it.sm.keystore.aeskeystore.AESHardwareKeystore;
import it.sm.keystore.aeskeystore.MyAESKeystore;
import it.sm.messages.EncryptedMessage;
import it.sm.messages.Messaggio;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.SystemColor;
import javax.swing.ImageIcon;
import javax.swing.JSeparator;


public class ChatFrame {

	private JFrame frame;
	private JTextField messageBox;
	
	private JTextPane chatBox;
	
	private JPanel topPanel;
	private JButton btnInvia;
	
	private SwingProgressBar bar;


	private JButton btnChiudi;
	private JLabel chatWith;
	
	private String currentIdentity;
	private String chattingTarget;
	private int destPort;
	
	private MyAESKeystore aesKeyStore;
	private boolean handshake_res;
	private static final int STARTER = 1; //type = 1 starter

	private int client_type; //type = 1 starter
	
	JFrame progressFrame;
	private JLabel lblNewLabel;
	private JSeparator separator;
	
	
	
	
	

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
		aesKeyStore = AESHardwareKeystore.getInstance(client_type);
		bar = new SwingProgressBar();
		progressFrame = new JFrame(MessageStringUtility.PROGRESS_BAR_MSG);
		initialize();
	}

	
	private void initialize() {
		initializeLoadingBar();

		progressFrame.setVisible(true);
		if(!System.getProperty("os.name").toLowerCase().contains("mac")) 
			LookAndFeelUtility.setLookAndFeel(LookAndFeelUtility.GRAPHITE);
		initializeFrame();
		initializeTopPanel();
		initializeChatBox();
		initializeButtons();

		
		if(client_type == STARTER) {
			btnInvia.setEnabled(false);
						
			handshake();
		}else progressFrame.dispose();
	}
	
	private void initializeLoadingBar() {
		progressFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		progressFrame.setBounds(200, 200, 1000, 312);
		progressFrame.setResizable(false);
		progressFrame.setContentPane(bar);
		progressFrame.pack();
	}


	//Inizializza il frame.
	private void initializeFrame() {
		
		frame = new JFrame();
		frame.setTitle("Chat");
		frame.setBounds(100, 100, 451, 320);
		frame.getContentPane().setLayout(null);
		frame.setResizable(false);
		frame.getContentPane().setBackground(new Color(36, 47, 65));
		
		
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
		topPanel.setBounds(0, 0, 474, 48);
		frame.getContentPane().add(topPanel);
		topPanel.setLayout(null);
		topPanel.setBackground(new Color(97, 212, 195));
		
		lblNewLabel = new JLabel("");
		lblNewLabel.setBounds(174, 0, 469, 230);
		topPanel.add(lblNewLabel);
		lblNewLabel.setIcon(new ImageIcon(ChatFrame.class.getResource("/it/chat/gui/icons/icon_app.png")));
		
		chatWith = new JLabel(this.chattingTarget);
		chatWith.setForeground(SystemColor.text);
		chatWith.setFont(new Font("AppleGothic", Font.BOLD, 15));
		chatWith.setHorizontalAlignment(SwingConstants.CENTER);
		chatWith.setBounds(6, 6, 437, 37);
		topPanel.add(chatWith);
	}
	
	
	//Inizializza i bottoni inferiori.
	private void initializeButtons() {
		
		btnInvia = new JButton("Send Message");
		btnInvia.setFont(new Font("AppleGothic", Font.PLAIN, 13));
				
		btnInvia.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
			//Se non ho inserito alcun messaggio, mostro un dialog
			if(messageBox.getText().length() == 0) {
				
				JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.EMPTY_MESSAGE,MessageStringUtility.WARNING,JOptionPane.WARNING_MESSAGE);
			
		    //Altrimenti procedo..
			}else{
				
				MessagingHelper mh = MessagingHelper.getInstance();
				
				try {
				
				EncryptedMessage encryptedMessage = aesKeyStore.encrypt(messageBox.getText());
				
				//Invio il messaggio chiamando il metodo sendMessage
				
				boolean res = mh.sendChatMessage(currentIdentity,getDestPort(), encryptedMessage.getEncryptedMessage(), encryptedMessage.getMsg_key(),getChatFrame());	
				
				//Se l'invio va a buon fine, mostro il messaggio all'interno del chat box
				if(res) {
					chatBox.setText(chatBox.getText().concat("[Me] "+messageBox.getText()+"\n"));
					messageBox.setText("");
				
				//Altrimenti mostro un dialog di errore, nel caso in cui sia impossibile contattare l'utente specificato (ad es: non � online)
				}else {
	    			JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.UNABLE_CONTACTING,MessageStringUtility.ERROR,JOptionPane.ERROR_MESSAGE);
	    			mh.closeChat(getDestPort(),true);
	    			frame.dispose();
				}
				}catch(OutOfBoundEncrypt ec){
	    			JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.MESSAGE_LENGTH_ERR,MessageStringUtility.ERROR,JOptionPane.ERROR_MESSAGE);
				}
					
				}
			
				
				
			}
			
			
		});
		
		
		btnInvia.setBounds(313, 266, 132, 24);
		frame.getContentPane().add(btnInvia);
		frame.getRootPane().setDefaultButton(btnInvia);
		btnInvia.requestFocus();

		
		btnChiudi = new JButton("Close Chat");
		btnChiudi.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		btnChiudi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				
				//Quando clicco su chiudi, l'effetto � lo stesso di quello definito per la chiusura della finestra
				
				frame.dispose();
				
				MessagingHelper mh = MessagingHelper.getInstance();
				mh.closeChat(getDestPort(),true);
				mh.stopUpdates(getDestPort());
				
				
			}
		});
		btnChiudi.setBounds(6, 266, 112, 24);
		frame.getContentPane().add(btnChiudi);
		
		separator = new JSeparator();
		separator.setBounds(10, 252, 437, 12);
		frame.getContentPane().add(separator);
		
		
		
	}
	
	//Inizializza il chatBox centrale.
	private void initializeChatBox() {
		
		chatBox = new JTextPane();
		chatBox.setForeground(SystemColor.text);
		chatBox.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		chatBox.setBounds(10, 48, 414, 138);
		chatBox.setEditable(false);
		chatBox.setBackground(new Color(36, 47, 65));
		frame.getContentPane().add(chatBox);
		
		JScrollPane scrollPane = new JScrollPane(chatBox);
		scrollPane.setBounds(0, 48, 451, 182);
		frame.getContentPane().add(scrollPane);
		
		messageBox = new JTextField();
		messageBox.setForeground(new Color(255, 255, 255));
		messageBox.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		messageBox.setBounds(10, 234, 435, 20);
		frame.getContentPane().add(messageBox);
		messageBox.setColumns(10);
		messageBox.setBackground(new Color(36, 47, 65));
	}
	
	
	
	
	/**
	 * Funzione che si occupa di effettuare l'handshke per lo scambio della chiave 
	 * */
	
	private void handshake() {
		
		bar.updateBar(15);
		
		MessagingHelper mh = MessagingHelper.getInstance();
				
		String token_to_send = aesKeyStore.requireTokenToShare(client_type);
		
		System.out.println("[ChatFrame - STARTER] Avvio Handshake con token:"+token_to_send);
		
		bar.updateBar(30);
		
		mh.sendMessage(currentIdentity,getDestPort(), token_to_send, getChatFrame());
		
		this.progressFrame.dispose();
	}
		
	
	
	private int getDestPort() {
		return this.destPort;
	}
	
	public void setVisible(boolean f) {
		this.frame.setVisible(true);
	}
	
		public void handshakeError() {
			JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.HANDSHAKE_ERR,MessageStringUtility.ERROR,JOptionPane.ERROR_MESSAGE);
			this.frame.dispose();
		}
	
	//Metodo utilizzato quando l'altro interlocutore interrompe la comunicazione.
	public void interruptCommunication() {
		JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.COMM_INTERR,MessageStringUtility.WARNING,JOptionPane.WARNING_MESSAGE);
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
	
	public JButton getBtnInvia() {
		return btnInvia;
	}


	public void setBtnInvia(JButton btnInvia) {
		this.btnInvia = btnInvia;
	}
	
}
