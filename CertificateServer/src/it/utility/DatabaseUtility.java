package it.utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

public class DatabaseUtility {
	// TODO ATTENZIONE LA CLASSE È DA RENDERE SICURA!!!
	private static DatabaseUtility instance;

	public static DatabaseUtility getInstance() {
		if (instance == null) {
			synchronized (DatabaseUtility.class) {
				DatabaseUtility inst = instance;
				if (inst == null) {
					synchronized (DatabaseUtility.class) {
						try {
							instance = new DatabaseUtility();
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		return instance;
	}

	private DatabaseUtility() throws ClassNotFoundException {
		Class.forName("com.mysql.jdbc.Driver");
	}

	public Connection connect() {
		Connection con = null;
		try {
			String url1 = "jdbc:mysql://localhost:3306/secure_messaging";
			String url2 = "?verifyServerCertificate=true";
			String url3 = "&useSSL=true";
			String url4 = "&requireSSL=true";
			String url = url1 + url2 + url3 + url4;
			String user = "ssluser";
			String password = "sslpassword";
			con = DriverManager.getConnection(url, user, password);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return con;
	}
	


	public DatabaseTriple query(String command) {
		Connection conn = null; 
		PreparedStatement statement = null;
		ResultSet result = null;
		try {
			conn = this.connect();
			statement = conn.prepareStatement(command);
			result = statement.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new DatabaseTriple(conn, statement, result);
	}

	/*
	 * public static void generatePasswordFile () throws NoSuchAlgorithmException,
	 * NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
	 * BadPaddingException { KeyGenerator keygen = KeyGenerator.getInstance("AES");
	 * SecretKey aesKey = keygen.generateKey(); Cipher aesCipher =
	 * Cipher.getInstance("AES/ECB/PKCS5Padding");
	 * aesCipher.init(Cipher.ENCRYPT_MODE, aesKey); byte[] cleartext =
	 * "ssluser".getBytes(); byte[] cleartext2 = "sslpassword".getBytes(); byte[]
	 * ciphertext = aesCipher.doFinal(cleartext); byte[] ciphertext2 =
	 * aesCipher.doFinal(cleartext2); aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
	 * cleartext = aesCipher.doFinal(ciphertext); cleartext2 =
	 * aesCipher.doFinal(ciphertext2); System.out.println(ciphertext);
	 * System.out.println(ciphertext2); String stringa = new String(cleartext);
	 * String stringa2 = new String(cleartext2); System.out.println(cleartext);
	 * System.out.println(cleartext2); }
	 * 
	 */

}
