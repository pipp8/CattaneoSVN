/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.cgss.modules.wma.sms;

import java.io.IOException;

import jwhisper.cgss.modules.wma.Connector;
import jwhisper.cgss.modules.wma.MessageConnection;
import jwhisper.cgss.modules.wma.sms.SMSMessageConnection;



/**
 * A factory class for creating new MessageConnection objects.
 */

public class SMSConnector implements Connector {

    /**
     * Creates and opens a MessageConnection.
     *
     * @param name             the URL for the connection.
     * @return                 a new MessageConnection object.
     *
     * @exception IllegalArgumentException if a parameter is invalid.
     * @exception ConnectionNotFoundException if the requested connection
     *   cannot be made, or the protocol type does not exist.
     * @exception IOException  if some other kind of I/O error occurs.
     * @exception SecurityException if a requested protocol handler is not
     *             permitted
     */
    public MessageConnection open(String name) throws IOException {

	/* Check for a valid url string. */

	if (name.startsWith("sms:") || name.startsWith("cbs:")) {
	    SMSMessageConnection handler = new SMSMessageConnection();

	    String namefrag = name.substring(4);
	    handler.openPrim(namefrag);
	    
	    return handler;
	} else {
	    throw new IllegalArgumentException("Wrong protocol scheme");
	}
    }
}


