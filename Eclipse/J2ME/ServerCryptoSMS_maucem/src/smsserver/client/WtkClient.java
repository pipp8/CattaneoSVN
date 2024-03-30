package smsserver.client;


import java.io.IOException;

import org.cryptosms.helpers.Logger;


import smsserver.CSettings;
import smsserver.utils.MessageUtils;

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
			Logger.getInstance().write("WTKClient: Connection refused. Try to disable the firewall. ");
		}
        wtkClient.setMessageListener(listener);
	}
	
	public String getPhoneNumber() {
		return wtkClient.getPhoneNumber();		
	}

	
	public boolean send (util.wma.BinaryMessage m){		
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
