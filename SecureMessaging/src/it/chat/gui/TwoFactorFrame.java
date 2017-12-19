package it.chat.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import it.chat.gui.utility.LookAndFeelUtility;
import it.chat.gui.utility.MessageStringUtility;
import it.chat.helpers.ServerHelper;
import it.sm.exception.CodeNotFoundException;
import it.sm.exception.ServerErrorException;

public class TwoFactorFrame {

	private JFrame frame;
	
	
	private JPanel topPanel;
	private JLabel firstLabel;
	private JLabel secondLabel;
	private JButton btnVerifica;
	private JTextField codeField;
	private JButton btnIndietro;
	
	private String currentUser;
	private LoginFrame caller;
	private boolean verified = false;
	private boolean debug = true;
	private JSeparator separator;

	/**
	 * Launch the application.
	 *//*
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TwoFactorFrame window = new TwoFactorFrame();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}*/

	/**
	 * Create the application.
	 */
	public TwoFactorFrame(String cu, LoginFrame cal) {
		this.currentUser = cu;
		this.caller = cal;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		if(!System.getProperty("os.name").toLowerCase().contains("mac")) 
			LookAndFeelUtility.setLookAndFeel(LookAndFeelUtility.GRAPHITE);
		initializeFrame();
		initializeTopPanel();
		initializeBottomPanel();
		
	}
	
	private void initializeFrame() {
		
		frame = new JFrame();
		frame.setBounds(100, 100, 453, 199);
		frame.getContentPane().setLayout(null);
		frame.setResizable(false);
		frame.setTitle("Verification Step");
		frame.getContentPane().setBackground(new Color(97, 212, 195));
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

	}
	
	
	private void initializeTopPanel() {
		
		topPanel = new JPanel();
		topPanel.setBounds(0, 0, 453, 93);
		topPanel.setBackground(new Color(36,47,65));
		frame.getContentPane().add(topPanel);
		topPanel.setLayout(null);
		
		firstLabel = new JLabel("We sent a message to the email address linked to your account.");
		firstLabel.setForeground(new Color(255, 255, 255));
		firstLabel.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		firstLabel.setHorizontalAlignment(SwingConstants.CENTER);
		firstLabel.setBounds(19, 22, 417, 24);
		topPanel.add(firstLabel);
		
		secondLabel = new JLabel("Insert the code found inside the message to continue.");
		secondLabel.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		secondLabel.setForeground(new Color(255, 255, 255));
		secondLabel.setHorizontalAlignment(SwingConstants.CENTER);
		secondLabel.setBounds(19, 51, 417, 23);
		topPanel.add(secondLabel);
	}
	
	
	private void initializeBottomPanel() {
		
		codeField = new JTextField();
		codeField.setForeground(new Color(255, 255, 255));
		codeField.setBackground(new Color(97, 212, 195));
		codeField.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		codeField.setBounds(22, 113, 405, 20);
		frame.getContentPane().add(codeField);
		codeField.setColumns(10);
		
		btnIndietro = new JButton("Back");
		btnIndietro.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		btnIndietro.setBounds(262, 145, 89, 26);
		frame.getContentPane().add(btnIndietro);
		
		btnVerifica = new JButton("Verify");
		btnVerifica.setFont(new Font("AppleGothic", Font.PLAIN, 13));
		btnVerifica.setBounds(358, 145, 89, 26);
		frame.getContentPane().add(btnVerifica);
		
		separator = new JSeparator();
		separator.setForeground(new Color(255, 255, 255));
		separator.setBounds(22, 132, 405, 12);
		frame.getContentPane().add(separator);
		btnVerifica.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				if(codeField.getText().length() == 0) {
					JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.EMPTY_CODE, MessageStringUtility.WARNING, JOptionPane.WARNING_MESSAGE);
				}else {
					
					try {
						
						ServerHelper sh = new ServerHelper();
						sh.validateTwoFactorCode(currentUser, codeField.getText());
						verified = true;
						JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.OK_CODE, MessageStringUtility.SUCCESS, JOptionPane.INFORMATION_MESSAGE);
						frame.dispose();
						caller.setEnabled(true);
						
						
					}catch(IllegalArgumentException e) {
						System.out.println("Wrong Code");
						JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.WRONG_CODE, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);
						
					}catch(CodeNotFoundException e1) {
						System.out.println(e1.getMessage());
						JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.NO_CODE, MessageStringUtility.WARNING, JOptionPane.WARNING_MESSAGE);
						frame.dispose();
					}catch(ServerErrorException e2) {
						System.out.println(e2.getMessage());
						JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.COMM_ERR, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);
					}
					
				
				}
				
				
				
				
				
			}
		});
		btnIndietro.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(!verified) {
					JOptionPane.showMessageDialog(frame.getContentPane(), MessageStringUtility.EMPTY_CODE, MessageStringUtility.ERROR, JOptionPane.ERROR_MESSAGE);
				}
				
				if(debug) {
					caller.setEnabled(true);
					frame.dispose();
				}
			}
		});
		
	}
	
	public void setVisible(boolean b) {
		this.frame.setVisible(b);
	}
	
	
	

}
