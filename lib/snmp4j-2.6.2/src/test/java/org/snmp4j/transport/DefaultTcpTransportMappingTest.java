package org.snmp4j.transport;

import junit.framework.TestCase;
import org.junit.Before;
import org.snmp4j.PDU;
import org.snmp4j.TransportMapping;
import org.snmp4j.TransportStateReference;
import org.snmp4j.asn1.BEROutputStream;
import org.snmp4j.log.ConsoleLogAdapter;
import org.snmp4j.log.ConsoleLogFactory;
import org.snmp4j.log.LogFactory;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DefaultTcpTransportMappingTest extends TestCase {

    static {
        LogFactory.setLogFactory(new ConsoleLogFactory());
        ConsoleLogAdapter.setDebugEnabled(true);
    }

    public void testSendMessage() throws Exception {
        DefaultTcpTransportMapping serverTransportMapping = new DefaultTcpTransportMapping();
        final List<OctetString> bytesReceivedList = new ArrayList<OctetString>();
        final Object sync = new Object();
        serverTransportMapping.addTransportListener(new TransportListener() {
            @Override
            public void processMessage(TransportMapping sourceTransport, Address incomingAddress,
                                       ByteBuffer wholeMessage, TransportStateReference tmStateReference) {
                OctetString bytesReceived = new OctetString(wholeMessage.array());
                System.out.println("Received from "+incomingAddress+": "+bytesReceived.toHexString());
                bytesReceivedList.add(bytesReceived);
                synchronized (sync) {
                    sync.notify();
                }
            }
        });
        serverTransportMapping.setServerEnabled(true);
        serverTransportMapping.listen();
        TcpAddress serverAddress = serverTransportMapping.getListenAddress();
        DefaultTcpTransportMapping clientTransportMapping = new DefaultTcpTransportMapping();
        clientTransportMapping.listen();
        TransportStateReference transportStateReference =
                new TransportStateReference(clientTransportMapping, null, null,
                        null, null, false, new Object());
        PDU v2cPDU = new PDU();
        v2cPDU.add(new VariableBinding(new OID(SnmpConstants.sysDescr), new OctetString("hello World")));
        BEROutputStream berOutputStream = new BEROutputStream(ByteBuffer.allocate(v2cPDU.getBERLength()));
        v2cPDU.encodeBER(berOutputStream);
        byte[] bytes2Send = berOutputStream.getBuffer().array();
        synchronized (sync) {
            clientTransportMapping.sendMessage(serverAddress, bytes2Send, transportStateReference);
            sync.wait(2000);
        }
        assertEquals(1, bytesReceivedList.size());
        assertEquals(new OctetString(bytes2Send).toHexString(), bytesReceivedList.get(0).toHexString());
        clientTransportMapping.close();
        serverTransportMapping.close();
    }

    public void testSendMessageAfterReconnect() throws Exception {
        DefaultTcpTransportMapping serverTransportMapping = new DefaultTcpTransportMapping();
        final List<OctetString> bytesReceivedList = new ArrayList<OctetString>();
        final Object sync = new Object();
        serverTransportMapping.addTransportListener(new TransportListener() {
            @Override
            public void processMessage(TransportMapping sourceTransport, Address incomingAddress,
                                       ByteBuffer wholeMessage, TransportStateReference tmStateReference) {
                OctetString bytesReceived = new OctetString(wholeMessage.array());
                System.out.println("Received from "+incomingAddress+": "+bytesReceived.toHexString());
                bytesReceivedList.add(bytesReceived);
                synchronized (sync) {
                    sync.notify();
                }
            }
        });
        serverTransportMapping.setServerEnabled(true);
        serverTransportMapping.listen();

        TcpAddress serverAddress = serverTransportMapping.getListenAddress();
        DefaultTcpTransportMapping clientTransportMapping = new DefaultTcpTransportMapping();
        clientTransportMapping.listen();
        TransportStateReference transportStateReference =
                new TransportStateReference(clientTransportMapping, null, null,
                        null, null, false, new Object());
        PDU v2cPDU = new PDU();
        v2cPDU.add(new VariableBinding(new OID(SnmpConstants.sysDescr), new OctetString("hello World")));
        BEROutputStream berOutputStream = new BEROutputStream(ByteBuffer.allocate(v2cPDU.getBERLength()));
        v2cPDU.encodeBER(berOutputStream);
        byte[] bytes2Send = berOutputStream.getBuffer().array();
        synchronized (sync) {
            clientTransportMapping.sendMessage(serverAddress, bytes2Send, transportStateReference);
            sync.wait(2000);
        }
        assertEquals(1, bytesReceivedList.size());
        assertEquals(new OctetString(bytes2Send).toHexString(), bytesReceivedList.get(0).toHexString());
        serverTransportMapping.close();

        // Second message
        bytesReceivedList.clear();
        serverTransportMapping.tcpAddress.setPort(serverAddress.getPort());
        serverTransportMapping.listen();
        v2cPDU.add(new VariableBinding(SnmpConstants.sysUpTime, new Integer32(1234)));
        berOutputStream = new BEROutputStream(ByteBuffer.allocate(v2cPDU.getBERLength()));
        v2cPDU.encodeBER(berOutputStream);
        bytes2Send = berOutputStream.getBuffer().array();
        synchronized (sync) {
            clientTransportMapping.sendMessage(serverAddress, bytes2Send, transportStateReference);
            sync.wait(2000);
        }
        assertEquals(1, bytesReceivedList.size());
        assertEquals(new OctetString(bytes2Send).toHexString(), bytesReceivedList.get(0).toHexString());
        clientTransportMapping.close();
        serverTransportMapping.close();
    }

}