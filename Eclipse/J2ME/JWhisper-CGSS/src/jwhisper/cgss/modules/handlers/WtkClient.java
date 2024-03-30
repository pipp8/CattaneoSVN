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





import com.sun.kvem.midp.io.j2se.wma.WMAMessage;
import com.sun.kvem.midp.io.j2se.wma.client.PhoneNumberNotAvailableException;
import com.sun.kvem.midp.io.j2se.wma.client.WMAClient;
import com.sun.kvem.midp.io.j2se.wma.client.WMAClientFactory;
import com.sun.kvem.midp.io.j2se.wma.client.WMAClient.MessageListener;


/* WTK client */

public class WtkClient implements IGenericClient {
	private WMAClient wtkClient;
	
	public WtkClient(MessageListener listener) throws IOException, PhoneNumberNotAvailableException {
		
		wtkClient = WMAClientFactory.newWMAClient(CSettings.getNumberSMSC_WTK(), WMAClient.SEND_AND_RECEIVE);

		try {
			wtkClient.connect();
		} catch (java.net.ConnectException e) {
			CGSSLogger.getInstance().write("WTKClient: Connection refused. Try to disable the firewall. ");
		}
        wtkClient.setMessageListener(listener);
	}
	
	public String getPhoneNumber() {
		return wtkClient.getPhoneNumber();		
	}

	
	public boolean send (jwhisper.cgss.modules.wma.BinaryMessage m){		
		WMAMessage wmaMsg = MessageUtils.BinaryMessage2WMAMessage(m);
		boolean sent = false;
		try {
			wtkClient.send(wmaMsg);
			sent = true;
		} catch (IOException e) {
			sent = false;
		}
		return sent;
		
	}
	

}
