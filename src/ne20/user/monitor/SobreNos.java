/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ne20.user.monitor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 *
 * @author Christian
 */
public class SobreNos extends JDialog {

    public SobreNos(JFrame parent) {
        super(parent, "Sobre nós", true);

        Box b = Box.createVerticalBox();
        b.add(Box.createGlue());
        b.add(new JLabel("Desenvolvido por Edsongley Varela de Almeida e Christian David Moura de Freitas",SwingConstants.CENTER));
        b.add(new JLabel("Com a finalidade de ter um feedback sobre o tráfego e informações do cliente conectado",SwingConstants.CENTER));
        b.add(new JLabel("No B-RAS NE20, devido uma demanda interna na empresa ao qual trabalhamos",SwingConstants.CENTER));
        b.add(new JLabel("O aplicativo e seu código-fonte são gratuitos e disponíveis para qualquer um.",SwingConstants.CENTER));
        b.add(new JLabel("Visite nosso repositório: http://github.com/cdavid14/ne20-user-monitor",SwingConstants.CENTER));
        b.add(Box.createGlue());
        getContentPane().add(b);

        JPanel p2 = new JPanel();
        JButton ok = new JButton("Ok");
        p2.add(ok);
        getContentPane().add(p2, "South");

        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setVisible(false);
            }
        });

        setSize(600, 200);
    }
}
