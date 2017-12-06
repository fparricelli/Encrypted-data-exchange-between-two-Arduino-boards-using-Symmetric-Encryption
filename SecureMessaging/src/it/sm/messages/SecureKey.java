package it.sm.messages;

import java.io.Serializable;


/* Classe che si occupa di mantenere tutti i dati
 * relativi alla chiave AES 'sicura': in particolare incapsula la 
 * chiave AES cifrata (encryptedKey) e la sua firma (keySignature),
 * in più conserva anche un campo from che indica l'identità del mittente.
 * Gli oggetti di questa classe verranno scambiati durante l'handshake.
 */
public class SecureKey implements Serializable{
	

	

	private static final long serialVersionUID = 1L;
	
	
	private byte [] encryptedKey;
	private byte [] keySignature;
	private String from;
	
	public SecureKey() {
		super();
	}
	
	public SecureKey(byte [] ek, byte [] ks) {
		this.encryptedKey = ek;
		this.keySignature = ks;
	}
	
	public byte[] getEncryptedKey() {
		return encryptedKey;
	}

	public void setEncryptedKey(byte[] encryptedMessage) {
		this.encryptedKey = encryptedMessage;
	}

	public byte[] getKeySignature() {
		return keySignature;
	}

	public void setKeySignature(byte[] messageSignature) {
		this.keySignature = messageSignature;
	}
	
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}
	

}
