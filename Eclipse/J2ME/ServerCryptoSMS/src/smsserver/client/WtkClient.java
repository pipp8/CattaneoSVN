package smsserver.client;


import java.io.IOException;

import smsserver.MyMessage;
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
		
		wtkClient = WMAClientFactory.newWMAClient(null, WMAClient.SEND_AND_RECEIVE);

		try {
			wtkClient.connect();
		} catch (java.net.ConnectException e) {
			System.out.println("WTKClient: Connection refused. Try to disable the firewall. ");
		}
        wtkClient.setMessageListener(listener);
	}
	
	public String getPhoneNumber() {
		return wtkClient.getPhoneNumber();		
	}

	public void send(MyMessage msg) throws IOException  {
		WMAMessage wmaMsg = MessageUtils.Message2WMAMessage(msg);
		wtkClient.send(wmaMsg);
	}

}
