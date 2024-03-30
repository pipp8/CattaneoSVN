/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.unisa.segnapunti;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author pipp8
 */
public class Segnapunti2 extends javax.swing.JFrame {

    /**
     * Creates new form Display
     */
    public Segnapunti2() {
        
        punteggioPlayerA = 0;
        punteggioPlayerB = 0;
        setPlayerA = 0;
        setPlayerB = 0;
        
        setTerminato = false;
        servePlayer = -1;
        
        initComponents();
    }

   

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
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Display.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Display.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Display.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Display.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Segnapunti2 app = new Segnapunti2();
                app.setExtendedState(app.getExtendedState() | Frame.MAXIMIZED_BOTH);
                app.setVisible(true);
            }
        });
    }

       
    private void CambioCampo(java.awt.event.ActionEvent evt) {                             
        int set = setPlayerA;  
        setPlayerA = setPlayerB;      
        setPlayerB = set;

        punteggioPlayerA = 0;       
        punteggioPlayerB = 0;

        fineSet(false);

        servePlayer = (servePlayer + 1) % 2; // servizio all'altro
        cambioServizio(servePlayer);
        showStatus();
               
        this.repaint();
    }         
        
        
    private void NuovaPartita(java.awt.event.ActionEvent evt) {    
        
        int ans = JOptionPane.showConfirmDialog(this,
            "Vuoi davvero iniziare una nuova partita.",
            "Warning", JOptionPane.YES_NO_OPTION); //  JOptionPane.QUESTION_MESSAGE);
        
        if (ans == JOptionPane.YES_OPTION) {
            setPlayerA = 0;
            setPlayerB = 0;
            punteggioPlayerA = 0;
            punteggioPlayerB = 0;
            
            showStatus();
            servePlayer = -1;
            fineSet(false);
        }
    }         
      
    
    private void PanelMouseClickedHandler(java.awt.event.MouseEvent evt) {                                     
        int bt = evt.getButton();
        boolean mod = evt.isControlDown() || evt.isShiftDown();
        int clickCount = evt.getClickCount();
        Component c = evt.getComponent();
            
        if (setPlayerA == 0 && setPlayerB == 0 &&
           punteggioPlayerA == 0 && punteggioPlayerB == 0) {
            if (c == pnlSetDx) {
                servePlayer = 1; // 1 ==> playerB (Dx)
                cambioServizio( servePlayer);
                showStatus();
            }
            else if (c == pnlSetSx) {
                servePlayer = 0; // 0 ==> playerA (Sx)
                cambioServizio( servePlayer);
                showStatus();
            }
        }
    }                                   
        
    private void MouseClickedHandler(java.awt.event.MouseEvent evt) {                                     
        int bt = evt.getButton();
        boolean mod = evt.isControlDown() || evt.isShiftDown();
        int clickCount = evt.getClickCount();
        
        if (servePlayer >= 0 && clickCount == 1) {
            switch (bt) {
                case MouseEvent.NOBUTTON:
                    System.out.println("No Button ???"); break;

                case MouseEvent.BUTTON1:
                    IncrementLeftPlayer( mod);
                    break;

                case MouseEvent.BUTTON2:
                    System.out.println("Button 2 pressed"); break;

                case MouseEvent.BUTTON3:
                    IncrementRightPlayer(mod);
                    break;
                default:
                    System.out.println("How many buttons has this mouse ????"); break;
            }
        }
        else {
            System.out.println("N. Click: " + clickCount);
        }
    }                                    
    
    public void IncrementLeftPlayer(boolean mod)
    {
        if (mod) {
            if (punteggioPlayerA > 0) {
                punteggioPlayerA--;
                if (setTerminato) {
                    setPlayerA--;
                    pnlSetSx.setMsg(setPlayerA);
                    fineSet(false);
                }
            }
        }
        else {
            if (!setTerminato) {
               punteggioPlayerA++;
        
                if (punteggioPlayerA >= maxPunti &&
                        ((punteggioPlayerA-punteggioPlayerB) >= 2)) {
                    setPlayerA++;
                    pnlSetSx.setMsg(setPlayerA);
                    fineSet(true);
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        }
        
        if (!setTerminato)
            checkCambioServizio();
        pnlSx.setMsg( punteggioPlayerA);
    }
    
    
    public void IncrementRightPlayer(boolean mod) 
    {
        if (mod) {
            if (punteggioPlayerB > 0) {
                punteggioPlayerB--;
                if (setTerminato) {
                    setPlayerB--;
                    pnlSetDx.setMsg(setPlayerB);
                    fineSet(false);
                }
            }
        }
        else {
            if (!setTerminato) {
                punteggioPlayerB++;

                if (punteggioPlayerB >= maxPunti &&
                        ((punteggioPlayerB-punteggioPlayerA) >= 2)) {
                    setPlayerB++;
                    pnlSetDx.setMsg(setPlayerB);
                    fineSet(true);
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        }
        
        if (!setTerminato)
            checkCambioServizio();
        pnlDx.setMsg( punteggioPlayerB);    
    }
    
    
    public void checkCambioServizio() {
        
        int totPunti = punteggioPlayerA + punteggioPlayerB;
        
        if (punteggioPlayerA >= 10 && punteggioPlayerB >= 10) {
            // si cambia ad ogni punto
            if (totPunti % 2 == 0) {
                // serve chi ha cominiciato
                cambioServizio(servePlayer);
            }
            else {
                // serve l'altro (se ha iniziato A allora B e viceversa
                cambioServizio( (servePlayer + 1) % 2);                 
            }
        }
        else {
            // si cambia ogni 2 servizi
            if (totPunti % 2 == 0) {
                // cambio
                if (totPunti % 4 == 0) {
                    // serve chi ha cominciato
                    cambioServizio(servePlayer);
                }
                else {
                    // serve l'altro (se ha iniziato A allora B e viceversa
                    cambioServizio( (servePlayer + 1) % 2); 
                }
            }
        }
    }
        
    
    public void cambioServizio( int serve) {
        
        if (serve == 0) {
            pnlDx.setActiveBackground(false);
            pnlSx.setActiveBackground(true);
        }
        else {
            pnlSx.setActiveBackground(false);
            pnlDx.setActiveBackground(true);
        }
    }
    
    
    public void fineSet(boolean val) {
        setTerminato = val;
        btCambio.setEnabled( val);
        pnlSx.setActiveBackground(false);
        pnlDx.setActiveBackground(false);
    }
    
    public void showStatus() {
        
        pnlSx.setMsg(punteggioPlayerA);
        pnlDx.setMsg(punteggioPlayerB);
        pnlSetSx.setMsg(setPlayerA);
        pnlSetDx.setMsg(setPlayerB); 
    }
    
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        this.setPreferredSize(new Dimension(1200, 600));
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                MouseClickedHandler(evt);
            }
        });
        
  
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        java.awt.GridBagLayout layout = new java.awt.GridBagLayout();
        layout.rowHeights = new int[] {200, 300, 35};
        layout.rowWeights = new double[] {0.5, 0.5, 0};
        layout.columnWidths = new int[] {400,200,200,400};
        layout.columnWeights = new double[] {0.66, 0.33, 0.33, 0.66};
        getContentPane().setLayout(layout);

        
        pnlSx = new SelfAdjustingPanel( new Color(255, 255, 255), Color.black, "/it/unisa/resources/Servizio-Ico.jpg");
        pnlSx.setPreferredSize(new Dimension( getPreferredSize().width / 3, getPreferredSize().height -15));
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.75;
        gridBagConstraints.weighty = 0.75;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        getContentPane().add(pnlSx, gridBagConstraints);

        pnlSetSx = new SelfAdjustingPanel( new Color(220, 220, 220), Color.red);
        pnlSetSx.setPreferredSize(new java.awt.Dimension(getPreferredSize().width / 8, 200));
        pnlSetSx.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                PanelMouseClickedHandler(evt);
            }
        });
                
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 0.30;
        gridBagConstraints.weighty = 0.30;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        getContentPane().add(pnlSetSx, gridBagConstraints);

        pnlSetDx = new SelfAdjustingPanel(new Color(220, 220, 220), Color.red);
        pnlSetDx.setPreferredSize(new java.awt.Dimension(getPreferredSize().width / 8, 200));
        pnlSetDx.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                PanelMouseClickedHandler(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 0.30;
        gridBagConstraints.weighty = 0.30;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        getContentPane().add(pnlSetDx, gridBagConstraints);

                
        pnlDx = new SelfAdjustingPanel(new Color(255, 255, 255), Color.black, "/it/unisa/resources/Servizio-Ico.jpg" );
        pnlDx.setPreferredSize(new Dimension( getPreferredSize().width / 3, getPreferredSize().height -15));
        pnlDx.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                PanelMouseClickedHandler(evt);
            }
        });
        

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 0.75;
        gridBagConstraints.weighty = 0.75;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        getContentPane().add(pnlDx, gridBagConstraints);

        btNewGame = new JButton("Nuova Partita");
        btNewGame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NuovaPartita(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 3;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        getContentPane().add(btNewGame, gridBagConstraints);

        btCambio = new JButton("Cambio Campo");
        btCambio.setEnabled(false);
        btCambio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CambioCampo(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 3;
        gridBagConstraints.ipady = 3;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        getContentPane().add(btCambio, gridBagConstraints);
        pack();
    }// </editor-fold>                        
                


    // Variables declaration - do not modify                     
    private SelfAdjustingPanel pnlSx;
    private SelfAdjustingPanel pnlDx;
    private SelfAdjustingPanel pnlSetSx;
    private SelfAdjustingPanel pnlSetDx;
    private JLabel lblSx;
    private JLabel lblDx;
    
    private JButton btNewGame;
    private JButton btCambio;
    
    private int punteggioPlayerA = 0;
    private int punteggioPlayerB = 0;
    private int setPlayerA = 0;
    private int setPlayerB = 0;
    private boolean setTerminato = false;
    private int servePlayer = -1;
    
    final int maxPunti = 11;
    final int serviziXCambio = 2;
    // End of variables declaration                   
}
