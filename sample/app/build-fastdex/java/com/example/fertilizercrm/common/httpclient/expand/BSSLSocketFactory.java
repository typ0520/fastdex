package com.example.fertilizercrm.common.httpclient.expand;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import com.example.fertilizercrm.common.httpclient.MySSLSocketFactory;

/**
 * This file is introduced to fix HTTPS Post bug on API &lt; ICS see
 * http://code.google.com/p/android/issues/detail?id=13117#c14 <p>&nbsp;</p> Warning! This omits SSL
 * certificate validation on every device, use with caution
 */
public class BSSLSocketFactory extends MySSLSocketFactory {
    
    public BSSLSocketFactory(InputStream keyStream,char[] password) throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
		super(getKeyStore(keyStream, password));
    }
    
    public static KeyStore getKeyStore(InputStream keyStream, char[] password) {
    	KeyStore trustStore = null;
		try {
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		try {
			trustStore.load(keyStream,password);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return trustStore;
	}
}