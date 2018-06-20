package net.ossrs.rtmp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * @author fkrauthan
 */
public class TLSSocketFactory extends SSLSocketFactory {

  private SSLSocketFactory internalSSLSocketFactory;

  public TLSSocketFactory() throws KeyManagementException, NoSuchAlgorithmException {
    SSLContext context = SSLContext.getInstance("TLSv1.2");

    final TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
              @Override
              public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
              }

              @Override
              public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
              }

              @Override
              public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
              }
            }
    };
    context.init(null, trustAllCerts, new java.security.SecureRandom());
    internalSSLSocketFactory = context.getSocketFactory();
  }

  @Override
  public String[] getDefaultCipherSuites() {
    return internalSSLSocketFactory.getDefaultCipherSuites();
  }

  @Override
  public String[] getSupportedCipherSuites() {
    return internalSSLSocketFactory.getSupportedCipherSuites();
  }

  @Override
  public Socket createSocket() throws IOException {
    return enableTLSOnSocket(internalSSLSocketFactory.createSocket());
  }

  @Override
  public Socket createSocket(Socket s, String host, int port, boolean autoClose)
      throws IOException {
    return enableTLSOnSocket(internalSSLSocketFactory.createSocket(s, host, port, autoClose));
  }

  @Override
  public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
    return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port));
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
      throws IOException, UnknownHostException {
    return enableTLSOnSocket(
        internalSSLSocketFactory.createSocket(host, port, localHost, localPort));
  }

  @Override
  public Socket createSocket(InetAddress host, int port) throws IOException {
    return enableTLSOnSocket(internalSSLSocketFactory.createSocket(host, port));
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
      throws IOException {
    return enableTLSOnSocket(
        internalSSLSocketFactory.createSocket(address, port, localAddress, localPort));
  }

  private Socket enableTLSOnSocket(Socket socket) {
    if (socket != null && (socket instanceof SSLSocket)) {
      ((SSLSocket) socket).setEnabledProtocols(new String[] { "TLSv1.1", "TLSv1.2",});
      String[] supportedCipherSuites = ((SSLSocket) socket).getSupportedCipherSuites();
      String[] supportedCipherSuitesFactory = internalSSLSocketFactory.getSupportedCipherSuites();
      String[] supportedProtocolsFactory = ((SSLSocket) socket).getEnabledProtocols();
      System.out.println(Arrays.toString(supportedProtocolsFactory));
      System.out.println(Arrays.toString(supportedCipherSuitesFactory));
      System.out.println(supportedCipherSuites.length);
//      ((SSLSocket) socket).setEnabledCipherSuites(new String[] { "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256" });
      ((SSLSocket) socket).setEnabledCipherSuites(new String[] { "TLS_RSA_WITH_NULL_SHA256" });
    }
    return socket;
  }
}
