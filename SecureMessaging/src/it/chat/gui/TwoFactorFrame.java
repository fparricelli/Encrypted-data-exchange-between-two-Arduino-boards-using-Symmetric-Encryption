package it.chat.gui;

import java.awt.Color;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import it.chat.gui.utility.LookAndFeelUtility;
import it.chat.gui.utility.MessageStringUtility;
import it.chat.helpers.ServerHelper;
import it.sm.exception.CodeNotFoundException;
import it.sm.exception.ServerErrorException;

import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class TwoFactorFrame {

	private JFrame frame;
	
	
	private JPanel topPanel;
	private JLabel firstLabel;
	private JLabel secondLabel;
	
	private JPanel bottomPanel;
	private JButton btnVerifica;
	private JTextField codeField;
	private JButton btnIndietro;
	
	private String currentUser;
	private LoginFrame caller;
	private boolean verified = false;
	private boolean debug = true;

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
		frame.getContentPane().setBackground(Color.DARK_GRAY);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

	}
	
	
	private void initializeTopPanel() {
		
		topPanel = new JPanel();
		topPanel.setBounds(10, 11, 417, 48);
		frame.getContentPane().add(topPanel);
		topPanel.setLayout(null);
		
		firstLabel = new JLabel("We sent a message to the email address linked to your account.");
		firstLabel.setHorizontalAlignment(SwingConstants.CENTER);
		firstLabel.setBounds(0, 0, 417, 24);
		topPanel.add(firstLabel);
		
		secondLabel = new JLabel("Insert the code found inside the message to continue.");
		secondLabel.setHorizontalAlignment(SwingConstants.CENTER);
		secondLabel.setBounds(0, 25, 417, 23);
		topPanel.add(secondLabel);
	}
	
	
	private void initializeBottomPanel() {
		
		bottomPanel = new JPanel();
		bottomPanel.setBounds(10, 70, 417, 79);
		frame.getContentPane().add(bottomPanel);
		bottomPanel.setLayout(null);
		
		codeField = new JTextField();
		codeField.setBounds(10, 11, 397, 20);
		bottomPanel.add(codeField);
		codeField.setColumns(10);
		
		btnVerifica = new JButton("Verify");
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
		btnVerifica.setBounds(318, 42, 89, 26);
		bottomPanel.add(btnVerifica);
		
		btnIndietro = new JButton("Back");
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
		btnIndietro.setBounds(219, 42, 89, 26);
		bottomPanel.add(btnIndietro);
		
	}
	
	public void setVisible(boolean b) {
		this.frame.setVisible(b);
	}
	
	
	

}
