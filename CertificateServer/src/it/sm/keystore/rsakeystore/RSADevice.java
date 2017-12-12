package it.sm.keystore.rsakeystore;

import java.io.IOException;

import it.utility.database.DatabaseUtility;

public class RSADevice extends RSASoftwareKeystore {
	private static RSADevice instance;

	private RSADevice(String path, String alias, String password) {
		super(path, alias, password);
		// TODO Auto-generated constructor stub
	}

	public static RSADevice getInstance() {
		if (instance == null) {
			synchronized (RSADevice.class) {
				RSADevice inst = instance;
				if (inst == null) {
					synchronized (DatabaseUtility.class) {
						
							String current = new String(System.getenv("SECURE_MESSAGING_HOME"));
							current = current.concat("\\secure_place\\app_keystore.keystore");
							instance = new RSADevice(current, "secure_messaging",
									"changeit");
										
							
							// TODO Auto-generated catch block
						}
						

					}
				}
			}
		
		return instance;

	}

}
