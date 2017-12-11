package it.sm.keystore.rsakeystore;

import it.utility.DatabaseUtility;

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

						instance = new RSADevice(".\\secure_place\\app_keystore.keystore", "secure_messaging",
								"changeit");

					}
				}
			}
		}
		return instance;

	}

}
