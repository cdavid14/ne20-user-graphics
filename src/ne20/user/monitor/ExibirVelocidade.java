/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ne20.user.monitor;

import java.awt.Cursor;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
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
 * @author Suporte10
 */
public class ExibirVelocidade extends Thread {

    int clientOID = -1;
    CommunityTarget target = new CommunityTarget();
    boolean testSTOP = true;

    JButton INICIAR,
            PARAR;
    JLabel texto;
    String ip,
           community;
    JTextField usuario;
    long lastUP,
            lastDOWN;
    JPanel jpanel;
    JProgressBar loading;

    public ExibirVelocidade(JButton INICIAR, JButton PARAR, JLabel texto, JTextField usuario, JPanel jpanel, JProgressBar loading, String ip, String community) {
        this.texto = texto;
        this.ip = ip;
        this.INICIAR = INICIAR;
        this.PARAR = PARAR;
        this.community = community;
        this.usuario = usuario;
        this.jpanel = jpanel;
        this.loading = loading;
    }

    public void run() {
        try {
            this.testSTOP = false;
            this.PARAR.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    pararClicado(evt);
                }
            });

            this.target = new CommunityTarget();
            this.target.setCommunity(new OctetString(this.community));
            this.target.setAddress(GenericAddress.parse("udp:" + this.ip + "/161")); // supply your own IP and port
            this.target.setRetries(2);
            this.target.setTimeout(1500);
            this.target.setVersion(SnmpConstants.version2c);

            //Search user OID
            Map<String, String> result = this.doWalk(".1.3.6.1.4.1.2011.5.2.1.15.1.3", target); // ifTable, mib-2 interfaces
            this.clientOID = -1;
            
            for (Map.Entry<String, String> entry : result.entrySet()) {
                if (entry.getValue().equals(this.usuario.getText() + "@vivatelecom")) {
                    clientOID = Integer.parseInt(entry.getKey().replace(".1.3.6.1.4.1.2011.5.2.1.15.1.3.", ""));
                    break;
                }
            }
            this.jpanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            this.PARAR.setEnabled(true);
            this.loading.setVisible(false);
            //Bring user information
            //
        } catch (Exception err) {
            System.out.println("Merdinha");
            err.printStackTrace();
        }
        if(this.clientOID == -1){
            JOptionPane.showMessageDialog(null, "Usuario Não Encontrado!");
            this.INICIAR.setEnabled(true);
            this.PARAR.setEnabled(false);
            this.usuario.setEditable(true);
            this.texto.setText("Sem Informação");
            this.testSTOP = true;
        }
        while (!this.testSTOP) {
            try {
                long tempUpV4 = Long.parseLong(this.doGet(".1.3.6.1.4.1.2011.5.2.1.15.1.36." + this.clientOID, this.target));
                long tempUpV6 = Long.parseLong(this.doGet(".1.3.6.1.4.1.2011.5.2.1.15.1.70." + this.clientOID, this.target));
                long tempDownV4 = Long.parseLong(this.doGet(".1.3.6.1.4.1.2011.5.2.1.15.1.37." + this.clientOID, this.target));
                long tempDownV6 = Long.parseLong(this.doGet(".1.3.6.1.4.1.2011.5.2.1.15.1.71." + this.clientOID, this.target));

                String textUP = "",
                       textDOWN = "";
                if (this.lastUP != 0) {
                    if(this.lastUP < (tempUpV4 + tempUpV6)){
                        long up = (tempUpV4 + tempUpV6) - this.lastUP;
                        //System.out.println("UP:"+up);
                        this.lastUP = up;
                        String upUnit = "Bps";
                        while(up > 1000){
                            if(upUnit.equals("Bps")) upUnit = "Kbps";
                            else if(upUnit.equals("Kbps")) upUnit = "Mbps";
                            up = up/1000;
                        }
                        textUP = String.format("%d %s", up, upUnit);
                    }
                } else {
                    this.lastUP = tempUpV4 + tempUpV6;
                    textUP = "0.0";
                }

                if (this.lastDOWN != 0) {
                    if(this.lastDOWN < (tempDownV4 + tempDownV6)){
                        long down = (tempDownV4 + tempDownV6) - this.lastDOWN;
                        //System.out.println("DOWN:"+down);
                        this.lastDOWN = down;
                        String downUnit = "Bps";
                        while(down > 1000){
                            if(downUnit.equals("Bps")) downUnit = "Kbps";
                            else if(downUnit.equals("Kbps")) downUnit = "Mbps";
                            down = down/1000;
                        }
                        textDOWN = String.format("%d %s", down, downUnit);
                    }
                } else {
                    this.lastDOWN = tempDownV4 + tempDownV6;
                    textDOWN = "0.0";
                }
                texto.setText(String.format("%s/%s", textUP, textDOWN));
                Thread.sleep(5000);
            } catch (NumberFormatException e){
                JOptionPane.showMessageDialog(null, "Usuario Não Encontrado!");
            } catch (IOException ex) {
                System.out.println("MERDA");
            } catch (InterruptedException ex) {
                Logger.getLogger(ExibirVelocidade.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.texto.setText("Sem Informação");
    }
    
    private void pararClicado(java.awt.event.ActionEvent evt){
        this.INICIAR.setEnabled(true);
        this.PARAR.setEnabled(false);
        this.usuario.setEditable(true);
        this.texto.setText("Sem Informação");
        this.testSTOP = true;
    }
    
    public static String doGet(String tableOid, Target target) throws IOException {
        TransportMapping<? extends Address> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        PDU pdu = new PDU();
        OID oid = new OID(tableOid);
        pdu.add(new VariableBinding(oid));
        pdu.setType(PDU.GET);
        ResponseEvent event = snmp.send(pdu, target, null);

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
