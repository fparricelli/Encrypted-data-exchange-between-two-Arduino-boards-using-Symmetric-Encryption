package it.sm.keystore.rsakeystore;

import java.security.PrivateKey;

import it.sm.keystore.MyKeystore;

/* Keystore RSA: oltre alle funzioni base di cifratura/decifratura deve consentire
 * la possibilit� di calcolare la firma di un messaggio usando la chiave privata
 * contenuta nel keystore.
 */

public interface MyRSAKeystore extends MyKeystore{
	
	public byte [] sign(byte [] messageBytes, String alg) throws Exception;
	public PrivateKey extractPrivateKey() throws Exception;

}
