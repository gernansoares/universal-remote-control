package br.com.urc.config.ssl;

import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

public class GenericTrustManager implements X509TrustManager {

    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    public void checkClientTrusted(X509Certificate[] certs, String authType) {
    }

    public void checkServerTrusted(X509Certificate[] certs, String authType) {
    }

}
