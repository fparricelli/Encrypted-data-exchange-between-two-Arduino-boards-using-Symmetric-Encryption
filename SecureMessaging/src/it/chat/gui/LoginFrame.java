package it.chat.gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.UUID;

import javax.security.auth.login.FailedLoginException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import com.octo.captcha.service.image.DefaultManageableImageCaptchaService;

import it.chat.gui.utility.LookAndFeelUtility;
import it.chat.gui.utility.MessageStringUtility;
import it.chat.helpers.ServerHelper;
import it.chat.user.AuthUser;
import it.sm.exception.ForbiddenAccessException;
import it.sm.exception.ServerErrorException;
import it.sm.exception.TwoFactorRequiredException;

public class LoginFrame {

	private JFrame frame;
	
	private JPanel centerPanel;
	private JPasswordField passwordField;
	private JTextField userNameField;
	private JButton loginButton;
	
	private JLabel captchaLabel;
	private JTextField captchaField;
	private String randString;
	private DefaultManageableImageCaptchaService a;
	private BufferedImage captchaImage;
	private JLabel lblPassword;
	private JSeparator separator_1;
	private JLabel lblInsertCaptcha;
	private JSeparator separator_2;
	private JLabel lblNotRegisteredSign;
	private JLabel lblNewLabel;
	
	
	
	
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
		frame.getContentPane().setBackground(new Color(97, 212, 195));
	}
	
	private void initializeTopPanel() {
	}
	
	private void initializeCenterPanel() {
		
		centerPanel = new JPanel();
		centerPanel.setBounds(81, 0, 299, 318);
		frame.getContentPane().add(centerPanel);
		centerPanel.setLayout(null);
		centerPanel.setBackground(new Color(36, 47, 65));
		userNameField = new JTextField();
		userNameField.setForeground(new Color(255, 255, 255));
		userNameField.setBackground(new Color(36, 47, 65));
		userNameField.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		userNameField.setToolTipText("");
		userNameField.setBounds(77, 18, 143, 23);
		centerPanel.add(userNameField);
		userNameField.setColumns(10);
		
		passwordField = new JPasswordField();
		passwordField.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		passwordField.setBounds(77, 75, 143, 23);

		passwordField.setForeground(new Color(255, 255, 255));
		passwordField.setBackground(new Color(36, 47, 65));
		centerPanel.add(passwordField);
		
		captchaLabel = new JLabel("");
		captchaLabel.setBounds(52, 123, 201, 71);
		
		centerPanel.add(captchaLabel);
		
		captchaField = new JTextField();
		captchaField.setBounds(52, 195, 201, 20);
		captchaField.setVisible(false);
		captchaField.setForeground(new Color(255, 255, 255));
		captchaField.setBackground(new Color(36, 47, 65));
		centerPanel.add(captchaField);
		captchaField.setColumns(10);
		
		loginButton = new JButton("Login");
		loginButton.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		loginButton.setBounds(103, 250, 89, 23);
		centerPanel.add(loginButton);
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
						separator_2.setVisible(true);
						lblInsertCaptcha.setVisible(true);
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
						lblInsertCaptcha.setVisible(false);
						captchaLabel.setVisible(false);
					
					}catch(ServerErrorException e3) {
						System.out.println(e3.getMessage());
						JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.COMM_ERR, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);
						captchaField.setVisible(false);
						lblInsertCaptcha.setVisible(false);
						captchaLabel.setVisible(false);
					}
					
				}
				
				
			}
		});
		loginButton.requestFocus();
		frame.getRootPane().setDefaultButton(loginButton);
		
		JSeparator separator = new JSeparator();
		separator.setForeground(SystemColor.activeCaption);
		separator.setBounds(77, 39, 143, 12);
		centerPanel.add(separator);
		
		JLabel lblUsername = new JLabel("username");
		lblUsername.setForeground(SystemColor.activeCaption);
		lblUsername.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		lblUsername.setBounds(163, 47, 61, 16);
		centerPanel.add(lblUsername);
		
		lblPassword = new JLabel("password");
		lblPassword.setForeground(Color.WHITE);
		lblPassword.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		lblPassword.setBounds(159, 105, 61, 16);
		centerPanel.add(lblPassword);
		
		separator_1 = new JSeparator();
		separator_1.setForeground(Color.WHITE);
		separator_1.setBounds(77, 99, 143, 12);
		centerPanel.add(separator_1);
		
		lblInsertCaptcha = new JLabel("Insert Captcha");
		lblInsertCaptcha.setForeground(Color.WHITE);
		lblInsertCaptcha.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		lblInsertCaptcha.setBounds(163, 220, 90, 16);
		lblInsertCaptcha.setVisible(false);
		centerPanel.add(lblInsertCaptcha);
		
		separator_2 = new JSeparator();
		separator_2.setForeground(Color.WHITE);
		separator_2.setBounds(52, 214, 201, 12);
		separator_2.setVisible(false);
		centerPanel.add(separator_2);
		
		lblNotRegisteredSign = new JLabel("Not Registered? Sign Up");
		lblNotRegisteredSign.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				lblNotRegisteredSign.setForeground(new Color(97, 212, 195));
			}
			@Override
			public void mousePressed(MouseEvent e) {
				lblNotRegisteredSign.setForeground(new Color(97, 212, 195));
				frame.dispose();
				RegistrationFrame rf = new RegistrationFrame();
				rf.setVisible(true);

			}
			@Override
			public void mouseExited(MouseEvent e) {
				lblNotRegisteredSign.setForeground(new Color(255, 255, 255));

			}
		});
		lblNotRegisteredSign.setForeground(new Color(255, 255, 255));
		lblNotRegisteredSign.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		lblNotRegisteredSign.setBounds(77, 285, 147, 16);
		centerPanel.add(lblNotRegisteredSign);
		
		lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(new ImageIcon(LoginFrame.class.getResource("/it/chat/gui/icons/icon_app.png")));
		lblNewLabel.setBounds(6, -197, 470, 509);
		frame.getContentPane().add(lblNewLabel);
	}
	
	
	private void initializeBottomPanel() {
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
