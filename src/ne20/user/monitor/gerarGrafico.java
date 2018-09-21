/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ne20.user.monitor;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.DynamicTimeSeriesCollection;
import org.jfree.data.time.Second;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
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


public class gerarGrafico extends ApplicationFrame {

    int clientOID;
    CommunityTarget target = new CommunityTarget();
    
    double lastUP;
    double lastDOWN;

    final XYSeries s1 = new XYSeries("Upload");
    final XYSeries s2 = new XYSeries("Download");

    public gerarGrafico(final String title, String usuario) throws InterruptedException {
        super(title);

        final XYSeriesCollection dataset = new XYSeriesCollection();

        dataset.addSeries(s1);
        dataset.addSeries(s2);

        final JFreeChart chart = ChartFactory.createXYLineChart(
                title, // chart title
                "X", // domain axis label
                "Y", // range axis label
                dataset, // data
                PlotOrientation.VERTICAL,
                true, // include legend
                true,
                false
        );

        final XYPlot plot = chart.getXYPlot();
        final NumberAxis domainAxis = new NumberAxis("Tempo (15s)");
        final NumberAxis rangeAxis = new NumberAxis("Consumo (kbps)");
        plot.setDomainAxis(domainAxis);
        plot.setRangeAxis(rangeAxis);
        chart.setBackgroundPaint(Color.white);
        plot.setOutlinePaint(Color.black);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);
        setContentPane(chartPanel);

        try {

            target = new CommunityTarget();
            target.setCommunity(new OctetString("Viva100%"));
            target.setAddress(GenericAddress.parse("udp:172.16.254.65/161")); // supply your own IP and port
            target.setRetries(2);
            target.setTimeout(1500);
            target.setVersion(SnmpConstants.version2c);

            //Search user OID
            Map<String, String> result = doWalk(".1.3.6.1.4.1.2011.5.2.1.15.1.3", target); // ifTable, mib-2 interfaces

            for (Map.Entry<String, String> entry : result.entrySet()) {
                if (entry.getValue().startsWith(usuario + "@")) {
                    clientOID = Integer.parseInt(entry.getKey().replace(".1.3.6.1.4.1.2011.5.2.1.15.1.3.", ""));
                    break;
                }
            }

            //Bring user information
            //
        } catch (Exception err) {
            System.out.println("Merdinha");
            err.printStackTrace();
        }
        
        
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
