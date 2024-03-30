/*
 * NewJFrame.java
 *
 * Created on 17 settembre 2007, 20.28
 */

package smsserver;

import java.awt.Color;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Date;


import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import smsserver.client.IGenericClient;

@SuppressWarnings("serial")
public class SmsFrame extends javax.swing.JFrame {

    private Engine _engine;
    
    /** Creates new form NewJFrame */
    public SmsFrame() {
        initComponents();
        GUIControl gc = new GUIControl(this);
        
        _engine = new Engine(gc);
        gc.setEngine (_engine);
        
        new Thread(gc).start();
    }
    
   
                             
    private void initComponents() {
    	
    	  
    	  jLabel1 = new javax.swing.JLabel();
          jLabel2 = new javax.swing.JLabel();
          jLabelEmulatorStatus = new javax.swing.JLabel();
          jLabelGSMStatus = new javax.swing.JLabel();
          jProgressBar1 = new javax.swing.JProgressBar();
          jProgressBar1.setStringPainted(true);
          jSeparator1 = new javax.swing.JSeparator();
          jScrollPane1 = new javax.swing.JScrollPane();
          jTable1 = new javax.swing.JTable();
          jLabel5 = new javax.swing.JLabel();
          jSmsNumber = new javax.swing.JTextField();
          sendButton = new javax.swing.JButton();
          jSeparator2 = new javax.swing.JSeparator();
          jLabel6 = new javax.swing.JLabel();
          jLabel7 = new javax.swing.JLabel();
          jLabel3 = new javax.swing.JLabel();
          jScrollPane2 = new javax.swing.JScrollPane();
          jSmsText = new javax.swing.JTextArea();
          jLabel4 = new javax.swing.JLabel();
          jScrollPane5 = new javax.swing.JScrollPane();
          jSmsTextReaded = new javax.swing.JTextArea();
          jButton1 = new javax.swing.JButton();
          jButton2 = new javax.swing.JButton();
          jButton3 = new javax.swing.JButton();
          jScrollPane3 = new javax.swing.JScrollPane();
          jMessageLog = new javax.swing.JTextArea();
          jClearMsgsButton = new javax.swing.JButton();

          
          
          setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
          setResizable(false);
          Dimension preferredSize = new Dimension(800, 600);
          setPreferredSize(preferredSize);
          setTitle("SMS Server");
          jLabel1.setText("Emulator");

          jLabel2.setText("GSM Modem");

          jLabelEmulatorStatus.setText("Not connected.");

          jLabelGSMStatus.setText("Not connected.");

          
          jTable1.setModel(new javax.swing.table.DefaultTableModel(
                  new Object [][] {},
                  new String [] {
                      "Date", "Sender", "Receiver", "Text", "Status"
                  }
              ));
              
               
          jTable1.setAutoscrolls(true);
          
          JTableHeader th =  jTable1.getTableHeader();
          th.setReorderingAllowed(false);
      
          jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
              public void mouseClicked(java.awt.event.MouseEvent evt) {
                  jTable1MouseClicked(evt);
              }
          });
          
              jScrollPane1.setViewportView(jTable1);
          
//            jSmsTextReaded.setColumns(20);
//            jSmsTextReaded.setLineWrap(true);
//            jSmsTextReaded.setRows(3);
//            jScrollPane4.setViewportView(jSmsTextReaded);
            jLabel5.setText("Send Number");

            sendButton.setText("Send SMS");
            sendButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    sendButtonActionPerformed(evt);
                }
            });
            
            jLabel6.setText("Incoming SMS");

            jLabel7.setText("Message Log ");

            jLabel3.setText("Signal");

            
            
            jMessageLog.setColumns(20);
           
        
            
            jScrollPane3.setViewportView(jMessageLog);

            jSmsText.setColumns(20);
            jSmsText.setLineWrap(true);
            jSmsText.setRows(3);
            jScrollPane2.setViewportView(jSmsText);

            jLabel4.setText("Text");

                   
          
          jSmsTextReaded.setColumns(20);
          jSmsTextReaded.setRows(5);
          jSmsTextReaded.setLineWrap(true);
          jSmsTextReaded.setDisabledTextColor(new Color(0,0,0));
          jSmsTextReaded.setEnabled(false);
          jScrollPane5.setViewportView(jSmsTextReaded);

          jButton1.setText("jButton1");

          jButton2.setText("jButton2");

          jButton3.setText("jButton3");

          jMessageLog.setColumns(20);
          jMessageLog.setRows(5);
          jMessageLog.setLineWrap(true);
          jMessageLog.setDisabledTextColor(new Color(0,0,0));
          jMessageLog.setAutoscrolls(true);
          		
          jMessageLog.setEnabled(false);
          
          jScrollPane3.setViewportView(jMessageLog);

          jClearMsgsButton.setText("Clear All");
          jClearMsgsButton.addActionListener(new java.awt.event.ActionListener() {
              public void actionPerformed(java.awt.event.ActionEvent evt) {
                  jClearMsgsButtonActionPerformed(evt);
              }
          });

          org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
          getContentPane().setLayout(layout);
          layout.setHorizontalGroup(
              layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
              .add(layout.createSequentialGroup()
                  .addContainerGap()
                  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                      .add(layout.createSequentialGroup()
                          .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                              .add(jLabel1)
                              .add(jLabel2))
                          .add(55, 55, 55)
                          .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                              .add(layout.createSequentialGroup()
                                  .add(jLabelGSMStatus)
                                  .add(62, 62, 62)
                                  .add(jLabel3)
                                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                  .add(jProgressBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                              .add(jLabelEmulatorStatus)))
                      .add(layout.createSequentialGroup()
                          .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                              .add(jLabel5)
                              .add(jLabel4))
                          .add(17, 17, 17)
                          .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                              .add(layout.createSequentialGroup()
                                  .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 238, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                  .add(sendButton))
                              .add(jSmsNumber, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 140, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                  .addContainerGap(314, Short.MAX_VALUE))
              .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 788, Short.MAX_VALUE)
              .add(layout.createSequentialGroup()
                  .addContainerGap()
                  .add(jLabel7)
                  .addContainerGap(728, Short.MAX_VALUE))
              .add(layout.createSequentialGroup()
                  .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 778, Short.MAX_VALUE)
                  .addContainerGap())
              .add(layout.createSequentialGroup()
                  .addContainerGap()
                  .add(jLabel6)
                  .addContainerGap(712, Short.MAX_VALUE))
              .add(layout.createSequentialGroup()
                  .addContainerGap()
                  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                      .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 768, Short.MAX_VALUE)
                      .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                          .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 503, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                          .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                          .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                              .add(layout.createSequentialGroup()
                                  .add(jButton1)
                                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                  .add(jButton2)
                                  .add(7, 7, 7)
                                  .add(jButton3))
                              .add(jScrollPane5))))
                  .addContainerGap())
              .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                  .addContainerGap(705, Short.MAX_VALUE)
                  .add(jClearMsgsButton)
                  .addContainerGap())
          );
          layout.setVerticalGroup(
              layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
              .add(layout.createSequentialGroup()
                  .addContainerGap()
                  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                      .add(layout.createSequentialGroup()
                          .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                              .add(jLabel1)
                              .add(jLabelEmulatorStatus))
                          .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                          .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                              .add(jLabel2)
                              .add(jLabelGSMStatus)
                              .add(jLabel3)))
                      .add(jProgressBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                      .add(layout.createSequentialGroup()
                          .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                              .add(jLabel5)
                              .add(jSmsNumber, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                          .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                          .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                              .add(jLabel4)
                              .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                      .add(sendButton))
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(jLabel6)
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                      .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                          .add(jScrollPane5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                          .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                          .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                              .add(jButton1)
                              .add(jButton2)
                              .add(jButton3)))
                      .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 133, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(jLabel7)
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 124, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                  .add(jClearMsgsButton)
                  .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          );
          pack();
    }

    // Invia un sms con i dati specificati nel form
    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {                                           
    	
    	if (jSmsNumber.getText().length() == 0) {
    		addMessageLog("sms requires a phone number and a message.");
    		return;
    	}

    	MyMessage msg = new MyMessage();
    	
    	IGenericClient gClient = null;
        if (jSmsNumber.getText().substring(0, 3).compareToIgnoreCase("+55")==0){
         	gClient = _engine.getWtkClient();
         	msg.setToPort(CConstants.DEFAULT_SMS_PORT);
         } else {
         	gClient = _engine.getGSMClient();
         }
        
        msg.setSender(gClient.getPhoneNumber());
        msg.setReceiver(jSmsNumber.getText());
        msg.setText(jSmsText.getText());

    	_engine.sendSms(msg, gClient);
    }                                          

    
    private void jClearMsgsButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                 
    	jMessageLog.setText("");
    }                                                

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {                                     
    	
    	
    	jSmsTextReaded.setText((String)jTable1.getValueAt(jTable1.getSelectedRow(), 3));
    	
    	
    } 
    
    
    
    // Aggiunge un messaggio nella finestra dei log
    public void addMessageLog(String s){
    	
    	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    	String timestamp = sdf.format(new Date());
    	
    	String str = "";    	
    	if (jMessageLog.getText().length() > 0) 
    		str = jMessageLog.getText() + "\n";
    	str += timestamp + " - " + s;
    		
    	jMessageLog.setText(str);
    }
 
    
    // Aggiunte un sms alla tabella
    public void addSMS(MyMessage msg){

    	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    	String timestamp = sdf.format(msg.getTime());
    	
    	String [] s =  {timestamp,    
   			 msg.getSenderFullQualified(),
   			 msg.getReceiverFullQualified(),
   			 msg.getText(),
   			 ""};
    	DefaultTableModel tb = (DefaultTableModel)jTable1.getModel();
    	tb.addRow(s);
    }

    
    public void setLabelEmulatorStatus(String str){
    	jLabelEmulatorStatus.setText(str);
    }
    
    public void setLabelGSMModemStatus(String str){
    	jLabelGSMStatus.setText(str);
    }
    
    public void setGSMModemSignal(int s){
    	jProgressBar1.setValue(s);
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
    	System.setProperty("kvem.home","C:\\Programmi\\Java\\WTK251\\");
    	
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SmsFrame().setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify                     
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabelEmulatorStatus;
    private javax.swing.JLabel jLabelGSMStatus;
    //private javax.swing.JList jList1;
    private javax.swing.JProgressBar jProgressBar1;
    
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextArea jSmsText;
    private javax.swing.JTextArea jSmsTextReaded;
    private javax.swing.JTextArea jMessageLog;
    private javax.swing.JTextField jSmsNumber;
    private javax.swing.JButton sendButton;
    // End of variables declaration       
    
    // Variables declaration - do not modify                     
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jClearMsgsButton;
    
    private javax.swing.JScrollPane jScrollPane5;
    
    
    
    
}
