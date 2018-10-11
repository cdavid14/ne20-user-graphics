package ne20.user.monitor;

import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public class SplashScreen extends JWindow {
        
    public SplashScreen() {
    }
    
// Este é um método simples para mostrar uma tela de apresentção

// no centro da tela durante a quantidade de tempo passada no construtor

    public void showSplash() {        
        JPanel content = (JPanel)getContentPane();
        content.setBackground(Color.white);
        
        // Configura a posição e o tamanho da janela
        int width = 350;
        int height =100;
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width-width)/2;
        int y = (screen.height-height)/2;
        setBounds(x,y,width,height);
        
        // Constrói o splash screen
        JLabel label = new JLabel("\t\tCarregando informações dos equipamentos cadastrados...");
        Color bdr = new Color(220, 220, 220,  100);
        final JProgressBar pr = new JProgressBar();
        pr.setStringPainted(false);
        pr.setString(null);
        pr.setValue(0);
        pr.setIndeterminate(true);
        pr.setSize(new Dimension(width, 23));

        content.add(label, BorderLayout.CENTER);
        content.add(pr, BorderLayout.SOUTH);
        content.setBorder(BorderFactory.createLineBorder(bdr, 5));  
        // Torna visível
        setVisible(true);
    }
    
    public void start() {        
        showSplash();

    }
    
    public void stop() {
        setVisible(false);
    }
}