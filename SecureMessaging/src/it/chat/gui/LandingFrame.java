package it.chat.gui;
import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import it.chat.gui.utility.MessageStringUtility;
import it.chat.helpers.CertificateHelper;
import it.chat.helpers.MessagingHelper;
import it.chat.helpers.ServerHelper;
import it.chat.user.AuthUser;
import it.sm.exception.AccessDeniedException;
import it.sm.exception.PolicyConflictException;
import it.sm.exception.RedirectToLoginException;
import it.sm.exception.ServerErrorException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import java.awt.Font;
import javax.swing.JSeparator;
public class LandingFrame {

	private JFrame frame;
	private JPanel welcomePanel;
	private JPanel adminPanel;
	private JPanel userPanel;
	
	private JLabel lblWelcome;
	
	
	private JButton btnAdmin;
	private JButton btnTecnico;
	private JButton btnUser;
	private JButton btnLogout;
	
	
	private String currentNome;
	private String currentCognome;
	private String currentRole;
	private int currentNumber;
	
	private AuthUser currentUser;
	
	int client_type;
	private JLabel lblSecureMessaging;
	private JLabel labelRole;
	
	/* Frame 'home', dalla quale l'utente corrente pu� contattare
	 * gli altri utenti secondo i permessi accordati.
	 * 
	 * Il main prende in ingresso (args) tre parametri: identit�, ruolo (utente, tecnico, admin) e porta di ascolto
	 * (dev'essere scelta in accordo con le liste contatti in formato xml presenti sul server)
	 * ad esempio, apri due terminali e lanci nel primo:
	 * java -classpath (...) it.chat.gui.LandingFrame Donald Duck utente 210
	 * 
	 * e nel secondo:
	 * java -classpath (...) it.chat.gui.LandingFrame Bob Aggiustatutto tecnico 205
	 */
	
	
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				
				try {
					System.out.println("Sei "+args[0]+" "+args[1]+",hai scelto ruolo:"+args[2]+ " e porta:"+args[3]);
					LandingFrame window = new LandingFrame(args[0],args[1], args[2],Integer.valueOf(args[3]));
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	
	public LandingFrame(String nome,String cognome,String role, int num) {
		this.currentNome = nome;
		this.currentCognome = cognome;
		this.currentRole = role;
		this.currentNumber = num;
		initialize();
	}
	
	public LandingFrame(AuthUser u) {
		this.currentUser = u;
		this.currentNome = u.getName();
		this.currentCognome = u.getSurname();
		this.currentRole = u.getRole();
		this.currentNumber = u.getTelephone();
		System.out.println("Landing frame, token:"+this.currentUser.getToken());
		initialize();
	}

	
	private void initialize() {
		initializeStores();
		if(!System.getProperty("os.name").toLowerCase().contains("mac"))
			setLookAndFeel();
		initializeFrame();
		initializeWelcomePanel();
		initializeAdminPanel();
		initializeTecnicoPanel();
		initializeUserPanel();
		initializeMessageListening();
		
	}
	
	private void initializeWelcomePanel() {
		
		userPanel = new JPanel();
		userPanel.setForeground(UIManager.getColor("Button.background"));
		userPanel.setBackground(new Color(97, 212, 195));
		userPanel.setBounds(0, 0, 238, 276);
		frame.getContentPane().add(userPanel);
		userPanel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(new ImageIcon(LandingFrame.class.getResource("/it/chat/gui/icons/user_red.png")));
		lblNewLabel.setBounds(132, -119, 100, 480);
		userPanel.add(lblNewLabel);

		
		welcomePanel = new JPanel();
		welcomePanel.setBackground(new Color(36, 47, 65));
		welcomePanel.setBounds(238, 0, 212, 83);
		frame.getContentPane().add(welcomePanel);
		welcomePanel.setLayout(null);
		
		lblSecureMessaging = new JLabel("Secure Messaging");
		lblSecureMessaging.setFont(new Font("AppleGothic", Font.PLAIN, 15));
		lblSecureMessaging.setForeground(UIManager.getColor("OptionPane.background"));
		lblSecureMessaging.setBounds(40, 37, 155, 35);
		welcomePanel.add(lblSecureMessaging);
		
		
	}
	
	//Inizializza l'intero frame.
	private void initializeFrame() {
		
		frame = new JFrame();
		frame.setTitle(MessageStringUtility.LAND_TITLE);
		frame.setBounds(100, 100, 450, 395);
		frame.getContentPane().setLayout(null);
		frame.getContentPane().setBackground(new Color(72,166,152));
		frame.setResizable(false);
		
		
		frame.addWindowListener(new WindowAdapter() {
		      
			public void windowClosing(WindowEvent e) {
		        
				//Quando chiudo la finestra, termino l'ascolto..
				MessagingHelper mh = MessagingHelper.getInstance();
				mh.stopListening();
				
				//...e chiudo l'applicazione
				System.exit(0);
		      }
		});

	}
	
	//Inizializza il contenuto del pannello Admins.
	private void initializeAdminPanel() {
		
		adminPanel = new JPanel();
		adminPanel.setForeground(UIManager.getColor("Button.background"));
		adminPanel.setBackground(new Color(36,47,65));
		adminPanel.setBounds(238, 43, 212, 330);
		frame.getContentPane().add(adminPanel);
		adminPanel.setLayout(null);
		
		btnAdmin = new JButton("Contact Administrator");
		btnAdmin.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		
		btnAdmin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				//Prima controllo se l'utente ha chat gi� aperte
				MessagingHelper mh = MessagingHelper.getInstance();
				boolean hac = mh.hasActiveChats();
				
				//Se ha chat aperte, mostro un dialog di avviso
				if(hac) {
					JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.CLOSE_ACT_CHATS, MessageStringUtility.INFO, JOptionPane.INFORMATION_MESSAGE);
				}else {
				
				//Altrimenti gli consento di procedere..
				//..richiedendo la lista contatti admin, che verr� (o meno) recuperata a seconda dei permessi
				//previsti per l'utente corrente.
					try {
						
						ServerHelper sh = new ServerHelper();
						File cList = sh.getContactList("admins", currentRole,currentUser.getToken(),currentUser);
						System.out.println("[Landing frame] Token ricevuto:"+currentUser.getToken());
						
						ContactFrame cf = new ContactFrame(currentNome,currentCognome,"Admins",cList);
						cf.setVisible(true);
					
					}catch(AccessDeniedException e) {
						
						System.out.println(e.getMessage());
						JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.ACC_DENIED, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);
					
					}catch(PolicyConflictException e1) {
						
						System.out.println(e1.getMessage());
						JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.POL_ERR, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);
					
					}catch(RedirectToLoginException e2) {
						
						System.out.println(e2.getMessage());
						
						JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.CONT_LIST_ERR, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);
						btnLogout.doClick();
						LoginFrame lf = new LoginFrame();
						lf.setVisible(true);
					
					}catch(ServerErrorException e3) {
						System.out.println(e3.getMessage());
						JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.COMM_ERR, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);

					}
					
					
					
				}
			}
		});
		
		btnAdmin.setBounds(6, 143, 188, 24);
		adminPanel.add(btnAdmin);
		
		btnTecnico = new JButton("Contact Technical");
		btnTecnico.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		btnTecnico.setBounds(6, 222, 188, 23);
		adminPanel.add(btnTecnico);
		
		btnUser = new JButton("Contact User");
		btnUser.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		btnUser.setBounds(6, 74, 188, 23);
		adminPanel.add(btnUser);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(51, 105, 161, 12);
		adminPanel.add(separator);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(51, 179, 161, 12);
		adminPanel.add(separator_1);
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setBounds(62, 257, 161, 12);
		adminPanel.add(separator_2);
		
		btnLogout = new JButton("Logout");
		btnLogout.setBounds(113, 296, 89, 23);
		adminPanel.add(btnLogout);
		btnLogout.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		
		lblWelcome = new JLabel("Welcome " +currentNome+" "+currentCognome);
		lblWelcome.setBounds(10, 278, 226, 43);
		frame.getContentPane().add(lblWelcome);
		lblWelcome.setFont(new Font("AppleGothic", Font.BOLD, 13));
		lblWelcome.setBackground(UIManager.getColor("window"));
		lblWelcome.setForeground(UIManager.getColor("MenuItem.selectionForeground"));
		lblWelcome.setHorizontalAlignment(SwingConstants.LEFT);
		
		labelRole = new JLabel("Role: "+this.currentRole);
		labelRole.setBounds(10, 323, 218, 32);
		frame.getContentPane().add(labelRole);
		labelRole.setFont(new Font("AppleGothic", Font.BOLD, 13));
		labelRole.setBackground(UIManager.getColor("window"));
		labelRole.setForeground(UIManager.getColor("MenuItem.selectionForeground"));
		labelRole.setHorizontalAlignment(SwingConstants.LEFT);
		
		
		
		btnLogout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MessagingHelper mh = MessagingHelper.getInstance();
				mh.stopListening();
				
				frame.dispose();
			}
		});
		
		btnUser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				//Controllo se l'utente ha chat aperte
				MessagingHelper mh = MessagingHelper.getInstance();
				boolean hac = mh.hasActiveChats();
				
				//Se ne ha, mostro un dialog di avviso
				if(hac) {
					JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.CLOSE_ACT_CHATS, MessageStringUtility.INFO, JOptionPane.INFORMATION_MESSAGE);
				}else {
				
					//Altrimenti gli consento di procedere..
					//..richiedendo la lista contatti utenti, che verr� (o meno) recuperata a seconda dei permessi
					//previsti per l'utente corrente.
					try {
						
						ServerHelper sh = new ServerHelper();
						File cList = sh.getContactList("utenti", currentRole,currentUser.getToken(),currentUser);
						System.out.println("[Landing Frame] Token ricevuto:"+currentUser.getToken());
						
						ContactFrame cf = new ContactFrame(currentNome,currentCognome,"Utenti",cList);
						cf.setVisible(true);
					
					}catch(AccessDeniedException e) {
						
						System.out.println(e.getMessage());
						JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.ACC_DENIED, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);
					
					}catch(PolicyConflictException e1) {
						
						System.out.println(e1.getMessage());
						JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.POL_ERR, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);
					
					}catch(RedirectToLoginException e2) {
						
						System.out.println(e2.getMessage());
						
						JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.CONT_LIST_ERR, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);
						btnLogout.doClick();
						LoginFrame lf = new LoginFrame();
						lf.setVisible(true);
					
					}catch(ServerErrorException e3) {
						System.out.println(e3.getMessage());
						JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.COMM_ERR, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);

					}
					
				}
			}
		});
		
		btnTecnico.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				//Controllo se l'utente ha chat aperte
				MessagingHelper mh = MessagingHelper.getInstance();
				boolean hac = mh.hasActiveChats();
				
				//Se ne ha, mostro un dialog di avviso
				if(hac) {
					JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.CLOSE_ACT_CHATS, MessageStringUtility.INFO, JOptionPane.INFORMATION_MESSAGE);
				}else {
					
					//Altrimenti gli consento di procedere..
					//..richiedendo la lista contatti tecnici, che verr� (o meno) recuperata a seconda dei permessi
					//previsti per l'utente corrente.
					try {
						
						ServerHelper sh = new ServerHelper();
						
						
						File cList = sh.getContactList("tecnici", currentRole,currentUser.getToken(),currentUser);
						System.out.println("[Landing Frame] Token ricevuto:"+currentUser.getToken());
						
						ContactFrame cf = new ContactFrame(currentNome,currentCognome,"Tecnici",cList);
						cf.setVisible(true);
					
					}catch(AccessDeniedException e1) {
						
						System.out.println(e1.getMessage());
						JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.ACC_DENIED, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);
					
					}catch(PolicyConflictException e2) {
						
						System.out.println(e2.getMessage());
						JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.POL_ERR, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);
					
					}catch(RedirectToLoginException e2) {
						
						System.out.println(e2.getMessage());
						
						JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.CONT_LIST_ERR, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);
						btnLogout.doClick();
						LoginFrame lf = new LoginFrame();
						lf.setVisible(true);
					
					}catch(ServerErrorException e3) {
						System.out.println(e3.getMessage());
						JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.COMM_ERR, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);

					}
					
					
				}
				
			}
		});
	}
	
	//Inizializza i contenuti del pannello tecnici.
	private void initializeTecnicoPanel() {
	}
	
	
	//Inizializza i contenuti del pannello utente.
	private void initializeUserPanel() {
	}
	
	
	
	//Metodo che si occupa di settare il look and feel del frame.
	private void setLookAndFeel() {
		
		try {
			
			UIManager.setLookAndFeel("com.jtattoo.plaf.graphite.GraphiteLookAndFeel");
			
			}catch(Exception e) {
				e.printStackTrace();
			}
	}
	
	
	//Inizializza l'ascolto sulla porta specificata.
	//In questo modo, all'apertura del Landing Frame l'utente si mette in ascolto di richieste di comunicazione
	//Sulla porta specificata all'avvio del main
	//(Nello scenario reale, la porta di ascolto corrisponder� al "numero di telefono" del soggetto, e verr� settata
	//All'atto dell'autenticazione dell'utente (recuperando tale info dal db)
	private void initializeMessageListening() {
		
		MessagingHelper mh = MessagingHelper.getInstance();
		String currIdentity = this.currentNome+" "+this.currentCognome;
		boolean res = mh.startListening(this.currentNumber, currIdentity);
		
		//Se l'inizializzazione dell'ascolto non va a buon fine (ad es: porta gi� occupata) allora mostro dialog
		if(!res) {
			JOptionPane.showMessageDialog(frame.getContentPane(), "Errore in ascolto messaggi, riprovare.","Errore!",JOptionPane.ERROR_MESSAGE);
		}
	
	}
	
	
	
	private void initializeStores() {
		CertificateHelper ch = CertificateHelper.getInstance();
		ch.init(currentNome);
	}
	
	
	public void setVisible(boolean b) {
		this.frame.setVisible(b);
	}
}
