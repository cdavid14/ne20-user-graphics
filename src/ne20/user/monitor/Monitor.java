/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ne20.user.monitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;
import javax.swing.text.MaskFormatter;
import net.sourceforge.yamlbeans.YamlException;
import net.sourceforge.yamlbeans.YamlReader;
import net.sourceforge.yamlbeans.YamlWriter;
import org.snmp4j.smi.IpAddress;

/**
 *
 * @author Suporte10
 */
public class Monitor extends javax.swing.JFrame {

    private JPanel jpmestre, jpleste, jpcentro, jpoeste, jpsul;
    private ArrayList<NE20Info> nes;
    private ArrayList<NE20Server> servers;
    JTabbedPane tabbedPane = new JTabbedPane();
    JTextField txlo;
    JComboBox jcne20;
    private static SplashScreen loading;

    /**
     * Creates new form Monitor
     */
    public Monitor() {
        try {
            // Set cross-platform Java L&F (also called "Metal")
            javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException e) {
            // handle exception
        } catch (ClassNotFoundException e) {
            // handle exception
        } catch (InstantiationException e) {
            // handle exception
        } catch (IllegalAccessException e) {
            // handle exception
        }
        tabbedPane.setTabLayoutPolicy(JTabbedPane.VERTICAL);

        jpmestre = new JPanel(new BorderLayout());
        //jpmestre.setBackground(Color.yellow);

        File test = new File(".config.yml");
        if (!test.exists()) {
            startProperties();
        }
        nes = new ArrayList<>();
        servers = new ArrayList<>();

        readNE20();

        loading.stop();
        ////////////////////////////////////////////////////////////////////////
        //CENTRO
        jpcentro = new JPanel(new GridLayout(1, 1));
        jpcentro.add(tabbedPane);

        jpmestre.add(jpcentro);
        ////////////////////////////////////////////////////////////////////////
        //LESTE
        jpleste = new JPanel(new GridLayout(10, 1));

        TitledBorder bne = new TitledBorder("NE 20");
        jcne20 = new JComboBox(nes.toArray());
        JPanel jpne = new JPanel();
        bne.setTitleJustification(TitledBorder.CENTER);
        bne.setTitlePosition(TitledBorder.TOP);
        jpne.add(jcne20);
        jpne.setBorder(bne);

        TitledBorder blo = new TitledBorder("User Login");
        blo.setTitleJustification(TitledBorder.CENTER);
        blo.setTitlePosition(TitledBorder.TOP);
        JPanel jplo = new JPanel();
        jplo.setBorder(blo);
        txlo = new JTextField("");
        txlo.setColumns(10);
        jplo.add(txlo);

        JButton jbgo = new JButton("Monitorar");
        jbgo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                String ip = jcne20.getSelectedItem().toString();
                String login = txlo.getText().toLowerCase().trim();
                if (login.length() > 100 | login.length() < 1) {
                    return;
                }
                //verificar se a aba ja esta aberta
                for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                    Object c = tabbedPane.getComponentAt(i);
                    ExtJPanel p = (ExtJPanel) c;
                    if (p.isThis(ip, login)) {
                        return;
                    }
                }
                //adicionar tabbed pane
                ExtJPanel novo = new ExtJPanel(ip, login);
                novo.setServer(servers.get(jcne20.getSelectedIndex()));
                tabbedPane.addTab(login, novo);
                tabbedPane.setSelectedIndex(tabbedPane.indexOfTab(login));
                // Modificar aba para adicionar botão de fechar
                int id = tabbedPane.indexOfTab(login);
                JPanel pnlTab = new JPanel(new GridBagLayout());
                pnlTab.setOpaque(false);
                JLabel lblTitle = new JLabel(login);
                JButton btnClose = new JButton("x");
                //Alterar caracteristicas do botão para melhorar o aspecto
                btnClose.setPreferredSize(new Dimension(20, 20));
                btnClose.setBorderPainted(false);
                btnClose.setFocusPainted(false);
                btnClose.setContentAreaFilled(false);
                btnClose.setMargin(new Insets(0, 0, 0, 0));
                btnClose.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        btnClose.setBackground(Color.RED);
                    }

                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        btnClose.setBackground(null);
                    }
                });

                //Propriedades do texto
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.weightx = 1;

                pnlTab.add(lblTitle, gbc);

                //Propriedades do botão
                gbc.gridx++;
                gbc.weightx = 1;
                pnlTab.add(btnClose, gbc);

                //Setar nova aba
                tabbedPane.setTabComponentAt(id, pnlTab);

                //Evento do botão de fechar
                btnClose.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        Component selected = tabbedPane.getSelectedComponent();
                        if (selected != null) {
                            novo.stop();
                            tabbedPane.remove(selected);
                        }
                    }
                });
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        novo.doYourThings();
                    }
                }).start();

            }
        });

        jpleste.add(jpne);
        jpleste.add(jplo);
        jpleste.add(jbgo);
        jpmestre.add(jpleste, BorderLayout.WEST);
        ////////////////////////////////////////////////////////////////////////

        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        add(jpmestre);

        loading.stop();
    }

    private void readNE20() {
        try {
            YamlReader reader = new YamlReader(new FileReader(".config.yml"));
            while (true) {
                NE20Info ne = reader.read(NE20Info.class);
                if (ne == null) {
                    break;
                }

                nes.add(ne);
                NE20Server server = new NE20Server(ne);
                servers.add(server);
                server.readServer();
                server.start();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (YamlException ex) {
            Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void startProperties() {

        try {
            NE20Info info = new NE20Info("172.16.254.65", "Viva100%");
            YamlWriter writer = new YamlWriter(new FileWriter(".config.yml"));
            writer.write(info);
            writer.close();

            //File f = new File(".config.yml");
            //Runtime.getRuntime().exec("attrib +H .config.yml");
        } catch (YamlException ex) {
            Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        snmp_versao = new javax.swing.ButtonGroup();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("NE20 - VIVA");
        setResizable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 680, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 421, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Monitor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Monitor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Monitor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Monitor.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        loading = new SplashScreen();
        loading.start();

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Monitor m = new Monitor();
                m.setVisible(true);
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup snmp_versao;
    // End of variables declaration//GEN-END:variables
}
