package it.chat.gui;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextField;

import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.passay.WhitespaceRule;

import it.chat.gui.utility.LookAndFeelUtility;
import it.chat.gui.utility.MessageStringUtility;
import it.chat.helpers.ServerHelper;
import it.sm.exception.AlreadyRegisteredUsernameException;
import it.sm.exception.PasswordCheckFailedException;
import it.sm.exception.ServerErrorException;
import it.sm.exception.TelephoneTipingException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.awt.event.ActionEvent;
import java.awt.Font;
import javax.swing.JSeparator;
import javax.swing.ImageIcon;

public class RegistrationFrame {

	private JFrame frame;
	private JPanel centerPanel;
	private JPanel bottomPanel;
	private JButton btnRegister;
	private JPasswordField passwordField;
	private JTextField nameField;
	private JTextField surnameField;
	private JLabel lblSurname;
	private JSeparator separator_3;
	private JTextField emailField;
	private JLabel lblEmail;
	private JSeparator separator_4;
	private JTextField telephoneField;
	private JLabel lblTelephone;
	private JSeparator separator_5;
	private JTextField usernameField;
	private JLabel lblUsername;
	private JSeparator separator;
	private JLabel lblAlreadyAMember;
	private JLabel lblNewLabel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					RegistrationFrame window = new RegistrationFrame();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public RegistrationFrame() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		if(!System.getProperty("os.name").toLowerCase().contains("mac")) {
			LookAndFeelUtility.setLookAndFeel(LookAndFeelUtility.GRAPHITE);
		}
		
		initializeFrame();
		initializeTopPanel();
		initializeCenterPanel();
		initializeBottomPanel();
		
	}
	
	
	private void initializeFrame() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setResizable(false);
		frame.setTitle("Registration Panel");
		frame.getContentPane().setBackground(new Color(97, 212, 195));

	}
	
	private void initializeTopPanel() {
		
	}
	
	private void initializeCenterPanel() {
		
		centerPanel = new JPanel();
		centerPanel.setBounds(70, 0, 307, 378);
		frame.getContentPane().add(centerPanel);
		centerPanel.setLayout(null);
		centerPanel.setBackground(new Color(36, 47, 65));
		
		JLabel lblPassword = new JLabel("password");
		lblPassword.setForeground(new Color(255, 255, 255));
		lblPassword.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		lblPassword.setBounds(175, 98, 68, 20);
		centerPanel.add(lblPassword);
		
		JLabel lblNome = new JLabel("name");
		lblNome.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		lblNome.setForeground(new Color(255, 255, 255));
		lblNome.setBounds(197, 149, 72, 14);
		centerPanel.add(lblNome);
		
		btnRegister = new JButton("Register");
		btnRegister.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		btnRegister.setBounds(115, 318, 89, 23);
		centerPanel.add(btnRegister);
		
		passwordField = new JPasswordField();
		passwordField.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		passwordField.setForeground(new Color(255, 255, 255));
		passwordField.setColumns(10);
		passwordField.setBackground(new Color(36, 47, 65));
		passwordField.setBounds(75, 72, 159, 20);
		centerPanel.add(passwordField);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setForeground(Color.WHITE);
		separator_1.setBounds(74, 91, 159, 14);
		centerPanel.add(separator_1);
		
		nameField = new JTextField();
		nameField.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		nameField.setForeground(new Color(255, 255, 255));
		nameField.setColumns(10);
		nameField.setBackground(new Color(36, 47, 65));
		nameField.setBounds(74, 117, 159, 20);
		centerPanel.add(nameField);
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setForeground(Color.WHITE);
		separator_2.setBounds(74, 138, 159, 14);
		centerPanel.add(separator_2);
		
		surnameField = new JTextField();
		surnameField.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		surnameField.setForeground(new Color(255, 255, 255));
		surnameField.setColumns(10);
		surnameField.setBackground(new Color(36, 47, 65));
		surnameField.setBounds(74, 164, 159, 20);
		centerPanel.add(surnameField);
		
		lblSurname = new JLabel("surname");
		lblSurname.setForeground(Color.WHITE);
		lblSurname.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		lblSurname.setBounds(175, 196, 68, 14);
		centerPanel.add(lblSurname);
		
		separator_3 = new JSeparator();
		separator_3.setForeground(Color.WHITE);
		separator_3.setBounds(74, 185, 159, 14);
		centerPanel.add(separator_3);
		
		emailField = new JTextField();
		emailField.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		emailField.setForeground(new Color(255, 255, 255));
		emailField.setColumns(10);
		emailField.setBackground(new Color(36, 47, 65));
		emailField.setBounds(74, 211, 159, 20);
		centerPanel.add(emailField);
		
		lblEmail = new JLabel("email");
		lblEmail.setForeground(Color.WHITE);
		lblEmail.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		lblEmail.setBounds(196, 243, 39, 14);
		centerPanel.add(lblEmail);
		
		separator_4 = new JSeparator();
		separator_4.setForeground(Color.WHITE);
		separator_4.setBounds(74, 232, 159, 14);
		centerPanel.add(separator_4);
		
		telephoneField = new JTextField();
		telephoneField.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		telephoneField.setForeground(new Color(255, 255, 255));
		telephoneField.setColumns(10);
		telephoneField.setBackground(new Color(36, 47, 65));
		telephoneField.setBounds(74, 259, 159, 20);
		centerPanel.add(telephoneField);
		
		lblTelephone = new JLabel("telephone");
		lblTelephone.setForeground(Color.WHITE);
		lblTelephone.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		lblTelephone.setBounds(175, 292, 72, 14);
		centerPanel.add(lblTelephone);
		
		separator_5 = new JSeparator();
		separator_5.setForeground(Color.WHITE);
		separator_5.setBounds(74, 280, 159, 14);
		centerPanel.add(separator_5);
		
		usernameField = new JTextField();
		usernameField.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		usernameField.setForeground(new Color(255, 255, 255));
		usernameField.setColumns(10);
		usernameField.setBackground(new Color(36, 47, 65));
		usernameField.setBounds(75, 22, 159, 20);
		centerPanel.add(usernameField);
		
		lblUsername = new JLabel("username");
		lblUsername.setForeground(Color.WHITE);
		lblUsername.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		lblUsername.setBounds(175, 54, 72, 14);
		centerPanel.add(lblUsername);
		
		separator = new JSeparator();
		separator.setForeground(Color.WHITE);
		separator.setBounds(75, 43, 159, 14);
		centerPanel.add(separator);
		
		lblAlreadyAMember = new JLabel("Already a member? Sign in");
		lblAlreadyAMember.setForeground(Color.WHITE);
		lblAlreadyAMember.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		lblAlreadyAMember.setBounds(75, 345, 165, 16);
		lblAlreadyAMember.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				lblAlreadyAMember.setForeground(new Color(97, 212, 195));
			}
			@Override
			public void mousePressed(MouseEvent e) {
				lblAlreadyAMember.setForeground(new Color(97, 212, 195));
				frame.dispose();
				LoginFrame lf = new LoginFrame();
				lf.setVisible(true);

			}
			@Override
			public void mouseExited(MouseEvent e) {
				lblAlreadyAMember.setForeground(new Color(255, 255, 255));

			}
		});
		centerPanel.add(lblAlreadyAMember);
		
		lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(new ImageIcon(RegistrationFrame.class.getResource("/it/chat/gui/icons/icon_app.png")));
		lblNewLabel.setBounds(6, -156, 477, 534);
		frame.getContentPane().add(lblNewLabel);
		btnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			
			
			
		try {
				String pass = String.valueOf(passwordField.getPassword());
				checkPassword(pass);
				String telephone = String.valueOf(telephoneField.getText());
				checkTelephoneRight(telephone);
					
				
				//Necessario controllo sulla validit� degli parametri di input
				
				//Quando so che sono validi (e che non sono vuoti)..
				
				//Per testare , visto che la servlet accetta solo username e password (per ora)
				//E' possibile eliminare le params.put(..) relative agli altri parametri e lasciare solo quelle
				//per username e password
				Map<String, Object> params = new LinkedHashMap<>();
				
				params.put("username", usernameField.getText());
				params.put("password", String.valueOf(passwordField.getPassword()));
				params.put("name", nameField.getText());
				params.put("surname", surnameField.getText());
				params.put("email", emailField.getText());
				params.put("telephone", telephoneField.getText());
				
				
				ServerHelper sh = new ServerHelper();
				sh.register(params);
				
				JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.REG_OK, MessageStringUtility.SUCCESS, JOptionPane.INFORMATION_MESSAGE);
				frame.dispose();
				LoginFrame lf = new LoginFrame();
				lf.setVisible(true);
				
			}catch(PasswordCheckFailedException e) {
				System.out.println("Invalid password");
				JOptionPane.showMessageDialog(frame.getContentPane(), e.getMessage(), MessageStringUtility.WARNING, JOptionPane.WARNING_MESSAGE);
			
			} catch(TelephoneTipingException e) {
				JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.ERR_TEL, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);
			
			} catch (AlreadyRegisteredUsernameException e) {
				System.out.println(e.getMessage());
				JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.ERR_REG, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);

			} catch (ServerErrorException e) {
				System.out.println(e.getMessage());
				JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.COMM_ERR, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);

			}
				
			}
		});
	}
	
	private void initializeBottomPanel() {
	}
	
	
	private void checkPassword(String password) throws PasswordCheckFailedException {
		PasswordValidator validator = new PasswordValidator(
				  
				  // length between 8 and 16 characters
				  new LengthRule(8, 16),

				  // at least one upper-case character
				  new CharacterRule(EnglishCharacterData.UpperCase, 1),

				  // at least one lower-case character
				  new CharacterRule(EnglishCharacterData.LowerCase, 1),

				  // at least one digit character
				  new CharacterRule(EnglishCharacterData.Digit, 1),

				  // at least one symbol (special character)
				  new CharacterRule(EnglishCharacterData.Special, 1),

				  // no whitespace
				  new WhitespaceRule());

				
				RuleResult result = validator.validate(new PasswordData(password));
				if (result.isValid()) {
				  
				} else {
				  
				  StringBuilder b = new StringBuilder();
				  for (String msg : validator.getMessages(result)) {
				    b.append(msg);
				    b.append("\n");
				  }
				 
				 String finalmsg = b.toString();
				 
				 throw new PasswordCheckFailedException(finalmsg);
				 
				 
				}
	
	}
	
	public void checkTelephoneRight(String t) throws TelephoneTipingException {
		
		if (!t.matches("[0-9]+"))
				throw new TelephoneTipingException();
		
	}
	
	public void setVisible(boolean b) {
		this.frame.setVisible(b);
	}
}
