package it.chat.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import it.chat.gui.utility.LookAndFeelUtility;
import it.chat.gui.utility.MessageStringUtility;
import it.chat.helpers.CertificateHelper;
import it.chat.helpers.MessagingHelper;
import it.chat.helpers.ServerHelper;
import it.sm.exception.CertificateNotFoundException;
import it.sm.exception.ServerErrorException;
import java.awt.Font;


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
	
	
	private String currentNome;
	private String currentCognome;
	
	private static final int STARTER = 1;
	
	JFrame progressFrame;
	
	SwingProgressBar bar;
	

	
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
	public ContactFrame(String cn,String cc,String ctype, File cList) {
		this.currentNome = cn;
		this.currentCognome = cc;
		this.contactType = ctype;
		this.contactList = cList;
		bar = new SwingProgressBar();
		progressFrame = new JFrame("Loading..");
		initialize();
	}
	
	private void initializeLoadingBar() {
		progressFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		progressFrame.setBounds(200, 200, 300, 50);
		progressFrame.pack();
		progressFrame.setResizable(false);
		progressFrame.setContentPane(bar);
		
	}

	
	private void initialize() {
		initializeLoadingBar();
		if(!System.getProperty("os.name").toLowerCase().contains("mac")) 
			LookAndFeelUtility.setLookAndFeel(LookAndFeelUtility.GRAPHITE);
		initializeFrame();
		initializeTopPanel();
		initializeCenterPanel();
		initializeButtonPanel();
		fillContacts();
	}
	
	//Inizializza il frame.
	private void initializeFrame() {
		
		frame = new JFrame();
		frame.setTitle("Contacts:"+contactType);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(100, 100, 495, 298);
		frame.setResizable(false);
		frame.getContentPane().setLayout(new BorderLayout());
	}
	
	//Inizializza il pannello superiore.
	private void initializeTopPanel() {
		
		topPanel = new JPanel();
		topPanel.setBackground(new Color(36, 47, 65));
		frame.getContentPane().add(topPanel,BorderLayout.NORTH);
		
		titoloLabel = new JLabel(MessageStringUtility.SELECT_CONTACT);
		titoloLabel.setForeground(Color.WHITE);
		titoloLabel.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		titoloLabel.setBackground(Color.WHITE);
		
        topPanel.add(titoloLabel);
	}
	
	
	//Inizializza il pannello centrale, che contiene la tabella dei contatti.
	private void initializeCenterPanel() {
		
		centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.setBackground(new Color(97, 212, 195));
		frame.getContentPane().add(centerPanel,BorderLayout.CENTER);
		
		rubricaList = new JTable();
		rubricaList.setFont(new Font("AppleGothic", Font.PLAIN, 12));
		rubricaList.setBackground(new Color(97, 212, 195));
		
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
		
		String header[] = new String[] {"Name","Surname","Number"};
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
		centerPanel.add(scrollPane, BorderLayout.NORTH);
		scrollPane.setBackground(new Color(97, 212, 195));
		
	}
	
	//Inizializza i bottoni nella parte inferiore del frame.
	private void initializeButtonPanel() {
		
		buttonPanel = new JPanel();
		buttonPanel.setBackground(new Color(36, 47, 65));
		frame.getContentPane().add(buttonPanel,BorderLayout.SOUTH);
		
		backButton = new JButton("Back");
		backButton.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				frame.dispose();
				
			}
		});
        
		buttonPanel.add(backButton);
        
        contactButton = new JButton("Contact");
        contactButton.setFont(new Font("AppleGothic", Font.PLAIN, 13));
        contactButton.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		
        		//Prendo la riga selezionata
        		int row = rubricaList.getSelectedRow();
        		
        		//Se non seleziono nessuno, mostro un dialog
        		if(row == -1) {
        			JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.SELECT_CONTACT_ERR,MessageStringUtility.ERROR,JOptionPane.ERROR_MESSAGE);
        		//Altrimenti..
        		}else {
        			progressFrame.setVisible(true);
        			//Ricavo i dati dalla riga selezionata
        			String targetNome = (String)rubricaList.getValueAt(row, 0);
        			String targetCognome = (String)rubricaList.getValueAt(row, 1);
        			String targetIdentity = targetNome+" "+targetCognome;
        			int destPort = Integer.valueOf((String)rubricaList.getValueAt(row, 2));
        			        			
        			bar.updateBar(40);
        		 
	        		bar.updateBar(80);

						//Apro un nuovo chat frame per gestire la chat con il soggetto selezionato
	        			String currentIdentity = currentNome+" "+currentCognome;
	        			ChatFrame cf = new ChatFrame(currentIdentity,targetIdentity,destPort, STARTER);
	        			frame.dispose();
	        			cf.setVisible(true);
	        			
        			progressFrame.dispose();
        			
        			
        		}
        		
        	}
        });
		
        buttonPanel.add(contactButton);
        
       
        
	}
	
	

	
	//Effettua il parsing del file xml contenente la lista contatti, e ne mostra il contenuto in una tabella.
	//Si occupa inoltre di distruggere il file dopo aver svolto le necessarie operazioni di parsing,
	//Evitando cosi di conservare informazioni che possono essere riutilizzate in maniera non autorizzata.
	private void fillContacts() {
		
		try {
			
			
			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			
			protectXML(dbFactory);
			
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
					
					
					if(!(nome.equals(currentNome) && cognome.equals(currentCognome))) {
						dtm.addRow(new Object []{nome,cognome,numero});
					}
					
				
				}
			}
			
			//Al termine dell'operazione di parsing del contenuto del file, lo distruggo
			this.contactList.delete();
			
		    } catch (Exception e) {
			e.printStackTrace();
			this.contactList.delete();
			JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.CONT_LIST_ERR,MessageStringUtility.ERROR,JOptionPane.ERROR_MESSAGE);

		    }
		  }
		

	public void setVisible(boolean f) {
		this.frame.setVisible(true);
	}
	
	
	//Protezione contro attacchi XXE
	private void protectXML(DocumentBuilderFactory dbf) throws Exception{
		
			String FEATURE = null;
			// This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity attacks are prevented
			// Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
	       FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
	       dbf.setFeature(FEATURE, true);
	 
	       // If you can't completely disable DTDs, then at least do the following:
	       // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
	       // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
	       // JDK7+ - http://xml.org/sax/features/external-general-entities    
	       FEATURE = "http://xml.org/sax/features/external-general-entities";
	       dbf.setFeature(FEATURE, false);
	 
	       // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
	       // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
	       // JDK7+ - http://xml.org/sax/features/external-parameter-entities    
	       FEATURE = "http://xml.org/sax/features/external-parameter-entities";
	       dbf.setFeature(FEATURE, false);
	 
	       // Disable external DTDs as well
	       FEATURE = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
	       dbf.setFeature(FEATURE, false);
	 
	       // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
	       dbf.setXIncludeAware(false);
	       dbf.setExpandEntityReferences(false);
	       
	       dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		
		
	}
	
}
