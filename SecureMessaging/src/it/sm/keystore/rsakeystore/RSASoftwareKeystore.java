package it.sm.keystore.rsakeystore;

import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;

import javax.crypto.Cipher;

/* Classe che si occupa di astrarre un keystore Software di tipo RSA.
 * Fornisce metodi per effettuare cifratura, de-cifratura e firma
 * utilizzando la chiave privata contenuta nel kesytore.
 * 
 */
public class RSASoftwareKeystore implements MyRSAKeystore{
	
	//Path al quale � possibile trovare il keystore file
	private String keystorePath;
	
	//Alias del keystore (nome)
	private String alias;
	
	//Password per l'accesso al keystore
	private String password;
	

	public RSASoftwareKeystore(String path, String alias, String password){
		this.keystorePath = path;
		this.alias = alias;
		this.password = password;
	}
	
	
	
	/* Procedura di cifratura.
	 * Prende in input i bytes da cifrare, e restituisce i bytes cifrati.
	 */
	@Override
	public byte [] encrypt(byte[] bytesToEncrypt) throws Exception{
		Cipher c = Cipher.getInstance("RSA");
		c.init(Cipher.ENCRYPT_MODE, extractPrivateKey());
		return c.doFinal(bytesToEncrypt);
	}
	
	/* Procedura di de-cifratura.
	 * Prende in input i bytes da decifrare, e restituisce i bytes decifrati.
	 */
	@Override
	public byte[] decrypt(byte [] bytesToDecrypt) throws Exception {
		
		Cipher c = Cipher.getInstance("RSA");
		c.init(Cipher.DECRYPT_MODE, extractPrivateKey());
		return c.doFinal(bytesToDecrypt);
	}

	
	/* Procedura di firma.
	 * Prende in input i bytes da firmare e l'algoritmo da utilizzare, e restituisce
	 * i bytes della firma.
	 * Nota: accetta soltanto MD5 come algoritmo.
	 */
	@Override
	public byte[] sign(byte[] messageBytes, String alg) throws Exception{
		
		String sigAlgorithm = alg+"withRSA";
			
			Signature sig = Signature.getInstance(sigAlgorithm);
			sig.initSign(extractPrivateKey());
			sig.update(messageBytes);
			
			return sig.sign();
		}
		
	
	
	/* Procedura chiamata per estrarre la chiave privata dal Keystore
	 * ogniqualvolta ce ne fosse bisogno, in modo da non dover
	 * salvare la PrivateKey come variabile membro.
	 */
	public PrivateKey extractPrivateKey() throws Exception {
		
		FileInputStream fis = new FileInputStream(keystorePath);
		
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
	    ks.load(fis, password.toCharArray());

	    Key key = ks.getKey(alias, password.toCharArray());
	    
	    return (PrivateKey)key;
		
	}
	

}
