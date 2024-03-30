package smsserver.client;


import java.io.IOException;
import org.smslib.CIncomingMessage;
import org.smslib.CMessage;
import org.smslib.COutgoingMessage;
import org.smslib.CService;

import smsserver.CConstants;
import smsserver.GUIControl;
import smsserver.MyMessage;


/* GSM CLient implemented using smslib */
public class GSMClient extends Thread implements IGenericClient {
	
	private GUIControl _guiControl;
	private CService srv;
	private org.smslib.ISmsMessageListener _listener;
	
	public GSMClient(org.smslib.ISmsMessageListener listener, GUIControl gc) {
		_guiControl = gc;
		_listener = listener;
	}


	public void connect() throws Exception{
		// String str = "GSMModem connected on port " + CConstants.GSM_MODEM_COM_PORT;
	
		// Define the CService object. 
		srv = new CService(CConstants.GSM_MODEM_COM_PORT, 57600, "Nokia", "");
		
		srv.setLogger(null);

		// srv.setSimPin("0000");

		// Get the SMSC number information from SIM card
		srv.setSmscNumber("");

		// OK, let connect and see what happens...
		srv.connect();
	}
	
	public String getPhoneNumber() {
		return CConstants.SIM_WIND_MAUCEM;
	}

	public void send(MyMessage msg) throws IOException  {
		
		String recipient = msg.getReceiver(); // only the number (+39333536735) !!!
		String txt = msg.getText();
		COutgoingMessage msgOut = new COutgoingMessage(recipient, txt);
		
		msgOut.setMessageEncoding(CMessage.MessageEncoding.Enc7Bit);

		msgOut.setStatusReport(false);

		// 8 hours
		msgOut.setValidityPeriod(8);

		// msg.setFlashSms(true);

		//msg.setSourcePort(16500);
		//msg.setDestinationPort(16500);
		
		try {
			srv.sendMessage(msgOut);
			_guiControl.addMessageLog("Message sent to " + recipient);
		} catch (Exception e) {
			_guiControl.addMessageLog("Error: impossible to send sms to " + recipient);
		}
		
		
	}

	// Thread in ascolto dei messaggi in ingresso. 
	// Utilizzo del thread per parallelizzare 
	// l'ascolto insime all'emulatore
	public void run() {
	
		// Set the message callback class.
		srv.setMessageHandler(_listener);

		// Set the polling interval in seconds.
		srv.setAsyncPollInterval(10);

		// Set the class of the messages to be read.
		srv.setAsyncRecvClass(CIncomingMessage.MessageClass.All);
//		srv.setAsyncRecvClass(CIncomingMessage.MessageClass.Unread);
//		srv.setAsyncRecvClass(CIncomingMessage.MessageClass.Read);
			
		try {
			// Switch to asynchronous POLL mode.
			srv.setReceiveMode(CService.ReceiveMode.AsyncPoll);
			// Or do you want to switch to CNMI mode???
			// srv.setReceiveMode(CService.ReceiveMode.AsyncCnmi);
		} catch (Exception e1) {
			_guiControl.addMessageLog("Error: impossible to set the receive mode");			
		}
	}

	public void terminate(){
		try {
			//TODO la disconnect secondo me non viene mai chiamata
			srv.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int getGSMModemSignalLevel(){
		return srv.getDeviceInfo().getSignalLevel();
	}
}
