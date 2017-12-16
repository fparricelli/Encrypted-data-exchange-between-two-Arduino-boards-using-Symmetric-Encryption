package it.sm.keystore.rsakeystore;

import it.utility.database.DatabaseUtility;

public class RSADevice extends RSASoftwareKeystore {
	private static RSADevice instance;

	private RSADevice(String path, String alias, String password) {
		super(path, alias, password);
	}

	public static RSADevice getInstance() {
		if (instance == null) {
			synchronized (RSADevice.class) {
				RSADevice inst = instance;
				if (inst == null) {
					synchronized (DatabaseUtility.class) {
						
						String current;
						if(!System.getProperty("os.name").toLowerCase().contains("mac")) {
							current = new String(System.getenv("SECURE_MESSAGING_HOME"));
							current = current.concat("\\secure_place\\app_keystore.keystore");
						}
						else
							current = "/Users/francescoparricelli/Documents/Laurea_Magistrale/Secure_System_Design/Progetto-Finale-SSD/CertificateServer/secure_place/app_keystore.keystore";
						instance = new RSADevice(current, "secure_messaging", "changeit");

					}

				}
			}
		}

		return instance;

	}

}
