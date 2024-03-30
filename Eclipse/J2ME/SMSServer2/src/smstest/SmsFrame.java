package smstest;

/*
 * $Header: /cygdrive/C/CVSNT/cvsroot/Cattaneo/Sources/J2ME/SMSServer2/src/smstest/SmsFrame.java,v 1.2 2007-09-09 18:02:37 cattaneo Exp $
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.awt.*;
import java.awt.event.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.sun.kvem.midp.io.j2se.wma.*;
import com.sun.kvem.midp.io.j2se.wma.client.WMAClient;
import com.sun.kvem.midp.io.j2se.wma.client.WMAClient.MessageListener;
import com.sun.kvem.midp.io.j2se.wma.client.WMAClientFactory;


public class SmsFrame extends JFrame implements WMAClient.MessageListener {

	JPanel smsPanel = new JPanel();
	GridBagLayout gridBagLayout1;
	JTextField smsNumber = new JTextField(20);
	JTextField smsText = new JTextField(160);
	JTextArea smsIncomingList;
	JLabel label1 = new JLabel();
	JLabel label2 = new JLabel();
	JLabel label3 = new JLabel();
	JButton sendButton = new JButton("Send Sms");
	
    public static int DEFAULT_SMS_PORT = 50000;
    public static int DEFAULT_CBS_MESS_ID = 50001;

    private WMAClient wtkClient;
    
    private int smsPort = DEFAULT_SMS_PORT;
    private int cbsMessID = DEFAULT_CBS_MESS_ID;

    
	public SmsFrame() {
		try {
	        wtkClient = WMAClientFactory.newWMAClient(null, WMAClient.SEND_AND_RECEIVE);

	        wtkClient.connect();
	        wtkClient.setMessageListener(this);

	        GUIinit();
		} catch( Exception ee) {
			ee.printStackTrace();
		}
	}
	
	private void GUIinit() throws Exception {
		this.setBackground(new Color(157, 157, 173));
		// this.setSize(new Dimension(400, 493));
		this.setResizable(true);

		smsIncomingList = new JTextArea(10, 50);
		smsIncomingList.setEditable(false);
		JScrollPane smsIncomingListSP = new JScrollPane(smsIncomingList);

		gridBagLayout1 = new GridBagLayout();
		smsPanel.setLayout(gridBagLayout1);
		
		label1.setFont(new java.awt.Font("Dialog", 1, 12));
		label1.setText("Sms Number");
		label2.setFont(new java.awt.Font("Dialog", 1, 12));
		label2.setText("Sms Text");
		label3.setFont(new java.awt.Font("Dialog", 1, 12));
		label3.setText("Sms List");
		sendButton.setBackground(Color.lightGray);
		sendButton.setFont(new java.awt.Font("Dialog", 1, 12));
//		smsText.setBackground(Color.gray);
//		smsText.setForeground(Color.black);
//		smsNumber.setBackground(Color.gray);
//		smsNumber.setForeground(Color.black);
//		smsIncomingList.setBackground(Color.lightGray);
//		smsIncomingList.setFont(new java.awt.Font("Dialog", 1, 12));
		
		sendButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendSms();
			}
		});
	
//		smsIncomingList.addActionListener(new java.awt.event.ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				smsIncomingList_actionPerformed(e);
//			}
//		});
		
		smsText.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				smsText_actionPerformed(e);
			}
		});
		
		smsNumber.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				smsNumber_actionPerformed(e);
			}
		});
		
		this.add(smsPanel, BorderLayout.CENTER);
		pack();
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4,4,4,4);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.WEST;
		smsPanel.add(smsText, gbc);
		// BagConstraints(0, 3, 4, 1, 0.0, 0.0
		// GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 331, 0));
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.WEST;
		smsPanel.add(smsNumber, gbc);
		// BagConstraints(0, 1, 3, 1, 0.0, 0.0
		// GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 0, 2), 197, 0));
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.WEST;
		smsPanel.add(label1, gbc);
		// BagConstraints(0, 0, 3, 1, 0.0, 0.0
		//,GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.WEST;
		smsPanel.add(label2, gbc);
		// BagConstraints(0, 2, 3, 1, 0.0, 0.0
		// GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(1, 10, 0, 0), 0, 3));
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.WEST;
		smsPanel.add(label3, gbc);
		// BagConstraints(0, 5, 2, 1, 0.0, 0.0
		//,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 10, 0, 96), 0, 0));
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 2;
		gbc.gridheight = 4;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.WEST;
		smsPanel.add(smsIncomingListSP, gbc);
		// BagConstraints(0, 6, 3, 1, 0.0, 0.0
		//,GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(10, 30, 10, 43), 204, 218));
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		smsPanel.add(sendButton, gbc);
		// BagConstraints(0, 4, 1, 1, 0.0, 7.0
		//,GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(1, 10, 18, 0), 2, 2));
		this.setTitle(" SMS Tester");
	}
		
	void smsNumber_actionPerformed(ActionEvent e) {
	}
	
	
	void smsIncomingList_actionPerformed(ActionEvent e) {
	}
	
	void this_windowClosing(WindowEvent e) {
		System.exit(0);
	}
	
	void smsText_actionPerformed(ActionEvent e) {
	}
	
	private void sendSms() {
		//Stringa di connessione nel formato "sms://address:port"
		String destAddress = "sms://"+smsNumber.getText() + ":" + smsPort;

        if (smsNumber.getText().length() == 0) {
            System.out.println("sms requires a phone number and a message.");
            return;
        }

        Message message = new Message(smsText.getText());
        message.setToAddress(destAddress);
        message.setFromAddress("sms://" + wtkClient.getPhoneNumber());

        try {
            wtkClient.send(message);
            System.out.println("Sent sms.");
        } catch (Exception e) {
            System.err.println("Caught exception sending sms: ");
            e.printStackTrace();
        }
	}
		

   /**
     * callback to report a received message
     * specified by the WMAClient.MessageListener interface
     */
    public void notifyIncomingMessage(WMAClient client) {
        try {
            WMAMessage mess = wtkClient.receive();

            if (mess instanceof Message) {
                Message message = (Message)mess;
                String title;
    			String senderNumber = message.getFromAddress();
    			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    			String timestamp = sdf.format(message.getSendTime());

                if (message.isSMS()) {
                    if ( message.isText()) {
						// Si tratta di un messaggio di testo
						// Visualizza le informazioni del messaggio nella lista
						smsIncomingList.append(" SMS : " + message.getFromAddress() + ":" + message.getFromPort() +
								" -- " + timestamp +  " -- " + message.toString()+"\n");
					}
                    else {
						smsIncomingList.append(" SMS : " + message.getFromAddress() +
								" -- " + timestamp + " -- " + "Binary"+"\n");             	
                    }
                } else {
					smsIncomingList.append("CBS with message ID " + message.getToPort()+"\n");
                }
            }
        } catch (IOException ioe) {
            System.err.println("Caught executing:");
            ioe.printStackTrace();
        }
    }
}