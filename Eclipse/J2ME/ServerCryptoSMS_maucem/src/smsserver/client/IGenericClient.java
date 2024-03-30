package smsserver.client;

import java.io.IOException;


public interface IGenericClient  {
	
	String getPhoneNumber();
	boolean send(util.wma.BinaryMessage m)  throws IOException;
	//WMAMessage receive() throws IOException;;
}
