package smsserver;


import java.io.*;

import com.sun.kvem.midp.io.j2se.wma.client.PhoneNumberNotAvailableException;

import smsserver.client.GSMClient;
import smsserver.client.IGenericClient;
import smsserver.client.WtkClient;
import smsserver.listener.CIncomingMessageListener;
import smsserver.listener.ISmsMessageListener;


@SuppressWarnings("serial")
public class Engine  implements ISmsMessageListener{

	private GUIControl _guiControl;
	
	CIncomingMessageListener _listener;

	private WtkClient wtkClient;
    private GSMClient gsmClient;
    
    
	public Engine(GUIControl gc) {
		
			_guiControl = gc;
			
			_listener = new CIncomingMessageListener(this);
			_guiControl.addMessageLog("CIncomingMessageListener create");
	}
	

	// Invia un sms utilizzando il msg in input e il client specificato
	public void sendSms(MyMessage msg, IGenericClient gClient) {
        try {
            gClient.send(msg);
        } catch (Exception e) {            
            _guiControl.addMessageLog("Exception sending sms.");
        }
	}
	
	// Callback dell'ISmsMessageListener
	public void notifyIncomingMessage(MyMessage msg, boolean isEmulator)  {

		
		IGenericClient gClient = null;
	    if (isEmulator){
	    	gClient = wtkClient;
	    } else {
	    	gClient = gsmClient;
	    }
	
		_guiControl.addSMS(msg);
	
		// Test risposta automatica
		// Risposta al mittente dopo 10 sec se emulatore
		if (isEmulator) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}

		MyMessage message = new MyMessage();

		message.setSender(gClient.getPhoneNumber());
		String destAddress = "sms://" + msg.getSender() + ":" + CConstants.DEFAULT_SMS_PORT;		
		message.setReceiver(destAddress);
		message.setText("Thank You!");
		
		sendSms(message, gClient);
	}


	// Tenta la connessione al client wireless
	public boolean createWTKConnection(){
		boolean b = true;
		try {
			wtkClient = new WtkClient(_listener);
			_guiControl.addMessageLog("Emulator connected");
		} catch (IOException e) {
			_guiControl.addMessageLog("Impossibile connettersi all'emulatore");
			wtkClient =  null;
			b = false;
		} catch (PhoneNumberNotAvailableException e) {
			_guiControl.addMessageLog("Impossibile specificare il numero telefonico");
			wtkClient =  null;
			b = false;
		}
		return b;
	}

	// Tenta la connessione al modem gsm
	public boolean createGSMModemConnection(){
		boolean b = true;
		String str = "GSMModem connected on " + CConstants.GSM_MODEM_COM_PORT;
	
		gsmClient = new GSMClient(_listener, _guiControl); //cell
		try {
			gsmClient.connect();
			_guiControl.addMessageLog(str);	
		} catch (javax.comm.NoSuchPortException e) {
			str = "GSMClient: NoSuchPortException. Insert the modem. ";
			b = false;
		} catch (javax.comm.PortInUseException e){
			str = "GSMClient: Port currently owned by another process. ";
			b = false;
		} catch (Exception e) {
			str = "GSMClient: Connection refused. ";
			b = false;
		}

		if (b)
			gsmClient.start();
		else
			gsmClient = null;

		//_guiControl.addMessageLog(str);
	
		return b;
	}

	public WtkClient getWtkClient(){
		return wtkClient;
	}
	
	public GSMClient getGSMClient(){
		return gsmClient;
	}
}

