package it.chat.helpers;

import java.awt.List;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.*;
import java.util.UUID;

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
private List tempCertList = new List();

	public ReloadableX509TrustManager(String tspath) throws Exception {
		this.trustStorePath = tspath;
		reloadTrustManager();
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		trustManager.checkClientTrusted(chain, authType);
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		try {
			trustManager.checkServerTrusted(chain, authType);
		} catch (CertificateException cx) {
			addServerCertAndReload(chain[0], true);
			trustManager.checkServerTrusted(chain, authType);
		}
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
	
	

	private void addServerCertAndReload(Certificate cert, boolean permanent) {
		try {
			
				
				CertificateHelper ch = CertificateHelper.getInstance();
				String ids = getIdentity(cert)[0]+getIdentity(cert)[1]+"_cer";
				ch.addCertificateFromCert(cert, ids);
				reloadTrustManager();
				
				
		} catch (Exception ex) 
		{ ex.printStackTrace(); }
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
	
	
}