package it.sm.messages;

import java.io.Serializable;


//Classe che incapsula il messaggio cifrato con la chiave AES.

public class EncryptedMessage implements Serializable{
	
	
	private static final long serialVersionUID = 1L;
	private String encryptedMessage;
	
	public EncryptedMessage(String m) {
		this.encryptedMessage = m;
	}

	public String getEncryptedMessage() {
		return encryptedMessage;
	}

	public void setEncryptedMessage(String encryptedMessage) {
		this.encryptedMessage = encryptedMessage;
	}
	
	

}
