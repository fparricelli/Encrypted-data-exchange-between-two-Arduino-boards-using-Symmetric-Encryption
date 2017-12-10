package it.sm.keystore.aeskeystore;

import it.sm.messages.EncryptedMessage;

import it.sm.exception.Base64EncodedError;
import it.sm.exception.OutOfBoundEncrypt;

/* Keystore AES: oltre alle funzioni base di cifratura/decifratura,
 * deve necessariamente fornire operazioni di estrazione (per poter scambiare la propria chiave AES
 * con l'interlocutore) e per iniettare la chiave AES (in modo da settare la propria chiave AES in base
 * a quella che � stata concordata con l'interlocutore).
 * 
 */

public interface MyAESKeystore{
	
	public EncryptedMessage encrypt(String message) throws OutOfBoundEncrypt;
	public String decrypt(String message,  String msg_key) throws Base64EncodedError;
	public String requireTokenToShare();
	public boolean setTokenShared(String token);

}
