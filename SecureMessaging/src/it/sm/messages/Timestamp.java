package it.sm.messages;

import java.io.Serializable;
import java.util.Date;

public class Timestamp implements Serializable{
	
	private Date timestamp;

	public Timestamp(Date timestamp) {
		super();
		this.timestamp = timestamp;
	}
	
	public Date getTimestamp() {
		return this.timestamp;
	}

}
