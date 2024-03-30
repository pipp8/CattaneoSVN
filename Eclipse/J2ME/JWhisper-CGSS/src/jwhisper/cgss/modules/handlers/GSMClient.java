/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.cgss.modules.handlers;


import java.io.IOException;

import jwhisper.cgss.CSettings;
import jwhisper.cgss.modules.handlers.IGenericClient;
import jwhisper.cgss.utils.CGSSLogger;
import jwhisper.cgss.utils.MessageUtils;

import org.smslib.CIncomingMessage;
import org.smslib.COutgoingMessage;
import org.smslib.CService;






/* GSM CLient implemented using smslib */
public class GSMClient extends Thread implements IGenericClient {
	
	
	private CService srv;
	private org.smslib.ISmsMessageListener _listener;
	
	private String _phoneNumber;
	public GSMClient(org.smslib.ISmsMessageListener listener) {
		_listener = listener;
		_phoneNumber = CSettings.getNumberSMSC_GSM();
	}


	public void connect() throws Exception{
		// String str = "GSMModem connected on port " + CConstants.GSM_MODEM_COM_PORT;
	
		// Define the CService object. 
		srv = new CService(CSettings.getGsmModemComPort(), 57600, "Nokia", "");
		
		srv.setLogger(null);

		// srv.setSimPin("0000");

		// Get the SMSC number information from SIM card
		srv.setSmscNumber("");

		// OK, let connect and see what happens...
		srv.connect();
	}
	
	public String getPhoneNumber() {
		return _phoneNumber;
	}

	public boolean send(jwhisper.cgss.modules.wma.BinaryMessage msg) throws IOException  {
		
		COutgoingMessage out = MessageUtils.BinaryMessage2COutgoingMessage(msg);
		boolean sent = false;
		try {		
			
			srv.sendMessage(out);
			sent = true;
			CGSSLogger.getInstance().write("Message sent to " + out.getRecipient());
		} catch (Exception e) {
			sent = false;
			CGSSLogger.getInstance().write("Error: impossible to send sms to " + out.getRecipient());
		}
		return sent;
	
	}

	// Thread in ascolto dei messaggi in ingresso. 
	// Utilizzo del thread per parallelizzare 
	// l'ascolto insime all'emulatore
	public void run() {
	
		// Set the message callback class.
		srv.setMessageHandler(_listener);

		// Set the polling interval in seconds.
		//srv.setAsyncPollInterval(10);
		srv.setAsyncPollInterval(100000);

		// Set the class of the messages to be read.
		//srv.setAsyncRecvClass(CIncomingMessage.MessageClass.All);
		srv.setAsyncRecvClass(CIncomingMessage.MessageClass.Unread);
//		srv.setAsyncRecvClass(CIncomingMessage.MessageClass.Read);
			
		try {
			// Switch to asynchronous POLL mode.
			srv.setReceiveMode(CService.ReceiveMode.AsyncPoll);
			// Or do you want to switch to CNMI mode???
			// srv.setReceiveMode(CService.ReceiveMode.AsyncCnmi);
		} catch (Exception e1) {
			CGSSLogger.getInstance().write("Error: impossible to set the receive mode");			
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
