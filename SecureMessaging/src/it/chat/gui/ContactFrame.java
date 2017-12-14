package it.chat.gui;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.chat.helpers.CertificateHelper;
import it.chat.helpers.MessagingHelper;
import it.chat.helpers.ServerHelper;
import it.sm.exception.CertificateNotFoundException;
import it.sm.exception.ServerErrorException;


public class ContactFrame {

	private JFrame frame;
	
	private JPanel topPanel;
	private JPanel buttonPanel;
	private JPanel centerPanel;
	
	private JButton contactButton;
	private JButton backButton;
	
	private JLabel titoloLabel;
	private JTable rubricaList;
	private DefaultTableModel dtm;
	
	private File contactList;
	private String contactType;
	private String currentUser;
	
	private static final int STARTER = 1;
	
	/* Frame che mostra la lista contatti che � stata richiesta nel Landing Frame,
	 * sotto forma di tabella.
	 * Consente di scegliere tra i diversi contatti disponibili per iniziare 
	 * una nuova chat.
	 */
	
	
	
	//Main di test, usato per lanciare in maniera autonoma il frame
	/*
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					//testing
					ContactFrame window = new ContactFrame("CU","ctype",new File("./liste/utenti-list.xml"));
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	*/

	//Costruttore: prende il current user, il tipo di contact list da trattare e il File che punta alla contact list (xml)
	//che mostreremo nel frame
	public ContactFrame(String cu,String ctype, File cList) {
		this.currentUser = cu;
		this.contactType = ctype;
		this.contactList = cList;
		initialize();
	}

	
	private void initialize() {
		
		setLookAndFeel();
		initializeFrame();
		initializeTopPanel();
		initializeCenterPanel();
		initializeButtonPanel();
		fillContacts();
	}
	
	//Inizializza il frame.
	private void initializeFrame() {
		
		frame = new JFrame();
		frame.setTitle("Rubrica "+contactType);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(100, 100, 495, 298);
		frame.setResizable(false);
		frame.getContentPane().setLayout(new BorderLayout());
	}
	
	//Inizializza il pannello superiore.
	private void initializeTopPanel() {
		
		topPanel = new JPanel();
		frame.getContentPane().add(topPanel,BorderLayout.NORTH);
		
		titoloLabel = new JLabel("Contatti");
		
        topPanel.add(titoloLabel);
	}
	
	
	//Inizializza il pannello centrale, che contiene la tabella dei contatti.
	private void initializeCenterPanel() {
		
		centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		frame.getContentPane().add(centerPanel,BorderLayout.CENTER);
		
		rubricaList = new JTable();
		
	    dtm = new DefaultTableModel(0,0) {
            
	    	
			private static final long serialVersionUID = 1L;

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
            public Class getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
            
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
		
		String header[] = new String[] {"Nome","Cognome","Numero"};
		dtm.setColumnIdentifiers(header);
		rubricaList.setModel(dtm);
		rubricaList.setDefaultEditor(Object.class, null);
		rubricaList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rubricaList.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		rubricaList.setDefaultRenderer(String.class, centerRenderer);
		
		
		rubricaList.getColumnModel().getColumn(0).setPreferredWidth(165);
		rubricaList.getColumnModel().getColumn(1).setPreferredWidth(165);
		rubricaList.getColumnModel().getColumn(2).setPreferredWidth(165);
		
		
		JScrollPane scrollPane = new JScrollPane(rubricaList,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		centerPanel.add(scrollPane, BorderLayout.CENTER);
		
	}
	
	//Inizializza i bottoni nella parte inferiore del frame.
	private void initializeButtonPanel() {
		
		buttonPanel = new JPanel();
		frame.getContentPane().add(buttonPanel,BorderLayout.SOUTH);
		
		backButton = new JButton("Indietro");
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				frame.dispose();
				
			}
		});
        
		buttonPanel.add(backButton);
        
        contactButton = new JButton("Contatta");
        contactButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		
        		//Prendo la riga selezionata
        		int row = rubricaList.getSelectedRow();
        		
        		//Se non seleziono nessuno, mostro un dialog
        		if(row == -1) {
        			JOptionPane.showMessageDialog(frame.getContentPane(), "Seleziona un soggetto da contattare!","Errore!",JOptionPane.ERROR_MESSAGE);
        		//Altrimenti..
        		}else {
        			
        			//Ricavo i dati dalla riga selezionata
        			String nome = (String)rubricaList.getValueAt(row, 0);
        			String cognome = (String)rubricaList.getValueAt(row, 1);
        			String identity = nome+" "+cognome;
        			int destPort = Integer.valueOf((String)rubricaList.getValueAt(row, 2));
        			
        			CertificateHelper ch = CertificateHelper.getInstance();
        			
        			try {
						ch.getCertificate(nome, cognome);
					
        			} catch (CertificateNotFoundException e1) {
						System.out.println(e1.getMessage());
						JOptionPane.showMessageDialog(frame.getContentPane(), "Impossibile recuperare le informazioni del soggetto, riprovare.","Errore!",JOptionPane.ERROR_MESSAGE);
					
					} catch (ServerErrorException e1) {
						JOptionPane.showMessageDialog(frame.getContentPane(), "Errore di comunicazione, riprovare.","Errore!",JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					}
        			
        			
        			/*//Apro un nuovo chat frame per gestire la chat con il soggetto selezionato
        			ChatFrame cf = new ChatFrame(getCurrentUser(),identity,destPort, STARTER);
        			frame.dispose();
        			cf.setVisible(true);*/
        			
        			
        		}
        		
        	}
        });
		
        buttonPanel.add(contactButton);
        
       
        
	}
	
	//Setta il look and feel dell'applicazione
	private void setLookAndFeel() {
		
		try {
			
			UIManager.setLookAndFeel("com.jtattoo.plaf.graphite.GraphiteLookAndFeel");
			
			}catch(Exception e) {
				e.printStackTrace();
			}
	}

	
	//Effettua il parsing del file xml contenente la lista contatti, e ne mostra il contenuto in una tabella.
	//Si occupa inoltre di distruggere il file dopo aver svolto le necessarie operazioni di parsing,
	//Evitando cosi di conservare informazioni che possono essere riutilizzate in maniera non autorizzata.
	private void fillContacts() {
		
		try {

			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(this.contactList);

			doc.getDocumentElement().normalize();

			
			NodeList nList = null;
			
			if(this.contactType.equals("Admins")) {
				nList = doc.getElementsByTagName("admin");
			}else if(this.contactType.equals("Utenti")) {
				nList = doc.getElementsByTagName("utente");
			}else {
				nList = doc.getElementsByTagName("tecnico");
			}
			

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);


				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element currElem = (Element) nNode;
					
					String nome = currElem.getElementsByTagName("nome").item(0).getTextContent();
					String cognome = currElem.getElementsByTagName("cognome").item(0).getTextContent();
					String numero = currElem.getElementsByTagName("numero").item(0).getTextContent();
					
					dtm.addRow(new Object []{nome,cognome,numero});
				
				}
			}
			
			//Al termine dell'operazione di parsing del contenuto del file, lo distruggo
			this.contactList.delete();
			
		    } catch (Exception e) {
			e.printStackTrace();
			this.contactList.delete();
		    }
		  }
		

	public void setVisible(boolean f) {
		this.frame.setVisible(true);
	}
	
	
	
	private String getCurrentUser() {
		return this.currentUser;
	}
}