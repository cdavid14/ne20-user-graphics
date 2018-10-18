/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ne20.user.monitor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;
import net.sourceforge.yamlbeans.YamlException;
import net.sourceforge.yamlbeans.YamlReader;
import net.sourceforge.yamlbeans.YamlWriter;

/**
 *
 * @author Suporte10
 */
public class Monitor extends javax.swing.JFrame {

    private JPanel jpmestre, jpleste, jpcentro, jpoeste, jpsul;
    private ArrayList<NE20Info> nes;
    private ArrayList<NE20Server> servers;
    static JTabbedPane tabbedPane = new JTabbedPane();
    JTextField txlo;
    JComboBox jcne20;
    private Monitor me;

    /**
     * Creates new form Monitor
     */
    public Monitor() {
        this.me = this;
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

        ////////////////////////////////////////////////////////////////////////
        //MENU
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        
        JMenu editarMenu = new JMenu("Configurações");
        menuBar.add(editarMenu);

        JMenuItem editSrvMenu = new JMenuItem("Editar Servidores");
        editarMenu.add(editSrvMenu);

        JMenuItem aboutMenu = new JMenuItem("Sobre o sistema");
        menuBar.add(aboutMenu);

        aboutMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JDialog f;
                f = new SobreNos(new JFrame());
                f.setVisible(true);
            }
        });
        
        editSrvMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae){
                //System.out.println("teste:"+ me);
                EditarServidores edit = new EditarServidores(me);
                edit.setVisible(true);
            }
        });

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
                        JLabel selected = (JLabel) btnClose.getParent().getComponent(0);
                        tabbedPane.remove(tabbedPane.indexOfTab(selected.getText()));
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
        setTitle("NE20 Monitor");
    }

    private void readNE20() {
        try {
            YamlReader reader = new YamlReader(new FileReader(".config.yml"));
            servers.clear();
            nes.clear();
            while (true) {
                
                NE20Info ne = (NE20Info)reader.read();
                if (ne == null) {
                    break;
                }
                
                nes.add(ne);
                
                NE20Server server = new NE20Server(ne);
                servers.add(server);
                server.start();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (YamlException ex) {
            Logger.getLogger(Monitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ArrayList<NE20Info> getNES(){
        return this.nes;
    }
    
    public void saveProperties(ArrayList<NE20Info> temp){
        try {
            YamlWriter writer = new YamlWriter(new FileWriter(".config.yml"));
            for(NE20Info t : temp){
                writer.write(t);
            }
            writer.close();
            nes = temp;
        } catch (IOException ex) {
        } catch (YamlException ex) {
        }
    }

    private void startProperties() {

        try {
            NE20Info info = new NE20Info("10.10.10.10","adsl");
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
