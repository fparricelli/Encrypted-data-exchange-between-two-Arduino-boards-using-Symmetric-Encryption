package it.chat.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

class ReloadableX509TrustManager implements X509TrustManager {

private final String trustStorePath;
private X509TrustManager trustManager;
private final String certDownloadPath;

	public ReloadableX509TrustManager(String tspath,String certDownloadPath) throws Exception {
		this.trustStorePath = tspath;
		this.certDownloadPath = certDownloadPath;
		reloadTrustManager();
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		System.out.println("[Controllo Client Certificate..]");
		try {
			trustManager.checkServerTrusted(chain, authType);
		} catch (CertificateException cx) {
			
			System.out.println("[Controllo Client - CertificateException]");
			addCertAndReload(chain[0], true);
			trustManager.checkServerTrusted(chain, authType);
		}
		
		System.out.println("[Controllo Client Certificate] Terminato.");
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		System.out.println("[Controllo Server Certificate..]");
		try {
			trustManager.checkServerTrusted(chain, authType);
		} catch (CertificateException cx) {
			
			System.out.println("[Controllo Server - CertificateException]");
			addCertAndReload(chain[0], true);
			trustManager.checkServerTrusted(chain, authType);
		}
		
		System.out.println("[Controllo Server Certificate] Terminato.");
		
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		X509Certificate[] issuers = trustManager.getAcceptedIssuers();
		return issuers;
	}

	
	private void reloadTrustManager() throws Exception {

		
		KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
		InputStream in = new FileInputStream(trustStorePath);
		try { ts.load(in, null); }
		finally { in.close(); }

		TrustManagerFactory tmf = TrustManagerFactory.getInstance(
        TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ts);

		
		TrustManager tms[] = tmf.getTrustManagers();
		for (int i = 0; i < tms.length; i++) {
			if (tms[i] instanceof X509TrustManager) {
				trustManager = (X509TrustManager)tms[i];
				return;
			}
		}

		throw new NoSuchAlgorithmException("No X509TrustManager in TrustManagerFactory");
	}
	
	

	private void addCertAndReload(Certificate actualCert, boolean permanent) {
		
		X509Certificate actualCert509 = (X509Certificate) actualCert;
		int sNumActualCert = actualCert509.getSerialNumber().intValue();
		String aliasActualCert = getIdentity(actualCert)[0]+getIdentity(actualCert)[1]+"_cer_"+sNumActualCert;
		
		try {
			
				//NOTA: prima di inserire un certificato nel trust-store, andrebbe verificata la validità
				//Di tale certificato controllando verificandone la firma della CA.
				//Essendo i certificati self-signed, è superfluo effettuare un controllo in questo caso
				//Ma in uno scenario reale, andrebbe fatto.
				
				
				Date actualDate = new Date();
				//Controllo: se il certificato che voglio inserire è scaduto, allora lo scarico dal server
				if(actualDate.after(actualCert509.getNotAfter())) {
					
					ServerHelper sh = new ServerHelper();
					//Chiedo il nuovo certificato al server
					File fNewCert = sh.getCertificate(getIdentity(actualCert)[0], getIdentity(actualCert)[1], certDownloadPath);
					Certificate newCert = getCertificateFromFile(fNewCert);
					
					//Una volta ottenuto l'oggetto Certificate, posso cancellare il file
					fNewCert.delete();
					CertificateHelper ch = CertificateHelper.getInstance();
					//Rimuovo il certificato scaduto dal truststore
					ch.removeCertificate(aliasActualCert);
					
					X509Certificate newCert509 = (X509Certificate) newCert;
					int sNumNewCert = newCert509.getSerialNumber().intValue();
					String aliasNewCert = getIdentity(newCert)[0]+getIdentity(newCert)[1]+"_cer_"+sNumNewCert;
					
					//Aggiungo il certificato che ho recuperato e ricarico il truststore
					ch.addCertificateFromCert(newCert, aliasNewCert);
					reloadTrustManager();
				
				}else{
					
					//Il certificato che voglio inserire è valido, quindi lo metto
					//Nel trust-store e lo ricarico
					CertificateHelper ch = CertificateHelper.getInstance();
					ch.addCertificateFromCert(actualCert, aliasActualCert);
					reloadTrustManager();
				}
				
				
		}catch (Exception e){
		e.printStackTrace();
		
		}
		
	}
	
	private String [] getIdentity(Certificate cert) {
		
	try {
		X509Certificate cer = (X509Certificate) cert;
		X500Name x500name = new JcaX509CertificateHolder(cer).getSubject();
		RDN cn = x500name.getRDNs(BCStyle.CN)[0];

		String identity = IETFUtils.valueToString(cn.getFirst().getValue()).toLowerCase();
		
		String [] p = identity.split(" ");
		return new String [] {p[0], p[1]};
		
		}catch(Exception e) {
			e.printStackTrace();
		}
	return null;
		
	}
	
	private Certificate getCertificateFromFile(File c) {
		Certificate cert = null;
		
		try {
			CertificateFactory fact = CertificateFactory.getInstance("X.509");
			FileInputStream fis = new FileInputStream (c);
			cert = (X509Certificate) fact.generateCertificate(fis);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return cert;

	}
	
	
}