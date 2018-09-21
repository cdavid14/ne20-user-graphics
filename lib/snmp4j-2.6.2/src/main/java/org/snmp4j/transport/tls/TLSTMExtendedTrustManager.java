/*_############################################################################
  _##
  _##  SNMP4J 2 - TLSTMExtendedTrustManager.java
  _##
  _##  Copyright (C) 2003-2017  Frank Fock and Jochen Katz (SNMP4J.org)
  _##
  _##  Licensed under the Apache License, Version 2.0 (the "License");
  _##  you may not use this file except in compliance with the License.
  _##  You may obtain a copy of the License at
  _##
  _##      http://www.apache.org/licenses/LICENSE-2.0
  _##
  _##  Unless required by applicable law or agreed to in writing, software
  _##  distributed under the License is distributed on an "AS IS" BASIS,
  _##  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  _##  See the License for the specific language governing permissions and
  _##  limitations under the License.
  _##
  _##########################################################################*/

package org.snmp4j.transport.tls;

import org.snmp4j.TransportStateReference;
import org.snmp4j.event.CounterEvent;
import org.snmp4j.log.LogAdapter;
import org.snmp4j.log.LogFactory;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OctetString;
import org.snmp4j.transport.TLSTM;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * TLSTM trust manager that implements the X509ExtendedTrustManager
 * interace.
 * @author Frank Fock
 * @since 2.5.7
 */
public class TLSTMExtendedTrustManager extends X509ExtendedTrustManager {

  private static final LogAdapter logger = LogFactory.getLogger(TLSTMExtendedTrustManager.class);

  X509TrustManager trustManager;
  private boolean useClientMode;
  private TransportStateReference tmStateReference;
  private TLSTM tlstm;

  public TLSTMExtendedTrustManager(TLSTM tlstm, X509TrustManager trustManager,
                                   boolean useClientMode, TransportStateReference tmStateReference) {
    this.tlstm = tlstm;
    this.trustManager = trustManager;
    this.useClientMode = useClientMode;
    this.tmStateReference = tmStateReference;
  }

  @Override
  public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
      throws CertificateException {
    if (checkClientTrustedIntern(x509Certificates)) return;
    try {
      trustManager.checkClientTrusted(x509Certificates, s);
    }
    catch (CertificateException cex) {
      tlstm.getCounterSupport().fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionOpenErrors));
      tlstm.getCounterSupport().fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionInvalidClientCertificates));
      logger.warn("Client certificate validation failed for '"+x509Certificates[0]+"'");
      throw cex;
    }
  }

  @Override
  public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
    if (preCheckServerTrusted(x509Certificates)) return;
    try {
      trustManager.checkServerTrusted(x509Certificates, s);
    }
    catch (CertificateException cex) {
      tlstm.getCounterSupport().fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionOpenErrors));
      tlstm.getCounterSupport().fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionUnknownServerCertificate));
      logger.warn("Server certificate validation failed for '"+x509Certificates[0]+"'");
      throw cex;
    }
    postCheckServerTrusted(x509Certificates);
  }

  private boolean isMatchingFingerprint(X509Certificate[] x509Certificates, OctetString fingerprint) {
    if ((fingerprint != null) && (fingerprint.length() > 0)) {
      for (X509Certificate cert : x509Certificates) {
        OctetString certFingerprint = null;
        certFingerprint = TLSTM.getFingerprint(cert);
        if (logger.isDebugEnabled()) {
          logger.debug("Comparing certificate fingerprint "+certFingerprint+
              " with "+fingerprint);
        }
        if (certFingerprint == null) {
          logger.error("Failed to determine fingerprint for certificate "+cert+
              " and algorithm "+cert.getSigAlgName());
        }
        else if (certFingerprint.equals(fingerprint)) {
          if (logger.isInfoEnabled()) {
            logger.info("Peer is trusted by fingerprint '"+fingerprint+"' of certificate: '"+cert+"'");
          }
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    TlsTmSecurityCallback<X509Certificate> callback = tlstm.getSecurityCallback();
    X509Certificate[] accepted = trustManager.getAcceptedIssuers();
    if ((accepted != null) && (callback != null)) {
      ArrayList<X509Certificate> acceptedIssuers = new ArrayList<X509Certificate>(accepted.length);
      for (X509Certificate cert : accepted) {
        if (callback.isAcceptedIssuer(cert)) {
          acceptedIssuers.add(cert);
        }
      }
      return acceptedIssuers.toArray(new X509Certificate[acceptedIssuers.size()]);
    }
    return accepted;
  }

  @Override
  public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {
    logger.debug("checkClientTrusted with socket");
    if (checkClientTrustedIntern(x509Certificates)) return;
    try {
      if (trustManager instanceof X509ExtendedTrustManager) {
        logger.debug("extended checkClientTrusted with socket");
        ((X509ExtendedTrustManager)trustManager).checkClientTrusted(x509Certificates, s, socket);
      }
      else {
        trustManager.checkClientTrusted(x509Certificates, s);
      }
    }
    catch (CertificateException cex) {
      tlstm.getCounterSupport().fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionOpenErrors));
      tlstm.getCounterSupport().fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionInvalidClientCertificates));
      logger.warn("Client certificate validation failed for '"+x509Certificates[0]+"'");
      throw cex;
    }
  }

  @Override
  public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {
    logger.debug("checkClientTrusted with socket");
    if (preCheckServerTrusted(x509Certificates)) return;
    try {
      if (trustManager instanceof X509ExtendedTrustManager) {
        logger.debug("extended checkClientTrusted with socket");
        ((X509ExtendedTrustManager)trustManager).checkServerTrusted(x509Certificates, s, socket);
      }
      else {
        trustManager.checkServerTrusted(x509Certificates, s);
      }
    }
    catch (CertificateException cex) {
      tlstm.getCounterSupport().fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionOpenErrors));
      tlstm.getCounterSupport().fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionUnknownServerCertificate));
      logger.warn("Server certificate validation failed for '"+x509Certificates[0]+"'");
      throw cex;
    }
    postCheckServerTrusted(x509Certificates);
  }

  private void postCheckServerTrusted(X509Certificate[] x509Certificates) throws CertificateException {
    TlsTmSecurityCallback<X509Certificate> callback = tlstm.getSecurityCallback();
    if (useClientMode && (callback != null)) {
      if (!callback.isServerCertificateAccepted(x509Certificates)) {
        logger.info("Server is NOT trusted with certificate '"+ Arrays.asList(x509Certificates)+"'");
        throw new CertificateException("Server's certificate is not trusted by this application (although it was trusted by the JRE): "+
        Arrays.asList(x509Certificates));
      }
    }
  }

  private boolean preCheckServerTrusted(X509Certificate[] x509Certificates) {
    if (tmStateReference.getCertifiedIdentity() != null) {
      OctetString fingerprint = tmStateReference.getCertifiedIdentity().getServerFingerprint();
      if (isMatchingFingerprint(x509Certificates, fingerprint)) return true;
    }
    Object entry = null;
    try {
      entry = TLSTM.getSubjAltName(x509Certificates[0].getSubjectAlternativeNames(), 2);
    } catch (CertificateParsingException e) {
      logger.error("CertificateParsingException while verifying server certificate "+
          Arrays.asList(x509Certificates));
    }
    if (entry == null) {
      X500Principal x500Principal = x509Certificates[0].getSubjectX500Principal();
      if (x500Principal != null) {
        entry = x500Principal.getName();
      }
    }
    if (entry != null) {
      String dNSName = ((String)entry).toLowerCase();
      String hostName = ((IpAddress)tmStateReference.getAddress())
          .getInetAddress().getCanonicalHostName();
      if ((dNSName != null) && (dNSName.length() > 0)) {
        if (dNSName.charAt(0) == '*') {
          int pos = hostName.indexOf('.');
          hostName = hostName.substring(pos);
          dNSName = dNSName.substring(1);
        }
        if (hostName.equalsIgnoreCase(dNSName)) {
          if (logger.isInfoEnabled()) {
            logger.info("Peer hostname "+hostName+" matches dNSName "+dNSName);
          }
          return true;
        }
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Peer hostname "+hostName+" did not match dNSName "+dNSName);
      }
    }
    return false;
  }

  @Override
  public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {
    logger.debug("checkClientTrusted with sslEngine");
    if (checkClientTrustedIntern(x509Certificates)) return;
    try {
      if (trustManager instanceof X509ExtendedTrustManager) {
        logger.debug("extended checkClientTrusted with sslEngine");
        ((X509ExtendedTrustManager)trustManager).checkClientTrusted(x509Certificates, s, sslEngine);
      }
      else {
        trustManager.checkClientTrusted(x509Certificates, s);
      }
    }
    catch (CertificateException cex) {
      tlstm.getCounterSupport().fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionOpenErrors));
      tlstm.getCounterSupport().fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionInvalidClientCertificates));
      logger.warn("Client certificate validation failed for '"+x509Certificates[0]+"'");
      throw cex;
    }
  }

  private boolean checkClientTrustedIntern(X509Certificate[] x509Certificates) {
    if ((tmStateReference != null) && (tmStateReference.getCertifiedIdentity() != null)) {
      OctetString fingerprint = tmStateReference.getCertifiedIdentity().getClientFingerprint();
      if (isMatchingFingerprint(x509Certificates, fingerprint)) {
        return true;
      }
    }
    TlsTmSecurityCallback<X509Certificate> callback = tlstm.getSecurityCallback();
    if (!useClientMode && (callback != null)) {
      if (callback.isClientCertificateAccepted(x509Certificates[0])) {
        if (logger.isInfoEnabled()) {
          logger.info("Client is trusted with certificate '"+x509Certificates[0]+"'");
        }
        return true;
      }
    }
    return false;
  }

  @Override
  public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {
    logger.debug("checkServerTrusted with sslEngine");
    if (preCheckServerTrusted(x509Certificates)) return;
    try {
      if (trustManager instanceof X509ExtendedTrustManager) {
        logger.debug("extended checkServerTrusted with sslEngine");
        ((X509ExtendedTrustManager)trustManager).checkServerTrusted(x509Certificates, s, sslEngine);
      }
      else {
        trustManager.checkServerTrusted(x509Certificates, s);
      }
    }
    catch (CertificateException cex) {
      tlstm.getCounterSupport().fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionOpenErrors));
      tlstm.getCounterSupport().fireIncrementCounter(new CounterEvent(this, SnmpConstants.snmpTlstmSessionUnknownServerCertificate));
      logger.warn("Server certificate validation failed for '"+x509Certificates[0]+"'");
      throw cex;
    }
    postCheckServerTrusted(x509Certificates);
  }

}
