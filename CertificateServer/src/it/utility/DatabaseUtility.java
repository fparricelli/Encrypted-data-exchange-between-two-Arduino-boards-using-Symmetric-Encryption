package it.utility;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class DatabaseUtility {
	public static void main(String[] args) {
		try {
			generatePasswordFile();
			System.out.println("Algoritmo terminato");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void generatePasswordFile () throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	{
		 KeyGenerator keygen = KeyGenerator.getInstance("AES");
		 SecretKey aesKey = keygen.generateKey();
		 Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		 aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
		 byte[] cleartext = "ssluser".getBytes();
		 byte[] cleartext2 = "sslpassword".getBytes();
		 byte[] ciphertext = aesCipher.doFinal(cleartext);
		 byte[] ciphertext2 = aesCipher.doFinal(cleartext2);
		 aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
		 cleartext = aesCipher.doFinal(ciphertext);
		 cleartext2 = aesCipher.doFinal(ciphertext2);
		 System.out.println(ciphertext);
		 System.out.println(ciphertext2);
		 String stringa = new String(cleartext);
		 String stringa2 = new String(cleartext2);
		 System.out.println(cleartext);
		 System.out.println(cleartext2);
	}
	
	
	

}
