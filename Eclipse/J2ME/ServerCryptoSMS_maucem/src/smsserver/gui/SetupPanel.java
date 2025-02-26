package smsserver.gui;

import javax.swing.JOptionPane;


import smsserver.CSettings;
/*
 * smsPanel.java
 *
 * Created on 22 settembre 2007, 16.41
 */
import smsserver.Engine;

/**
 *
 * @author  maucem
 */
@SuppressWarnings("serial")
public class SetupPanel extends javax.swing.JPanel implements java.awt.event.ActionListener {
    
	private Engine _engine;
	
    /** Creates new form smsPanel */
    public SetupPanel(Engine e) {
    	_engine = e;
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {                          
        jLabel1 = new javax.swing.JLabel();
        doSetupButton = new javax.swing.JButton();
        textName = new javax.swing.JTextField();
        textPhoneNumber = new javax.swing.JTextField();
        textPassphrase = new javax.swing.JPasswordField();
        jPasswordField2 = new javax.swing.JPasswordField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        textPhoneNumber1 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();

        setPreferredSize(new java.awt.Dimension(700, 400));
        jLabel1.setText("Name");

        doSetupButton.setText("Do Setup");
        doSetupButton.addActionListener(this);

        
        textPhoneNumber.setText(CSettings.getNumberSMSC_WTK());
        textPhoneNumber.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        textPhoneNumber.setEnabled(false);

        jLabel2.setText("Emulator Phone Number");

        jLabel3.setText("Passphrase");

        jLabel4.setText("Confirm Passphrase");

        textPhoneNumber1.setText(CSettings.getNumberSMSC_GSM());
        textPhoneNumber1.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        textPhoneNumber1.setEnabled(false);

        jLabel5.setText("GSM Phone Number");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(299, 299, 299)
                        .add(doSetupButton))
                    .add(layout.createSequentialGroup()
                        .add(204, 204, 204)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel2)
                            .add(jLabel3)
                            .add(jLabel4)
                            .add(jLabel5)
                            .add(jLabel1))
                        .add(34, 34, 34)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, textPhoneNumber1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, textPhoneNumber, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, textName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE))
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, textPassphrase)
                                .add(org.jdesktop.layout.GroupLayout.LEADING, jPasswordField2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)))))
                .add(191, 191, 191))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(48, 48, 48)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(textName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(14, 14, 14)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(textPhoneNumber1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(17, 17, 17)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(textPhoneNumber, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(15, 15, 15)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel3)
                    .add(textPassphrase, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(16, 16, 16)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel4)
                    .add(jPasswordField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(40, 40, 40)
                .add(doSetupButton)
                .addContainerGap(119, Short.MAX_VALUE))
        );
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == doSetupButton) {
            SetupPanel.this.doSetup(evt);
        }
    }                        

    private void doSetup(java.awt.event.ActionEvent evt) {                         

    	
    	
    	String name = textName.getText();
    	String gsm = textPhoneNumber1.getText();
    	String emulator = textPhoneNumber.getText();
    	
    	StringBuffer sb = new StringBuffer();
    	char [] chars = textPassphrase.getPassword();
    	String passphrase = sb.append(chars).toString();

    	sb = new StringBuffer();
    	chars = jPasswordField2.getPassword();
    	String repeatPass = sb.append(chars).toString();
    	
    	
    	if(name.length() < 2 || gsm.length() < 7 || emulator.length() < 7|| passphrase.length() < 5 || !passphrase.equals(repeatPass)) {
			StringBuffer e=new StringBuffer();
			if (name.length() < 2){
				e.append("Name must be at least 2 characters!");
				e.append("\n");
			}
			if (gsm.length() < 7) {
				e.append("GSM Phonenumber must be at least 7 characters!");
				e.append("\n");
			}
			if (emulator.length() < 7) {
				e.append("Emulator Phonenumber must be at least 7 characters!");
				e.append("\n");
			}
			if (passphrase.length() < 5){
				e.append("Passphrase must be at least 5 characters!");
				e.append("\n");
			}
			if(!passphrase.equals(repeatPass)){
				e.append("1. and 2. passphrase entry do not match!");
				e.append("\n");
			}
			JOptionPane.showMessageDialog(null, e.toString(), "SMS Server", JOptionPane.INFORMATION_MESSAGE);
    	} else {
    		System.out.println("engine "+_engine);
    		_engine.doSetup(name, gsm, emulator, passphrase, repeatPass);
    	}
    	
    }                        
    
    
    // Variables declaration - do not modify                     
    private javax.swing.JButton doSetupButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPasswordField jPasswordField2;
    private javax.swing.JTextField textName;
    private javax.swing.JPasswordField textPassphrase;
    private javax.swing.JTextField textPhoneNumber;
    private javax.swing.JTextField textPhoneNumber1;
    // End of variables declaration                   
    
}
