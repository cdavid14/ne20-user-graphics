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
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author christian
 */
public class EditarServidores extends JFrame {

    private JList lista;
    private DefaultListModel model;
    private JPanel jpmestre, jpbotoes;
    
    public EditarServidores(Monitor m) {
        setLayout(new BorderLayout());
        this.model = new DefaultListModel();
        this.lista = new JList(this.model);
        
        JScrollPane jsplista = new JScrollPane(this.lista);
        
        jpmestre = new JPanel(new GridBagLayout());
        
        jpbotoes = new JPanel(new GridLayout(1,2));
        
        JButton jbadicionar = new JButton("Adicionar");
        jbadicionar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                AdicionarServidor a = new AdicionarServidor(m,model);
                a.setVisible(true);
            }
        });
        
        JButton jbremover = new JButton("Remover");
        jbremover.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if(!lista.isSelectionEmpty()){
                    int selectID = lista.getSelectedIndex();
                    int deletar = JOptionPane.showConfirmDialog (null, "Você realmente deseja deletar esse equipamento?","Aviso",JOptionPane.YES_NO_OPTION);
                    if(deletar == JOptionPane.YES_OPTION){
                        ArrayList<NE20Info> temp = m.getNES();
                        temp.remove(selectID);
                        m.saveProperties(temp);
                        System.out.println("REMOVE: "+selectID);
                        model.remove(selectID);
                        m.jcne20.removeItemAt(selectID);
                    }
                }
            }
        });

        ArrayList<NE20Info> nes = m.getNES();
        for (int i = 0; i < nes.size(); i++) {
            model.addElement(nes.get(i).ip);
        }
        
        jpbotoes.add(jbadicionar);
        jpbotoes.add(jbremover);
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.ipady = 300;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpmestre.add(jsplista,gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.ipady = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        jpmestre.add(jpbotoes,gbc);
        
        add(jpmestre);
        setSize(250, 400);
        setTitle("Editar Servidores");
    }
}
