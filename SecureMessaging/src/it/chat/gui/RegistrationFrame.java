package it.chat.gui;

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

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import java.awt.event.ActionEvent;

public class RegistrationFrame {

	private JFrame frame;
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JTextField nameField;
	private JTextField surnameField;
	private JTextField emailField;
	private JTextField telephoneField;
	
	private JPanel topPanel;
	private JPanel centerPanel;
	private JPanel bottomPanel;
	
	private JButton btnBack;
	private JButton btnRegister;
	
	private JComboBox roleBox;

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
		frame.setBounds(100, 100, 450, 358);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setResizable(false);
		frame.setTitle("Registration Panel");
	}
	
	private void initializeTopPanel() {
		
		topPanel = new JPanel();
		topPanel.setBounds(10, 11, 414, 34);
		frame.getContentPane().add(topPanel);
		topPanel.setLayout(null);
		
	}
	
	private void initializeCenterPanel() {
		
		centerPanel = new JPanel();
		centerPanel.setBounds(10, 54, 414, 190);
		frame.getContentPane().add(centerPanel);
		centerPanel.setLayout(null);
		
		JLabel lblUsername = new JLabel("Username");
		lblUsername.setBounds(10, 22, 68, 14);
		centerPanel.add(lblUsername);
		
		usernameField = new JTextField();
		usernameField.setBounds(82, 19, 102, 20);
		centerPanel.add(usernameField);
		usernameField.setColumns(10);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(82, 47, 102, 20);
		centerPanel.add(passwordField);
		
		JLabel lblPassword = new JLabel("Password");
		lblPassword.setBounds(10, 50, 68, 14);
		centerPanel.add(lblPassword);
		
		nameField = new JTextField();
		nameField.setBounds(296, 19, 108, 20);
		centerPanel.add(nameField);
		nameField.setColumns(10);
		
		JLabel lblNome = new JLabel("Name");
		lblNome.setBounds(214, 22, 72, 14);
		centerPanel.add(lblNome);
		
		surnameField = new JTextField();
		surnameField.setBounds(296, 47, 108, 20);
		centerPanel.add(surnameField);
		surnameField.setColumns(10);
		
		JLabel lblSurname = new JLabel("Surname");
		lblSurname.setBounds(214, 50, 77, 14);
		centerPanel.add(lblSurname);
		
		emailField = new JTextField();
		emailField.setBounds(296, 79, 108, 20);
		centerPanel.add(emailField);
		emailField.setColumns(10);
		
		JLabel lblEmail = new JLabel("Email");
		lblEmail.setBounds(214, 82, 68, 14);
		centerPanel.add(lblEmail);
		
		telephoneField = new JTextField();
		telephoneField.setBounds(296, 113, 108, 20);
		centerPanel.add(telephoneField);
		telephoneField.setColumns(10);
		
		JLabel lblNumber = new JLabel("Telephone");
		lblNumber.setBounds(214, 116, 77, 14);
		centerPanel.add(lblNumber);
		
		roleBox = new JComboBox();
		roleBox.setBounds(296, 145, 108, 20);
		roleBox.addItem("Tecnico");
		roleBox.addItem("Utente");
		roleBox.addItem("Admin");
		centerPanel.add(roleBox);
		
		JLabel lblRole = new JLabel("Role");
		lblRole.setBounds(214, 148, 68, 14);
		centerPanel.add(lblRole);
	}
	
	private void initializeBottomPanel() {
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setBounds(10, 255, 414, 53);
		frame.getContentPane().add(bottomPanel);
		bottomPanel.setLayout(null);
		
		btnRegister = new JButton("Register");
		btnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			
			
			
		try {
				String pass = String.valueOf(passwordField.getPassword());
				checkPassword(pass);
				
				//Necessario controllo sulla validità degli parametri di input
				
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
				params.put("role", String.valueOf(roleBox.getSelectedItem()).toLowerCase());
				
				ServerHelper sh = new ServerHelper();
				sh.register(params);
				
				JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.REG_OK, MessageStringUtility.SUCCESS, JOptionPane.INFORMATION_MESSAGE);
				frame.dispose();
				LoginFrame lf = new LoginFrame();
				lf.setVisible(true);
				
			}catch(PasswordCheckFailedException e) {
				System.out.println("Invalid password");
				JOptionPane.showMessageDialog(frame.getContentPane(), e.getMessage(), MessageStringUtility.WARNING, JOptionPane.WARNING_MESSAGE);
			
			} catch (AlreadyRegisteredUsernameException e) {
				System.out.println(e.getMessage());
				JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.ERR_REG, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);

			} catch (ServerErrorException e) {
				System.out.println(e.getMessage());
				JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.COMM_ERR, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);

			}
				
			}
		});
		btnRegister.setBounds(315, 19, 89, 23);
		bottomPanel.add(btnRegister);
		
		btnBack = new JButton("Back");
		btnBack.setBounds(216, 19, 89, 23);
		bottomPanel.add(btnBack);
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
	
	public void setVisible(boolean b) {
		this.frame.setVisible(b);
	}
}
