/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ne20.user.monitor;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 *
 * @author christian
 */
public class AdicionarServidor extends JFrame{
    
    JPanel jpmestre,jpip,jpbotoes;
    JLabel jlIP,jlCommunity;
    JTextField jtfIP,jtfCommunity;
    JButton jbOK,jbVoltar;
    AdicionarServidor me;
    
    public AdicionarServidor(Monitor m, DefaultListModel e){
        me = this;
        setLayout(new BorderLayout());
        
        jpmestre = new JPanel(new GridLayout(3,1));
        
        jpip = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        
        jlIP = new JLabel("IP:", SwingConstants.RIGHT);
        jtfIP = new JTextField("");
        jtfIP.setColumns(40);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.ipadx = 10;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpip.add(jlIP,gbc);
        gbc.gridx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 120;
        jpip.add(jtfIP,gbc);
        
        jlCommunity = new JLabel("Community:", SwingConstants.RIGHT);
        jtfCommunity = new JTextField("");
        jtfCommunity.setColumns(40);
        
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.ipadx = 10;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpip.add(jlCommunity,gbc);
        gbc.gridx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 120;
        jpip.add(jtfCommunity,gbc);
        
        jbOK = new JButton("Adicionar");
        jbOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if(!jtfIP.getText().isEmpty() && !jtfCommunity.getText().isEmpty()){
                    NE20Info novo = new NE20Info(jtfIP.getText(),jtfCommunity.getText());
                    ArrayList<NE20Info> master = m.getNES();
                    master.add(novo);
                    m.saveProperties(master);
                    e.addElement(jtfIP.getText());
                    m.jcne20.addItem(jtfIP.getText());                    
                    JOptionPane.showMessageDialog(null, "Servidor Adicionado com Sucesso!","Novo Servidor",JOptionPane.INFORMATION_MESSAGE);
                    me.dispatchEvent(new WindowEvent(me, WindowEvent.WINDOW_CLOSING));
                    m.readNE20();
                }else{
                    JOptionPane.showMessageDialog(null, "Por favor preencha o IP e a commmunity");
                }
            }
        });
        jbVoltar = new JButton("Cancelar");
        
        jpbotoes = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        jpbotoes.add(jbOK,gbc);
        
        gbc.gridx++;
        jpbotoes.add(jbVoltar,gbc);
        
        jpmestre.add(jpip);
        jpmestre.add(jpbotoes);
        add(jpmestre);
        setSize(250, 200);
        setTitle("Adicionar Servidor");
    }
}
