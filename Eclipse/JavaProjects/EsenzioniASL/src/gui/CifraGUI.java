package gui;
/*
 * $Revision: 1.5 $
 * $Date: 2009-07-08 16:51:03 $
 */
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Cursor;
import java.awt.Event;
import java.awt.BorderLayout;
import java.awt.ScrollPane;

import java.security.cert.X509Certificate;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.KeyStroke;
import java.awt.Point;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JMenuItem;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.JTextArea;
import java.awt.GridBagConstraints;
import javax.swing.JButton;
import java.awt.GridLayout;
import java.awt.Font;
import javax.swing.JTextField;

import crypto.CifraCFCert;

import java.awt.Insets;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CifraGUI implements ActionListener, Runnable {

	private JFrame jFrame = null;  //  @jve:decl-index=0:visual-constraint="10,10"
	private JPanel jContentPane = null;
	private JMenuBar jJMenuBar = null;
	private JMenu fileMenu = null;
	private JMenu editMenu = null;
	private JMenu helpMenu = null;
	private JMenuItem exitMenuItem = null;
	private JMenuItem aboutMenuItem = null;
	private JMenuItem cutMenuItem = null;
	private JMenuItem copyMenuItem = null;
	private JMenuItem pasteMenuItem = null;
	private JMenuItem saveMenuItem = null;
	private JDialog aboutDialog = null;
	private JPanel aboutContentPane = null;
	private JLabel aboutVersionLabel = null;
	private JButton btStart = null;
	private JButton btExit = null;
	private JLabel lblInput = null;
	private JLabel lblLog = null;
	private JTextArea txtLog = null;
	private JScrollPane spLog = null;
	private JTextField txtNumRecord = null;
	private JLabel lblRecord = null;
	private JFileChooser InFileChooser = null;
	private JFileChooser CertFileChooser = null;
	private JTextField txtInFileName = null;
	private JButton btChooseInputFile = null;
	private JLabel lblCert = null;
	private JTextField txtCertificato = null;
	private JButton btChooseCertificate = null; 
	private JProgressBar progressBar = null;
	
	private volatile Thread worker = null;
	private boolean bRunning = false;
	private File 	inFile= null;
	private String  outFileName = null;
	private String  CertPath = "";  //  @jve:decl-index=0:
	private JLabel lblLogo = null;
	private JLabel lblProgressivo = null;
	private JTextField txtProgressivoInvio = null;

	private CifraCFCert cf = new CifraCFCert();
	/**
	 * This method initializes jFrame
	 * 
	 * @return javax.swing.JFrame
	 */
	private JFrame getJFrame() {
		if (jFrame == null) {
			jFrame = new JFrame();
			jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jFrame.setJMenuBar(getJJMenuBar());
			jFrame.setSize(650, 490);
			jFrame.setContentPane(getJContentPane());
			jFrame.setTitle("Cifra codici fiscali");
		}
		return jFrame;
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints17.gridy = 6;
			gridBagConstraints17.weightx = 1.0;
			gridBagConstraints17.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints17.ipadx = 120;
			gridBagConstraints17.ipady = 2;
			gridBagConstraints17.gridx = 1;
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.gridx = 1;
			gridBagConstraints16.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints16.insets = new Insets(20, 4, 0, 0);
			gridBagConstraints16.gridy = 5;
			lblProgressivo = new JLabel();
			lblProgressivo.setText("Progressivo Invio");
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.fill = GridBagConstraints.BOTH;
			gridBagConstraints15.anchor = GridBagConstraints.NORTHEAST;
			gridBagConstraints15.gridx = 3;
			gridBagConstraints15.weightx = 0;
			gridBagConstraints15.weighty = 0;
			gridBagConstraints15.gridheight = 7	;
			gridBagConstraints15.gridwidth = 1;
			gridBagConstraints15.insets = new Insets(0, 0, 10, 10);
			gridBagConstraints15.gridy = 0;
			ImageIcon icon = createImageIcon("images/logo-asl-na-5-120.gif", "logo asl na 5");
			lblLogo = new JLabel(icon);
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints14.gridx = 2;
			gridBagConstraints14.gridy = 8;
			gridBagConstraints14.ipadx = 120;
			gridBagConstraints14.ipady = 2;
			gridBagConstraints14.gridwidth = 2;
			gridBagConstraints14.weightx = 0.0;
			gridBagConstraints14.anchor = GridBagConstraints.CENTER;
			gridBagConstraints14.insets = new Insets(0, 10, 0, 10);
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 2;
			gridBagConstraints13.gridy = 3;
			gridBagConstraints13.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints13.insets = new Insets(0, 10, 0, 0);
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints12.gridx = 1;
			gridBagConstraints12.gridy = 3;
			gridBagConstraints12.weightx = 0.0;
			gridBagConstraints12.ipadx = 120;
			gridBagConstraints12.ipady = 2;
			gridBagConstraints12.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints12.insets = new Insets(1, 4, 0, 0);
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 1;
			gridBagConstraints11.gridy = 2;
			gridBagConstraints11.anchor = GridBagConstraints.SOUTHWEST;
			gridBagConstraints11.insets = new Insets(1, 4, 0, 0);
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 2;
			gridBagConstraints10.gridy = 1;
			gridBagConstraints10.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints10.weightx = 50.0;
			gridBagConstraints10.insets = new Insets(0, 10, 0, 0);
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints9.gridx = 1;
			gridBagConstraints9.gridy = 1;
			gridBagConstraints9.weightx = 0.0;
			gridBagConstraints9.ipadx = 120;
			gridBagConstraints9.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints9.ipady = 2;
			gridBagConstraints9.insets = new Insets(1, 4, 0, 0);
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints8.gridx = 1;
			gridBagConstraints8.gridy = 8;
			gridBagConstraints8.ipadx = 120;
			gridBagConstraints8.ipady = 2;
			gridBagConstraints8.weightx = 0.0;
			gridBagConstraints8.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints8.insets = new Insets(1, 4, 0, 0);
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 1;
			gridBagConstraints7.gridy = 7;
			gridBagConstraints7.ipadx = 2;
			gridBagConstraints7.ipady = 5;
			gridBagConstraints7.anchor = GridBagConstraints.SOUTHWEST;
			gridBagConstraints7.insets = new Insets(20, 4, 0, 0);
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.insets = new Insets(0, 2, 0, 4);
			gridBagConstraints6.gridx = 6;
			gridBagConstraints6.gridy = 1;
			gridBagConstraints6.ipadx = 30;
			gridBagConstraints6.ipady = 2;
			gridBagConstraints6.fill = GridBagConstraints.BOTH;
			gridBagConstraints6.weightx = 1.0;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.fill = GridBagConstraints.BOTH;
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.gridy = 10;
			gridBagConstraints5.ipadx = 150;
			gridBagConstraints5.ipady = 57;
			gridBagConstraints5.weightx = 10.0;
			gridBagConstraints5.weighty = 1.0;
			gridBagConstraints5.gridwidth = 3;
			gridBagConstraints5.gridheight = 2;
			gridBagConstraints5.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints5.insets = new Insets(1, 4, 4, 10);
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.insets = new Insets(10, 4, 0, 0);
			gridBagConstraints4.gridy = 9;
			gridBagConstraints4.ipadx = 2;
			gridBagConstraints4.ipady = 5;
			gridBagConstraints4.anchor = GridBagConstraints.SOUTHWEST;
			gridBagConstraints4.gridx = 1;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.insets = new Insets(10, 4, 0, 0);
			gridBagConstraints3.gridy = 0;
			gridBagConstraints3.ipadx = 2;
			gridBagConstraints3.ipady = 5;
			gridBagConstraints3.anchor = GridBagConstraints.SOUTHWEST;
			gridBagConstraints3.gridx = 1;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.insets = new Insets(0, 2, 0, 4);
			gridBagConstraints2.gridx = 6;
			gridBagConstraints2.gridy = 3;
			gridBagConstraints2.ipadx = 30;
			gridBagConstraints2.ipady = 2;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.fill = GridBagConstraints.BOTH;
			lblRecord = new JLabel();
			lblRecord.setText("# Record Processati");
			lblLog = new JLabel();
			lblLog.setText("Output");
			lblInput = new JLabel();
			lblInput.setText("Input File Name");
			lblCert = new JLabel();
			lblCert.setText("Certificato del MEF");
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(getBtExit(), gridBagConstraints2);
			jContentPane.add(lblInput, gridBagConstraints3);
			jContentPane.add(lblLog, gridBagConstraints4);
			jContentPane.add(getSpLog(), gridBagConstraints5);
			jContentPane.add(getBtStart(), gridBagConstraints6);
			jContentPane.add(lblRecord, gridBagConstraints7);
			jContentPane.add(getTxtNumRecord(), gridBagConstraints8);
			jContentPane.add(getTxtInFileName(), gridBagConstraints9);
			jContentPane.add(getBtChooseInputFile(), gridBagConstraints10);
			jContentPane.add(lblCert, gridBagConstraints11);
			jContentPane.add(getTxtCertificato(), gridBagConstraints12);
			jContentPane.add(getBtFileChooser(), gridBagConstraints13);
			jContentPane.add(getProgressBar(), gridBagConstraints14);
			jContentPane.add(lblLogo, gridBagConstraints15);
			jContentPane.add(lblProgressivo, gridBagConstraints16);
			jContentPane.add(getTxtProgressivoInvio(), gridBagConstraints17);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jJMenuBar	
	 * 	
	 * @return javax.swing.JMenuBar	
	 */
	private JMenuBar getJJMenuBar() {
		if (jJMenuBar == null) {
			jJMenuBar = new JMenuBar();
			jJMenuBar.add(getFileMenu());
			jJMenuBar.add(getEditMenu());
			jJMenuBar.add(getHelpMenu());
		}
		return jJMenuBar;
	}

	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getFileMenu() {
		if (fileMenu == null) {
			fileMenu = new JMenu();
			fileMenu.setText("File");
			fileMenu.add(getSaveMenuItem());
			fileMenu.add(getExitMenuItem());
		}
		return fileMenu;
	}

	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getEditMenu() {
		if (editMenu == null) {
			editMenu = new JMenu();
			editMenu.setText("Edit");
			editMenu.add(getCutMenuItem());
			editMenu.add(getCopyMenuItem());
			editMenu.add(getPasteMenuItem());
		}
		return editMenu;
	}

	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getHelpMenu() {
		if (helpMenu == null) {
			helpMenu = new JMenu();
			helpMenu.setText("Help");
			helpMenu.add(getAboutMenuItem());
		}
		return helpMenu;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getExitMenuItem() {
		if (exitMenuItem == null) {
			exitMenuItem = new JMenuItem();
			exitMenuItem.setText("Exit");
			exitMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});
		}
		return exitMenuItem;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getAboutMenuItem() {
		if (aboutMenuItem == null) {
			aboutMenuItem = new JMenuItem();
			aboutMenuItem.setText("About");
			aboutMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JDialog aboutDialog = getAboutDialog();
					aboutDialog.pack();
					Point loc = getJFrame().getLocation();
					loc.translate(20, 20);
					aboutDialog.setLocation(loc);
					aboutDialog.setVisible(true);
				}
			});
		}
		return aboutMenuItem;
	}

	/**
	 * This method initializes aboutDialog	
	 * 	
	 * @return javax.swing.JDialog
	 */
	private JDialog getAboutDialog() {
		if (aboutDialog == null) {
			aboutDialog = new JDialog(getJFrame(), true);
			aboutDialog.setTitle("About");
			aboutDialog.setContentPane(getAboutContentPane());
		}
		return aboutDialog;
	}

	/**
	 * This method initializes aboutContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getAboutContentPane() {
		if (aboutContentPane == null) {
			aboutContentPane = new JPanel();
			aboutContentPane.setLayout(new BorderLayout());
			aboutContentPane.add(getAboutVersionLabel(), BorderLayout.CENTER);
		}
		return aboutContentPane;
	}

	/**
	 * This method initializes aboutVersionLabel	
	 * 	
	 * @return javax.swing.JLabel	
	 */
	private JLabel getAboutVersionLabel() {
		if (aboutVersionLabel == null) {
			aboutVersionLabel = new JLabel();
			aboutVersionLabel.setText("Version 1.0");
			aboutVersionLabel.setHorizontalAlignment(SwingConstants.CENTER);
		}
		return aboutVersionLabel;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getCutMenuItem() {
		if (cutMenuItem == null) {
			cutMenuItem = new JMenuItem();
			cutMenuItem.setText("Cut");
			cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
					Event.CTRL_MASK, true));
		}
		return cutMenuItem;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getCopyMenuItem() {
		if (copyMenuItem == null) {
			copyMenuItem = new JMenuItem();
			copyMenuItem.setText("Copy");
			copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
					Event.CTRL_MASK, true));
		}
		return copyMenuItem;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getPasteMenuItem() {
		if (pasteMenuItem == null) {
			pasteMenuItem = new JMenuItem();
			pasteMenuItem.setText("Paste");
			pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
					Event.CTRL_MASK, true));
		}
		return pasteMenuItem;
	}

	/**
	 * This method initializes jMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getSaveMenuItem() {
		if (saveMenuItem == null) {
			saveMenuItem = new JMenuItem();
			saveMenuItem.setText("Save");
			saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
					Event.CTRL_MASK, true));
		}
		return saveMenuItem;
	}

	/**
	 * This method initializes btStart	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtStart() {
		if (btStart == null) {
			btStart = new JButton();
			btStart.setText("Start");
			btStart.addActionListener(this);
			btStart.setEnabled(false);
		}
		return btStart;
	}

	/**
	 * This method initializes btExit	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtExit() {
		if (btExit == null) {
			btExit = new JButton();
			btExit.setText("Exit");
			btExit.addActionListener(this);
		}
		return btExit;
	}

	/**
	 * This method initializes txtLog	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JScrollPane getSpLog() {
		if (txtLog == null) {
			txtLog = new JTextArea();
			txtLog.setFont(new Font("Courier", Font.PLAIN, 11));
			txtLog.setText("Output log\n");
			txtLog.setEditable(false);
	        spLog = new JScrollPane(txtLog,
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		}
		return spLog;
	}

	/**
	 * This method initializes txtNumRecord	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTxtNumRecord() {
		if (txtNumRecord == null) {
			txtNumRecord = new JTextField();
		}
		return txtNumRecord;
	}

	private JFileChooser getInFileChooser() {
		if (InFileChooser == null) {
			InFileChooser = new JFileChooser();
			InFileChooser.setFileFilter(new TextFileFilter());
			InFileChooser.setCurrentDirectory(new File("."));
		}
		return InFileChooser;
	}

	private JFileChooser getCertFileChooser() {
		if (CertFileChooser == null) {
			CertFileChooser = new JFileChooser();
			CertFileChooser.setFileFilter(new CertFileFilter());
			CertFileChooser.setCurrentDirectory(new File("."));
		}
		return CertFileChooser;
	}

	/**
	 * This method initializes txtInFileName	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTxtInFileName() {
		if (txtInFileName == null) {
			txtInFileName = new JTextField();
			txtInFileName.addActionListener(this);
		}
		return txtInFileName;
	}

	/**
	 * This method initializes btOpenFile	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtChooseInputFile() {
		if (btChooseInputFile == null) {
			btChooseInputFile = new JButton();
			btChooseInputFile.setText("...");
			btChooseInputFile.setName("openFC");
			btChooseInputFile.addActionListener(this);
		}
		return btChooseInputFile;
	}
	/**
	 * This method initializes txtCertificato	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTxtCertificato() {
		if (txtCertificato == null) {
			txtCertificato = new JTextField();
			txtCertificato.addActionListener(this);
		}
		return txtCertificato;
	}

	/**
	 * This method initializes btFileChooser	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtFileChooser() {
		if (btChooseCertificate == null) {
			btChooseCertificate = new JButton();
			btChooseCertificate.setText("...");
			btChooseCertificate.addActionListener(this);
		}
		return btChooseCertificate;
	}

	private JProgressBar getProgressBar() {
		if (progressBar == null) {
			progressBar = new JProgressBar(0, 100);
	        progressBar.setValue(0);
	        progressBar.setStringPainted(true);
	        progressBar.setVisible(false);
		}
		return progressBar;
	}

	public void actionPerformed(ActionEvent e) {
        //Handle open button action.
        if (e.getSource() == btChooseInputFile) {
        	JFileChooser fc = getInFileChooser();
            int returnVal = fc.showOpenDialog(getJContentPane());
 
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                inFile = fc.getSelectedFile();
                setInputFile();
            }
            else
            {
                txtLog.append("Open command cancelled by user.\n");
            }
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        }
        else if (e.getSource() == btChooseCertificate) {
            	JFileChooser fc = getCertFileChooser(); 
                int returnVal = fc.showOpenDialog(getJContentPane());
 
                if (returnVal == JFileChooser.APPROVE_OPTION)
                {
                    CertPath = fc.getName(fc.getSelectedFile());
                    setCertificate();
                }
                else
                {
                    txtLog.append("Open command cancelled by user.\n");
                }
                txtLog.setCaretPosition(txtLog.getDocument().getLength());

        //Handle save button action.
        } else if (e.getSource() == btStart) {
            txtLog.append("Starting processing input file.\n");
            worker = new Thread(this);
            worker.start();
        }
		else if (e.getSource() == btExit) {
	        txtLog.append("User Request End. Exiting.\n");
	        bRunning = false;
	        if (worker != null && worker.isAlive()) {
	        	try {
	        		Thread.sleep(300);
	        		//worker.stop();
	        	} catch(Exception ex) {}
	        }
	        System.exit(-255);
		}
        else if (e.getSource() == txtInFileName) {
        	inFile = new File(txtInFileName.getText());
        	setInputFile();
        }
        else if (e.getSource() == txtCertificato) {
            CertPath = txtCertificato.getText();
            setCertificate();
        }
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    private void setInputFile() {
    	String in = inFile.getName();
	    outFileName = in.substring(0, in.lastIndexOf('.')) + "out.txt";
	    txtInFileName.setText(in);
	    txtLog.append("Input file: " + in + ".\n");
	    txtLog.append("Ouput file: " + outFileName + ".\n");
	    if (CertPath.length() > 0 && inFile != null)
	    	btStart.setEnabled(true);
	    else
	    	btStart.setEnabled(false);
    }

    private void setCertificate() {
    	txtCertificato.setText(CertPath);
	   
	    //This is where a real application would open the file.
	    txtLog.append("Using: " + CertPath + " as certificate.\n");
		X509Certificate cert = cf.initPubKey(CertPath);
		txtLog.append("Using Certificate:\n" + cert.getIssuerDN()+"\n");
		txtLog.append("Data Scadenza: " + cert.getNotAfter().toString()+"\n");
		
	    if (cert != null  && inFile != null)
	    	btStart.setEnabled(true);
	    else {
	    	btStart.setEnabled(false);
	    	txtCertificato.setText("");
	    }
    }
    
    
	public void run() {
						
		// cf.initPrivKey("keyStore", "123456", "mefpp2", "123456");	
		
		DataInputStream dis = null;
		DataOutputStream dos = null ;

	    final int recLen = 40;
	    final int CFLen = 16;
	    final int startCF = 0;
	    final int fineCF = 16;
	    
		byte[] buf = new byte[recLen];
		byte[] CFBuf;
		int letti;
	       
		try {
			dis = new DataInputStream( new FileInputStream( inFile));
			dos = new DataOutputStream(	new FileOutputStream( new File( outFileName)));
			// record di apertura
			dos.writeBytes("0150110ESE" + getProgressivoInvio() + "0000" + getCurrentDate()+ "0000000000000000\r\n");
		}
		catch(Exception e) {
			txtLog.append("Error opening file:" + e.getMessage());
			progressBar.setValue(0);
			progressBar.setVisible(false);
			jContentPane.setCursor(null);
			return;
		}
		
		btStart.setEnabled(false);
		jContentPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		progressBar.setVisible(true);

		int nRecStimati = (int) inFile.length() / recLen;
		progressBar.setMaximum(nRecStimati);
		bRunning = true;

		for(int i = 1; bRunning; i++) {
			try {
				letti = dis.read(buf);
				if (letti < recLen) {
					txtLog.append("End of file.\n");
					txtLog.append("Processati " + (i-1) + " record input.\n");
					break;
				}
				else if ((buf[recLen-2] != 13) || (buf[recLen-1] != 10)) {
					txtLog.append("Record n. " + i + " formato non riconosciuto:\n");
					txtLog.append("buf[recLen-2] = " + buf[recLen-2] + " buf[recLen-1] = " + buf[recLen-1] + ".\n");
					txtLog.append("Processing aborted.\n");
					break;
				}
				
				txtNumRecord.setText(String.valueOf(i) + " / " + String.valueOf(nRecStimati));

				CFBuf = estrai(buf, startCF, fineCF);
				
				if (i < 100) {
					txtLog.append("PlainText : " + new String(CFBuf) + ".\n");
				}
				
			    // encryption step
			    byte[] cipherText = cf.encrypt(CFBuf);
			    String ct = new String(cipherText);
			    
			    if (i < 100) {
			    	txtLog.append("cipherText (len = " + ct.length() + ") :\n" + ct + "\n");
			    	txtLog.setCaretPosition(txtLog.getDocument().getLength());
			    }
			    // scrittura record output
			    dos.writeBytes("1150110"); // prologo ... da dove vengono questi valori ?
			    dos.write(cipherText);
			    dos.write(buf, fineCF, recLen-fineCF); // scrive tutto il resto del record compreso terminatore \r\n
			    dos.flush();
			    progressBar.setValue(i);
			    // Thread.sleep(1500);
			}
			catch(EOFException e) {
				txtLog.append("Letti " + i + " record(s) input");
			}
			catch(IOException e) {
				txtLog.append("Record n." + i + " Errore di Input/Output\n" + e.getMessage());
			}
			catch(Exception e) {
				txtLog.append(e.getMessage());
				e.printStackTrace();
			}
		}
		try {			
			dis.close();
			// record di chiusura
			dos.writeBytes("0150110ESE" + getProgressivoInvio() + "0000" + getCurrentDate()+ "0000000000000000\r\n");
			dos.close();
		}
		catch (Exception e) {
			txtLog.append("Error closing files: " + e.getMessage());
		}
//	    byte[] plainText = cf.decrypt(cipherText);
//	    System.out.println("plain : " + new String(plainText));
		progressBar.setValue(0);
		progressBar.setVisible(false);
		jContentPane.setCursor(null);
	}
	
	/* Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon(String path,
	                                           String description) {
	    java.net.URL imgURL = CifraGUI.class.getResource(path);
	    if (imgURL != null) {
	        return new ImageIcon(imgURL, description);
	    } else {
	        System.err.println("Couldn't find file: " + path);
	        return null;
	    }
	}

	// estrae una sottostringa come substring dal byte "da" incluso al byte "a" escluso 
	public byte[] estrai(byte[] buf, int da, int a) {
		byte[] ret = new byte[a - da];
		for(int i = da; i < a; i++)
			ret[i - da] = buf[i];
		
		return ret;
	}

	public String getCurrentDate() {
		GregorianCalendar cd = new GregorianCalendar();
		String ret = "";
		ret = String.format("%04d", cd.get(Calendar.YEAR));
		ret += String.format("%02d", cd.get(Calendar.MONTH)+1);
		ret += String.format("%02d", cd.get(Calendar.DAY_OF_MONTH));
		
		return ret;
	}

	public String getProgressivoInvio() {
		int  prog = Integer.parseInt(txtProgressivoInvio.getText());
		return String.format("%03d", prog);
	}
	/**
	 * This method initializes txtProgrssivoInvio	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTxtProgressivoInvio() {
		if (txtProgressivoInvio == null) {
			txtProgressivoInvio = new JTextField();
			txtProgressivoInvio.setText("1");
		}
		return txtProgressivoInvio;
	}

	/**
	 * Launches this application
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				CifraGUI application = new CifraGUI();
				application.getJFrame().setVisible(true);
			}
		});
	}

}
