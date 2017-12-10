package it.sm.messages;

import java.io.Serializable;


//Classe che incapsula il messaggio cifrato con la chiave AES.

public class EncryptedMessage implements Serializable{
	
	
	private static final long serialVersionUID = 1L;
	private String msg_key;
	private String encryptedMessage;
	public String getMsg_key() {
		return msg_key;
	}
	public void setMsg_key(String msg_key) {
		this.msg_key = msg_key;
	}
	public String getEncryptedMessage() {
		return encryptedMessage;
	}
	public void setEncryptedMessage(String encryptedMessage) {
		this.encryptedMessage = encryptedMessage;
	}
	public EncryptedMessage(String msg_key, String encryptedMessage) {
		super();
		this.msg_key = msg_key;
		this.encryptedMessage = encryptedMessage;
	}
	
	
}
