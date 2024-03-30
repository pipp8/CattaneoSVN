package smsserver.client;


import java.io.IOException;

import smsserver.MyMessage;

public interface IGenericClient  {
	
	String getPhoneNumber();
	void send(MyMessage m)  throws IOException;
	//WMAMessage receive() throws IOException;;
}
