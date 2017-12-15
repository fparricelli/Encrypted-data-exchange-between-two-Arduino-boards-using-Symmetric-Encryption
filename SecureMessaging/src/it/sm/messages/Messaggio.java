package it.sm.messages;

import java.io.Serializable;

public class Messaggio implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int senderPort;
	private String sender;
	private String msg;
	private String msg_key;
	
	public Messaggio(int s, String sn, String m) {
		this.senderPort = s;
		this.sender = sn;
		this.msg = m;
	}
	
	public Messaggio(int s, String sn, String m, String k) {
		this.senderPort = s;
		this.sender = sn;
		this.msg = m;
		this.msg_key = k;
	}
	
	public String getMsg_key() {
		return msg_key;
	}

	public void setMsg_key(String msg_key) {
		this.msg_key = msg_key;
	}

	public Messaggio(String m) {
		this.msg = m;
	}

	public int getSenderPort() {
		return senderPort;
	}

	public void setSenderPort(int senderPort) {
		this.senderPort = senderPort;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	

}
