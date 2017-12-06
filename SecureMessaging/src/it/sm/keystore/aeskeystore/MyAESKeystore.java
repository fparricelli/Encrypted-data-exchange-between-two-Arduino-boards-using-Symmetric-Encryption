package it.sm.keystore.aeskeystore;

import javax.crypto.SecretKey;

import it.sm.exception.Base64EncodedError;
import it.sm.exception.OutOfBoundEncrypt;

/* Keystore AES: oltre alle funzioni base di cifratura/decifratura,
 * deve necessariamente fornire operazioni di estrazione (per poter scambiare la propria chiave AES
 * con l'interlocutore) e per iniettare la chiave AES (in modo da settare la propria chiave AES in base
 * a quella che � stata concordata con l'interlocutore).
 * 
 */

public interface MyAESKeystore{
	
	public String encrypt(String message) throws OutOfBoundEncrypt;
	public String decrypt(String message) throws Base64EncodedError;
	public SecretKey getSecretKey();
	public void injectSecretKey(SecretKey s);

}
