/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package crymes.server.gui;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.util.Properties;

import javax.swing.Timer;


import crymes.book.CMessage;
import crymes.server.CSettings;
import crymes.server.Engine;
import crymes.server.GUIControl;
import crymes.server.gui.AddressListPanel;
import crymes.server.gui.ConnectionStatusPanel;
import crymes.server.gui.PassphraseDialog;
import crymes.server.gui.SetupPanel;
import crymes.server.gui.SmsPanel;
import crymes.server.gui.logPanel;
import crymes.util.helpers.CryptedRecordStore;
import crymes.util.helpers.Logger;
import crymes.util.recordstore.RecordStore;
import crymes.util.recordstore.RecordStoreException;




@SuppressWarnings("serial")
public class MainFrame extends javax.swing.JFrame  implements ActionListener{
    
    private javax.swing.JPanel connectionStatusPanel;
    private javax.swing.JTabbedPane jTabbedPane;
    private logPanel messagePanel;
	
	private final int SMS_TAB_POSITION = 0;
	private final int ADDRESSLIST_TAB_POSITION = 1;
	private final int SETUP_TAB_POSITION = 2;
	private int currentTab = -1;
	
	private static PassphraseDialog passphraseDialog = null;
	private static Timer _timer;
	private static boolean checkPassphrase = true;
	
	private AddressListPanel _addressListPanel;
	private SmsPanel _smsPanel;
	
	private static MainFrame _mf;
	private Engine _engine;
    private static Thread _gcThread;

    public MainFrame() {
    	_mf = this;
    	
    	// non commentare
    	@SuppressWarnings("unused")
		Logger log = Logger.getInstance(this);
      	init();
    }
    
       
    private void init(){
    	if (CSettings.isTimerPassphraseEnabled()) {
    		_timer = new Timer(CSettings.getTimerPassphraseDelay()*1000, this);
    	}

    	// controllo connessioni
        GUIControl gc = new GUIControl(this);
        _gcThread = new Thread(gc);
        
        _engine = new Engine(this);
        gc.setEngine (_engine);
        
        
  	  // disegna la gui
        initComponents();
        
        // setup o login... e init di conseguenza
        startApplication();
    
        // Questo thread permette di chiudere gli stores e serializzarli
        Thread t = new Thread(){
        	public void run() {
        		String [] stores=RecordStore.listRecordStores();
        		RecordStore rs ;
        		for (int i = 0; i < stores.length; i++) {        			
        			try {
						rs = RecordStore.openRecordStore(stores[i], false);
						rs.closeRecordStore();
					} catch (RecordStoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}        			
				}
			}
        };
        Runtime.getRuntime().addShutdownHook(t);
        
        
    }
   

    private void initComponents() {

    	jTabbedPane = new javax.swing.JTabbedPane();
    	
    	_addressListPanel = new AddressListPanel(_engine);
    	_smsPanel = new SmsPanel(_engine);
    	jTabbedPane.insertTab("SMS", null, _smsPanel , "", SMS_TAB_POSITION);
    	jTabbedPane.insertTab("Address List", null, _addressListPanel, "", ADDRESSLIST_TAB_POSITION);
    	jTabbedPane.insertTab("Setup", null, new SetupPanel(_engine), "", SETUP_TAB_POSITION);
    	currentTab = jTabbedPane.getSelectedIndex();
    	
    	 jTabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jTabbedPaneStateChanged(evt);
             }
         });
    	
        connectionStatusPanel = new ConnectionStatusPanel();
        //connectionStatusPanel.add(new connectionStatusPanel());
        messagePanel = new logPanel(this);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        
        
        setResizable(false);
        
        setTitle("JWhisper - DIA  Dipartimento di Informatica ed Applicazioni \"Renato M. Capocelli\"");


        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(connectionStatusPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jTabbedPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 700, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(messagePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(connectionStatusPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(14, 14, 14)
                .add(jTabbedPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 400, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(messagePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(19, 19, 19))
        );
        pack();
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
    	
    	// Leggo il file di properties
    	readConfig();
    	System.setProperty("kvem.home", CSettings.getKvemHome());
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
                new MainFrame().setVisible(true);
                if (checkPassphrase) 
                	checkPassphrase();
                
//            }
//        });
    }
    

    public void setActiveTabs(boolean firstExecution){
    	jTabbedPane.setEnabledAt(SMS_TAB_POSITION, !firstExecution);
    	jTabbedPane.setEnabledAt(ADDRESSLIST_TAB_POSITION, !firstExecution);
    	jTabbedPane.setEnabledAt(SETUP_TAB_POSITION, firstExecution);
    	
    	if (!firstExecution) jTabbedPane.setSelectedIndex(SMS_TAB_POSITION);
    }
    
    public void startPassphraseTimer(){
    	if (CSettings.isTimerPassphraseEnabled())
			_timer.start();		
    	
    }
    
    private void updateRecipientList(){
    	_smsPanel.updateRecipientList();
    }
    
 public void startApplication(){
	 
	 	boolean secondExecution = CryptedRecordStore.hasPass(); 
	  	setActiveTabs(!secondExecution);
    	
    	if (secondExecution){
    		// login
    		passphraseDialog = PassphraseDialog.getInstance(this);
    		Point p = this.getLocation();
    		int y = ((p.y + this.getHeight()) / 2) - (passphraseDialog.getHeight()/2); 
    		int x = ((p.x + this.getWidth()) / 2) - (passphraseDialog.getWidth()/2);
    		passphraseDialog.setLocation(x,y);
    		checkPassphrase = true;
    	} else {
    		// setup
    		checkPassphrase = false;
    		jTabbedPane.setSelectedIndex(SETUP_TAB_POSITION);	
    	}
    }

    
 	// quando cambia il tab selezionato
    private void jTabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {
    	if (jTabbedPane.getSelectedIndex() != currentTab){
    		currentTab = jTabbedPane.getSelectedIndex();
    	}

    	//action restartTimer();
    	restartTimer();
    	
    }
    
    public static void restartTimer(){
    	if (CSettings.isTimerPassphraseEnabled())
        	_timer.restart();
    	
    }
    // controllo della passphrase
    private static void checkPassphrase(){
    	if (CSettings.isTimerPassphraseEnabled())
    		_timer.stop();
   
    	passphraseDialog = PassphraseDialog.getInstance(_mf);
    	
    	Point p = _mf.getLocation();
		int y = ((p.y + _mf.getHeight()) / 2) - (passphraseDialog.getHeight()/2); 
		int x = ((p.x + _mf.getWidth()) / 2) - (passphraseDialog.getWidth()/2);
		passphraseDialog.setLocation(x,y);
		passphraseDialog.setVisible(true);    	
    }
    
    
    // callback della form con la passphrase
    protected void passphraseDialogCallback(String str) {
    	boolean passValid = true;
    	try{
			_engine.initAddressService(str);
		}catch(Exception e){
			passValid = false;
		}

    	if (passValid){
    		startGUIControl();
    		
    		_engine.finishInit();
    		updateAddressListTable();
    		updateRecipientList();
    		
			passphraseDialog.setVisible(false);
			passphraseDialog.callbackPassphrase(true);
			if (CSettings.isTimerPassphraseEnabled())
				_timer.start();			
		} else {
			passphraseDialog = PassphraseDialog.getInstance(this);
    		passphraseDialog.setVisible(true);
		}
	}


   public void updateAddressListTable() {
    	_addressListPanel.updateTable();
    	updateRecipientList();
	}

   public void addRowToMessageList(CMessage m) {
   		_smsPanel.addRowToMessageList(m);
	}

	public void addMessageLog(String s){
		if (messagePanel != null)
			messagePanel.addMessageLog(s);
		
	 }
   
    // L'action del timer
	public void actionPerformed(ActionEvent e) {
		checkPassphrase();
	}
	
	public static void startGUIControl(){
		if (!_gcThread.isAlive())
			_gcThread.start();
	}
    
	
	
	 public void setStatusEmulator(boolean connected, String number){
		 ((ConnectionStatusPanel)connectionStatusPanel).setStatusEmulator(connected, number);
	 }
	 public void setStatusGSMModem(boolean connected, String number){
		 ((ConnectionStatusPanel)connectionStatusPanel).setStatusGSMModem(connected, number);
	 }
	 public void setSignalGSMModem(int value){
		 ((ConnectionStatusPanel)connectionStatusPanel).setSignalGSMModem(value);
	 }
	 
	 
	 private static void readConfig() {
			try {
				Properties p = new Properties();
				p.load(new FileInputStream("config.ini"));
				
				CSettings.setKvemHome(p.getProperty("KVEM_HOME"));
//				CSettings.setNumberSMSC_GSM(p.getProperty("NUMBER_SMSC_GSM"));
				
				CSettings.setNumberSMSC_WTK(p.getProperty("NUMBER_SMSC_EMULATOR"));
				CSettings.setGsmModemComPort(p.getProperty("GSM_MODEM_COM_PORT"));
				
				CSettings.setTimerPassphraseEnabled(p.getProperty("TIMER_PROMPT_PASSPHRASE_ENABLED"));
				CSettings.setTimerPassphraseDelay(p.getProperty("TIMER_PROMPT_PASSPHRASE_DELAY"));
				
				
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	 
	 
}
