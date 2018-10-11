/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ne20.user.monitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import net.sourceforge.yamlbeans.YamlException;
import net.sourceforge.yamlbeans.YamlReader;
import net.sourceforge.yamlbeans.YamlWriter;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;

/**
 *
 * @author edsongley
 */
public class NE20Server extends Thread {

    private boolean sync = false;
    private NE20Info info;
    private ArrayList<NE20UserInfo> users;
    private CommunityTarget target = new CommunityTarget();
    private long last_server_read = 0;

    public NE20Server(NE20Info info) {
        this.info = info;
        users = new ArrayList<>();

        //ler arquivo em memoria
        readFile();

        //
        target.setCommunity(new OctetString(info.snmpv2comm));
        target.setAddress(GenericAddress.parse("udp:" + info.ip + "/161")); // supply your own IP and port
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);

    }

    public void readFile() {
        try {
            File f = new File(".user" + info.ip + ".yml");
            if (!f.exists()) {
                f.createNewFile();
            }
        } catch (Exception err) {

        }

        try {
            YamlReader reader = new YamlReader(new FileReader(".user" + info.ip + ".yml"));
            while (true) {
                NE20UserInfo ui = reader.read(NE20UserInfo.class);
                if (ui == null) {
                    break;
                }
                users.add(ui);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (YamlException ex) {
            Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void readServer() {
        try {
            sync = true;
            ArrayList<NE20UserInfo> temp = new ArrayList<>();

            Map<String, String> result = this.doWalk(".1.3.6.1.4.1.2011.5.2.1.15.1.3", target); // ifTable, mib-2 interfaces
            if (result.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "Problema ao comunicar com o equipamento, favor verificar as configurações SNMP na aplicação e em seu NE20.",
                        "Erro de Comunicação",
                        JOptionPane.ERROR_MESSAGE);
                sync = false;
                Monitor.tabbedPane.removeAll();
                
            } else {
                for (Map.Entry<String, String> entry : result.entrySet()) {
                    String ui_l = entry.getValue().substring(0, entry.getValue().indexOf("@"));
                    int ui_i = Integer.parseInt(entry.getKey().replace(".1.3.6.1.4.1.2011.5.2.1.15.1.3.", ""));
                    temp.add(new NE20UserInfo(ui_l, ui_i));

                    System.out.println("Lido: login=" + entry.getValue() + " consideramdo apenas = " + ui_l);
                }
                users = temp;

                try {
                    YamlWriter writer = new YamlWriter(new FileWriter(".user" + info.ip + ".yml"));
                    for (int i = 0; i < users.size(); i++) {
                        writer.write(users.get(i));
                    }
                    writer.close();
                    last_server_read = java.lang.System.currentTimeMillis() / 1000;
                } catch (YamlException ex) {
                    Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
                }
                sync = false;
            }
        } catch (IOException ex) {

            //Logger.getLogger(NE20Server.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public boolean hasData(String login) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).login.equals(login) && users.get(i).ne20id > 0) {
                if (compareOIDLogin(users.get(i))) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isSync() {
        return this.sync;
    }

    public boolean compareOIDLogin(NE20UserInfo user) {
        try {
            return this.doGet(".1.3.6.1.4.1.2011.5.2.1.15.1.3." + user.ne20id, target).startsWith(user.login + "@");
        } catch (IOException ex) {
            Logger.getLogger(NE20Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public NE20UserInfo NE20UserInfo(String login) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).login.equals(login) && users.get(i).ne20id > 0) {
                return users.get(i);
            }
        }

        return null;
    }

    public void run() {
        try {
            while (true) {

                long now = java.lang.System.currentTimeMillis() / 1000;
                if (now - last_server_read > 300) {
                    readServer();
                    last_server_read = java.lang.System.currentTimeMillis() / 1000;
                }

                sleep(1000);

            }
        } catch (InterruptedException ex) {
            Logger.getLogger(NE20Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public NE20UserTraff doUserGet(NE20UserInfo user) {
        try {
            TransportMapping<? extends Address> transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);
            transport.listen();

            PDU pdu = new PDU();
            OID[] oids = new OID[]{
                new OID(".1.3.6.1.4.1.2011.5.2.1.15.1.36." + user.ne20id), //hwAccessUpFlow64
                new OID(".1.3.6.1.4.1.2011.5.2.1.15.1.70." + user.ne20id), //hwAccessIPv6UpFlow64
                new OID(".1.3.6.1.4.1.2011.5.2.1.15.1.37." + user.ne20id), //hwAccessDnFlow64
                new OID(".1.3.6.1.4.1.2011.5.2.1.15.1.71." + user.ne20id), //hwAccessIPv6DnFlow64
                new OID(".1.3.6.1.4.1.2011.5.2.1.15.1.56." + user.ne20id), //hwAccessQosProfile
                new OID(".1.3.6.1.4.1.2011.5.2.1.15.1.17." + user.ne20id), //hwAccessMACAddress
                new OID(".1.3.6.1.4.1.2011.5.2.1.15.1.15." + user.ne20id), //hwAccessIPAddress
                new OID(".1.3.6.1.4.1.2011.5.2.1.15.1.25." + user.ne20id), //hwAccessStartTime
                new OID(".1.3.6.1.4.1.2011.5.2.1.15.1.59." + user.ne20id), //hwAccessIPv6WanAddress
                new OID(".1.3.6.1.4.1.2011.5.2.1.15.1.60." + user.ne20id), //hwAccessIPv6WanPrefix
                new OID(".1.3.6.1.4.1.2011.5.2.1.15.1.61." + user.ne20id), //hwAccessIPv6LanPrefix
                new OID(".1.3.6.1.4.1.2011.5.2.1.15.1.11." + user.ne20id) //hwAccessVLANID
            };
            for (OID oid : oids) {
                pdu.add(new VariableBinding(oid));
            }
            pdu.setType(PDU.GET);
            ResponseEvent event = snmp.send(pdu, target, null);

            //System.out.println("Returning "+tableOid+" = "+event.getResponse().get(0).getVariable().toString());
            return new NE20UserTraff(event.getResponse());
        } catch (IOException ex) {
            Logger.getLogger(NE20Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String doGet(String tableOid, Target target) throws IOException {
        TransportMapping<? extends Address> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        PDU pdu = new PDU();
        OID oid = new OID(tableOid);
        pdu.add(new VariableBinding(oid));
        pdu.setType(PDU.GET);
        ResponseEvent event = snmp.send(pdu, target, null);

        //System.out.println("Returning "+tableOid+" = "+event.getResponse().get(0).getVariable().toString());
        return event.getResponse().get(0).getVariable().toString();
    }

    public static Map<String, String> doWalk(String tableOid, Target target) throws IOException {
        Map<String, String> result = new TreeMap<>();
        TransportMapping<? extends Address> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());

        java.util.List events = treeUtils.getSubtree(target, new OID(tableOid));
        if (events == null || events.size() == 0) {
            System.out.println("Error: Unable to read table...");
            return result;
        }

        for (int i = 0; i < events.size(); i++) {
            TreeEvent event = (TreeEvent) (events.get(i));
            if (event == null) {
                continue;
            }
            if (event.isError()) {
                System.out.println("Error: table OID [" + tableOid + "] " + event.getErrorMessage());
                continue;
            }

            VariableBinding[] varBindings = event.getVariableBindings();
            if (varBindings == null || varBindings.length == 0) {
                continue;
            }
            for (VariableBinding varBinding : varBindings) {
                if (varBinding == null) {
                    continue;
                }

                result.put("." + varBinding.getOid().toString(), varBinding.getVariable().toString());
            }

        }
        snmp.close();
        return result;
    }

}
