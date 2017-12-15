package it.chat.gui;

import java.awt.Color;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.UIManager;

import com.octo.captcha.service.image.AbstractManageableImageCaptchaService;
import com.octo.captcha.service.image.DefaultManageableImageCaptchaService;

import it.chat.gui.utility.LookAndFeelUtility;
import it.chat.gui.utility.MessageStringUtility;
import it.chat.helpers.ServerHelper;
import it.chat.user.AuthUser;
import it.sm.exception.ForbiddenAccessException;
import it.sm.exception.ServerErrorException;
import it.sm.exception.TwoFactorRequiredException;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.security.auth.login.FailedLoginException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.UUID;
import java.awt.event.ActionEvent;

public class LoginFrame {

	private JFrame frame;
	
	
	private JPanel topPanel;
	private JLabel welcomeLabel;
	
	private JPanel centerPanel;
	private JPasswordField passwordField;
	private JTextField userNameField;
	private JLabel lblUsername;
	private JLabel lblPassword;
	
	private JPanel bottomPanel;
	private JButton loginButton;
	private JButton chiudiButton;
	private JLabel label;
	
	private JLabel captchaLabel;
	private JTextField captchaField;
	private JLabel labelInserisciCodice;
	private String randString;
	private DefaultManageableImageCaptchaService a;
	private BufferedImage captchaImage;
	
	
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginFrame window = new LoginFrame();
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
	public LoginFrame() {
		initialize();
		this.a = new DefaultManageableImageCaptchaService();
		
		
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		if(!System.getProperty("os.name").toLowerCase().contains("mac"))
			LookAndFeelUtility.setLookAndFeel(LookAndFeelUtility.GRAPHITE);
		initializeFrame();
		initializeTopPanel();
		initializeCenterPanel();
		initializeBottomPanel();
		
	
	}
	
	
	
	private void initializeFrame() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 340);
		frame.setTitle("Login");
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.getContentPane().setBackground(Color.DARK_GRAY);
	}
	
	private void initializeTopPanel() {
		
		topPanel = new JPanel();
		topPanel.setBounds(10, 11, 414, 47);
		frame.getContentPane().add(topPanel);
		topPanel.setLayout(null);
		
		welcomeLabel = new JLabel("Login Panel");
		welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		welcomeLabel.setBounds(0, 0, 414, 49);
		topPanel.add(welcomeLabel);
	}
	
	private void initializeCenterPanel() {
		
		centerPanel = new JPanel();
		centerPanel.setBounds(10, 69, 414, 140);
		frame.getContentPane().add(centerPanel);
		centerPanel.setLayout(null);
		
		userNameField = new JTextField();
		userNameField.setBounds(10, 62, 143, 20);
		centerPanel.add(userNameField);
		userNameField.setColumns(10);
		
		lblUsername = new JLabel("Username");
		lblUsername.setBounds(10, 42, 143, 14);
		centerPanel.add(lblUsername);
		
		lblPassword = new JLabel("Password");
		lblPassword.setBounds(10, 93, 143, 14);
		centerPanel.add(lblPassword);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(10, 109, 143, 20);
		centerPanel.add(passwordField);
		
		captchaLabel = new JLabel("");
		captchaLabel.setBounds(203, 11, 201, 71);
		
		centerPanel.add(captchaLabel);
		
		captchaField = new JTextField();
		captchaField.setBounds(203, 109, 201, 20);
		captchaField.setVisible(false);
		centerPanel.add(captchaField);
		captchaField.setColumns(10);
		
		labelInserisciCodice = new JLabel(MessageStringUtility.INSERT_CP);
		labelInserisciCodice.setBounds(203, 93, 211, 14);
		labelInserisciCodice.setVisible(false);
		centerPanel.add(labelInserisciCodice);
	}
	
	
	private void initializeBottomPanel() {
		
		bottomPanel = new JPanel();
		bottomPanel.setBounds(10, 220, 414, 70);
		frame.getContentPane().add(bottomPanel);
		bottomPanel.setLayout(null);
		
		loginButton = new JButton("Login");
		loginButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				
				if(userNameField.getText().length() == 0 || String.valueOf(passwordField.getPassword()).length() == 0) {
					JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.INSERT_US_PSW, MessageStringUtility.WARNING, JOptionPane.WARNING_MESSAGE);
					
				}else {
					
					if(captchaField.isVisible()) {
						
						if(captchaField.getText().length() == 0) {
							JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.INSERT_CP, MessageStringUtility.WARNING, JOptionPane.WARNING_MESSAGE);
							return;
						}else {
							boolean res = a.validateResponseForID(randString, captchaField.getText());
							if(!res) {
								JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.WRONG_CP, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);
								captchaField.setText("");
								captchaImage = a.getImageChallengeForID(randString);
								captchaLabel.setIcon(new ImageIcon(captchaImage));
								return;
							}
						}
					}
					
					
					String username = userNameField.getText();
					String password = String.valueOf(passwordField.getPassword());
					ServerHelper sh = new ServerHelper();
					
					try {
						
						
						
						AuthUser currentUser = sh.authenticate(username, password);
						System.out.println("Login Completato,benvenuto:"+currentUser.getName());
						
						
						LandingFrame lf = new LandingFrame(currentUser);
						lf.setVisible(true);
						
						frame.dispose();
						
						
					}catch(FailedLoginException ex) {
						System.out.println("Login Fallito");
						JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.WRONG_CRED, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);
						passwordField.setText("");
						
						if(captchaField.isVisible()) {
							captchaField.setText("");
						}
						
						String rand = UUID.randomUUID().toString();
						randString = rand;
						captchaImage = a.getImageChallengeForID(randString);
						captchaLabel.setIcon(new ImageIcon(captchaImage));
						captchaLabel.setVisible(true);
						labelInserisciCodice.setVisible(true);
						captchaField.setVisible(true);
						
					}catch(TwoFactorRequiredException e1) {
						System.out.println(e1.getMessage());
						TwoFactorFrame tff = new TwoFactorFrame(userNameField.getText(),getLoginFrame());
						frame.setEnabled(false);
						tff.setVisible(true);
						
					}catch(ForbiddenAccessException e2) {
						System.out.println(e2.getMessage());
						JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.ACC_BLOCKED, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);
						captchaField.setVisible(false);
						labelInserisciCodice.setVisible(false);
						captchaLabel.setVisible(false);
					
					}catch(ServerErrorException e3) {
						System.out.println(e3.getMessage());
						JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.COMM_ERR, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);
						captchaField.setVisible(false);
						labelInserisciCodice.setVisible(false);
						captchaLabel.setVisible(false);
					}
					
				}
				
				
			}
		});
		
		
		loginButton.setBounds(315, 36, 89, 23);
		loginButton.requestFocus();
		frame.getRootPane().setDefaultButton(loginButton);
		bottomPanel.add(loginButton);
		
		chiudiButton = new JButton("Register");
		chiudiButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("Registrazione");
			}
		});
		chiudiButton.setBounds(216, 36, 89, 23);
		bottomPanel.add(chiudiButton);
	}
	
	
	
	
	public void setEnabled(boolean b) {
		this.frame.setEnabled(b);
	}
	
	public LoginFrame getLoginFrame() {
		return this;
	}
	
	public void setVisible(boolean b) {
		this.frame.setVisible(b);
	}
}
