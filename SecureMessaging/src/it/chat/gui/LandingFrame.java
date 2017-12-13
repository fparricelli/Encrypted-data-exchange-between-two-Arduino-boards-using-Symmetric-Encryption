package it.chat.gui;
import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import it.chat.helpers.MessagingHelper;
import it.chat.helpers.ServerHelper;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.awt.event.ActionEvent;
public class LandingFrame {

	private JFrame frame;
	private JPanel welcomePanel;
	private JPanel adminPanel;
	private JPanel tecnicoPanel;
	private JPanel userPanel;
	
	private JLabel lblWelcome;
	private JLabel lblAdmin;
	private JLabel lblUser;
	private JLabel lblTecnico;
	
	
	private JButton btnAdmin;
	private JButton btnTecnico;
	private JButton btnUser;
	private JButton btnLogout;
	
	
	private String currentUser;
	private String currentRole;
	private int currentNumber;
	
	int client_type;
	
	/* Frame 'home', dalla quale l'utente corrente pu� contattare
	 * gli altri utenti secondo i permessi accordati.
	 * 
	 * Il main prende in ingresso (args) tre parametri: identit�, ruolo (utente, tecnico, admin) e porta di ascolto
	 * (dev'essere scelta in accordo con le liste contatti in formato xml presenti sul server)
	 * ad esempio, apri due terminali e lanci nel primo:
	 * java -classpath (...) it.chat.gui.LandingFrame Donald utente 210
	 * 
	 * e nel secondo:
	 * java -classpath (...) it.chat.gui.LandingFrame Bob tecnico 205
	 */
	
	
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				
				try {
					System.out.println("Sei "+args[0]+",hai scelto ruolo:"+args[1]+ " e porta:"+args[2]);
					LandingFrame window = new LandingFrame(args[0],args[1], Integer.valueOf(args[2]));
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	
	public LandingFrame(String nome,String role, int num) {
		this.currentUser = nome;
		this.currentRole = role;
		this.currentNumber = num;
		initialize();
	}

	
	private void initialize() {
		setLookAndFeel();
		initializeFrame();
		initializeWelcomePanel();
		initializeAdminPanel();
		initializeTecnicoPanel();
		initializeUserPanel();
		initializeMessageListening();
		
	}
	
	private void initializeWelcomePanel() {
		
		welcomePanel = new JPanel();
		welcomePanel.setBounds(10, 11, 414, 51);
		frame.getContentPane().add(welcomePanel);
		welcomePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		welcomePanel.setLayout(null);
		
		lblWelcome = new JLabel("Benvenuto, "+currentUser+" - ["+currentRole+"]");
		lblWelcome.setHorizontalAlignment(SwingConstants.LEFT);
		lblWelcome.setBounds(10, 11, 295, 29);
		welcomePanel.add(lblWelcome);
		
		btnLogout = new JButton("Logout");
		btnLogout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MessagingHelper mh = MessagingHelper.getInstance();
				mh.stopListening();
				
				frame.dispose();
			}
		});
		btnLogout.setBounds(315, 14, 89, 23);
		welcomePanel.add(btnLogout);
		
		
	}
	
	//Inizializza l'intero frame.
	private void initializeFrame() {
		
		frame = new JFrame();
		frame.setTitle("Help Desk");
		frame.setBounds(100, 100, 450, 395);
		frame.getContentPane().setLayout(null);
		frame.getContentPane().setBackground(Color.DARK_GRAY);
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
		adminPanel.setBounds(10, 73, 414, 85);
		frame.getContentPane().add(adminPanel);
		adminPanel.setLayout(null);
		
		lblAdmin = new JLabel("Clicca qui per parlare con un Amministratore");
		lblAdmin.setBounds(10, 11, 394, 24);
		adminPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		adminPanel.add(lblAdmin);
		
		btnAdmin = new JButton("Contatta Amministratore");
		
		btnAdmin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				//Prima controllo se l'utente ha chat gi� aperte
				MessagingHelper mh = MessagingHelper.getInstance();
				boolean hac = mh.hasActiveChats();
				
				//Se ha chat aperte, mostro un dialog di avviso
				if(hac) {
					JOptionPane.showMessageDialog(frame.getContentPane(), "Chiudi le chat attive prima di continuare.", "Info", JOptionPane.INFORMATION_MESSAGE);
				}else {
				
				//Altrimenti gli consento di procedere..
				//..richiedendo la lista contatti admin, che verr� (o meno) recuperata a seconda dei permessi
				//previsti per l'utente corrente.
					
					ServerHelper sh = new ServerHelper();
					int res = sh.getContactList("admins", currentRole);
					//Accesso consentito..
					if(res == 0) {
						ContactFrame cf = new ContactFrame(getCurrentUser(),"Admins", new File(sh.getContactListPath()+"/"+"admins-list.xml"));
						cf.setVisible(true);
					//Accesso negato..
					}else if(res == -1) {
						JOptionPane.showMessageDialog(frame.getContentPane(), "Accesso Negato!", "Errore", JOptionPane.ERROR_MESSAGE);
					//Errore di applicazione policy..
					}else if(res == -2) {
						JOptionPane.showMessageDialog(frame.getContentPane(), "Errore di applicazione permessi, riprova!", "Errore", JOptionPane.ERROR_MESSAGE);
					//Errore di comunicazione..
					}else{
						JOptionPane.showMessageDialog(frame.getContentPane(), "Errore di comunicazione!", "Errore", JOptionPane.ERROR_MESSAGE);
					}
					
					
					
				}
			}
		});
		
		btnAdmin.setBounds(10, 50, 207, 24);
		adminPanel.add(btnAdmin);
	}
	
	//Inizializza i contenuti del pannello tecnici.
	private void initializeTecnicoPanel() {
		
		tecnicoPanel = new JPanel();
		tecnicoPanel.setBounds(10, 169, 414, 85);
		frame.getContentPane().add(tecnicoPanel);
		tecnicoPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		tecnicoPanel.setLayout(null);
		
		lblTecnico = new JLabel("Clicca qui per parlare con un Tecnico");
		lblTecnico.setBounds(10, 11, 224, 14);
		tecnicoPanel.add(lblTecnico);
		
		btnTecnico = new JButton("Contatta Tecnico");
		
		btnTecnico.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				//Controllo se l'utente ha chat aperte
				MessagingHelper mh = MessagingHelper.getInstance();
				boolean hac = mh.hasActiveChats();
				
				//Se ne ha, mostro un dialog di avviso
				if(hac) {
					JOptionPane.showMessageDialog(frame.getContentPane(), "Chiudi le chat attive prima di continuare.", "Info", JOptionPane.INFORMATION_MESSAGE);
				}else {
					
					//Altrimenti gli consento di procedere..
					//..richiedendo la lista contatti tecnici, che verr� (o meno) recuperata a seconda dei permessi
					//previsti per l'utente corrente.
					ServerHelper sh = new ServerHelper();
					int res = sh.getContactList("tecnici", currentRole);
					//Accesso consentito..
					if(res == 0) {
						ContactFrame cf = new ContactFrame(getCurrentUser(),"Tecnici", new File(sh.getContactListPath()+"/"+"tecnici-list.xml"));
						cf.setVisible(true);
					//Accesso negato..
					}else if(res == -1) {
						JOptionPane.showMessageDialog(frame.getContentPane(), "Accesso Negato!", "Errore", JOptionPane.ERROR_MESSAGE);
					//Errore di applicazione policy..
					}else if(res == -2) {
						JOptionPane.showMessageDialog(frame.getContentPane(), "Errore di applicazione permessi, riprova!", "Errore", JOptionPane.ERROR_MESSAGE);
					//Errore di comunicazione..
					}else{
						JOptionPane.showMessageDialog(frame.getContentPane(), "Errore di comunicazione!", "Errore", JOptionPane.ERROR_MESSAGE);
					}
					
				}
				
			}
		});
		
		btnTecnico.setBounds(10, 47, 207, 23);
		tecnicoPanel.add(btnTecnico);
	}
	
	
	//Inizializza i contenuti del pannello utente.
	private void initializeUserPanel() {
		
		userPanel = new JPanel();
		userPanel.setBounds(10, 263, 414, 85);
		frame.getContentPane().add(userPanel);
		userPanel.setLayout(null);
		userPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		
		lblUser = new JLabel("Clicca qui per parlare con un Utente");
		lblUser.setBounds(10, 11, 207, 14);
		userPanel.add(lblUser);
		
		btnUser = new JButton("Contatta Utente");
		
		btnUser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				//Controllo se l'utente ha chat aperte
				MessagingHelper mh = MessagingHelper.getInstance();
				boolean hac = mh.hasActiveChats();
				
				//Se ne ha, mostro un dialog di avviso
				if(hac) {
					JOptionPane.showMessageDialog(frame.getContentPane(), "Chiudi le chat attive prima di continuare.", "Info", JOptionPane.INFORMATION_MESSAGE);
				}else {
				
					//Altrimenti gli consento di procedere..
					//..richiedendo la lista contatti utenti, che verr� (o meno) recuperata a seconda dei permessi
					//previsti per l'utente corrente.
					ServerHelper sh = new ServerHelper();
					int res = sh.getContactList("utenti", currentRole);
					//Accesso consentito..
					if(res == 0) {
						ContactFrame cf = new ContactFrame(getCurrentUser(),"Utenti", new File(sh.getContactListPath()+"/"+"utenti-list.xml"));
						cf.setVisible(true);
					//Accesso negato..
					}else if(res == -1) {
						JOptionPane.showMessageDialog(frame.getContentPane(), "Accesso Negato!", "Errore", JOptionPane.ERROR_MESSAGE);
					//Errore di applicazione policy..
					}else if(res == -2) {
						JOptionPane.showMessageDialog(frame.getContentPane(), "Errore di applicazione permessi, riprova!", "Errore", JOptionPane.ERROR_MESSAGE);
					//Errore di comunicazione..
					}else{
						JOptionPane.showMessageDialog(frame.getContentPane(), "Errore di comunicazione!", "Errore", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		
		btnUser.setBounds(10, 47, 207, 23);
		userPanel.add(btnUser);
	}
	
	
	
	//Metodo che si occupa di settare il look and feel del frame.
	private void setLookAndFeel() {
		
		try {
			
			UIManager.setLookAndFeel("com.jtattoo.plaf.graphite.GraphiteLookAndFeel");
			
			}catch(Exception e) {
				e.printStackTrace();
			}
	}
	
	
	
/*	private boolean sendPost(String list) {
		
		HttpsURLConnection.setDefaultHostnameVerifier(
			    new HostnameVerifier(){

			        public boolean verify(String hostname, SSLSession sslSession) {
			            if (hostname.equals("localhost")) {
			                return true;
			            }
			            return false;
			        }
			    });

		try {
			
			String httpsURL = "https://localhost:8443/TestContactServlet/contact-lists/"+list+"/";

			URL myurl = new URL(httpsURL);
			HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
			con.setRequestMethod("POST");
			
			String query = "list="+list+"&ruolo="+currentRole;

			con.setDoOutput(true); 
			con.setDoInput(true);
			
			DataOutputStream output = new DataOutputStream(con.getOutputStream());  

			output.writeBytes(query);
			output.flush();
			output.close();
			
			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + httpsURL);
			System.out.println("Post parameters : " + query);
			System.out.println("Response Code : " + responseCode);
			System.out.println("Content type:"+con.getContentType());
			
			if(con.getContentType().contains("application/octet-stream") && responseCode == 200) {

			File f = new File("./liste/"+list+"-list.xml");
			FileOutputStream fos = new FileOutputStream(f);
			InputStream is = con.getInputStream();
			
			
			    byte[] buffer = new byte[4096];
			    int length;
			    while ((length = is.read(buffer)) != -1) {
			        fos.write(buffer, 0, length);
			    }
			    fos.flush();
			    fos.close();
			    is.close();
			    System.out.println("Lista "+list+" scaricata!");
			    return true;
			    
		}else if(con.getContentType().contains("text/plain") && responseCode == 200) {
			
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String input = new String("");
			String readInput =  new String("");

			   while ((input = br.readLine()) != null){
			      readInput = input;
			   }
			   
			   br.close();
			   
			   if(readInput.equals("Accesso negato!")) {
				   JOptionPane.showMessageDialog(frame.getContentPane(), "Accesso Negato!","Errore",JOptionPane.ERROR_MESSAGE);
				   
			   }else {
				   JOptionPane.showMessageDialog(frame.getContentPane(), "Errore interno di policy!","Errore!",JOptionPane.ERROR_MESSAGE);
				  
			   }
			   
			   return false;
			
		}else {
			JOptionPane.showMessageDialog(frame.getContentPane(), "Impossibile contattare, riprova!","Errore",JOptionPane.ERROR_MESSAGE);
			System.out.println("Response code:"+responseCode);
			return false;
		}
			
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
		
		
	}*/
	
	
	//Inizializza l'ascolto sulla porta specificata.
	//In questo modo, all'apertura del Landing Frame l'utente si mette in ascolto di richieste di comunicazione
	//Sulla porta specificata all'avvio del main
	//(Nello scenario reale, la porta di ascolto corrisponder� al "numero di telefono" del soggetto, e verr� settata
	//All'atto dell'autenticazione dell'utente (recuperando tale info dal db)
	private void initializeMessageListening() {
		
		MessagingHelper mh = MessagingHelper.getInstance();
		boolean res = mh.startListening(this.currentNumber);
		
		//Se l'inizializzazione dell'ascolto non va a buon fine (ad es: porta gi� occupata) allora mostro dialog
		if(!res) {
			JOptionPane.showMessageDialog(frame.getContentPane(), "Errore in ascolto messaggi, riprovare.","Errore!",JOptionPane.ERROR_MESSAGE);
		}
	
	}
	
	private String getCurrentUser() {
		return this.currentUser;
	}
	
	
	
	
}
