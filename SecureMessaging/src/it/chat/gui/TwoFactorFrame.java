package it.chat.gui;

import java.awt.Color;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;

import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import it.chat.gui.utility.LookAndFeelUtility;


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

	/**
	 * Launch the application.
	 */
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
	}

	/**
	 * Create the application.
	 */
	public TwoFactorFrame() {
		
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
		frame.setTitle("Verifica Codice Email");
		frame.getContentPane().setBackground(Color.DARK_GRAY);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

	}
	
	
	private void initializeTopPanel() {
		
		topPanel = new JPanel();
		topPanel.setBounds(10, 11, 417, 48);
		frame.getContentPane().add(topPanel);
		topPanel.setLayout(null);
		
		firstLabel = new JLabel("Abbiamo inviato una mail all'indirizzo associato al tuo account.");
		firstLabel.setHorizontalAlignment(SwingConstants.CENTER);
		firstLabel.setBounds(0, 0, 417, 24);
		topPanel.add(firstLabel);
		
		secondLabel = new JLabel("Inserisci il codice presente nel messaggio per completare l'accesso.");
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
		
		btnVerifica = new JButton("Verifica");
		btnVerifica.setBounds(318, 42, 89, 26);
		bottomPanel.add(btnVerifica);
		
		btnIndietro = new JButton("Indietro");
		btnIndietro.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frame.dispose();
			}
		});
		btnIndietro.setBounds(219, 42, 89, 26);
		bottomPanel.add(btnIndietro);
		
	}
	
	
	
	
	

}
