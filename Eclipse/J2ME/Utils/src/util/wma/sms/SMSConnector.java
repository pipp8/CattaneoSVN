/*
 * @(#)SMSConnector.java	1.2 02/06/14 1.2
 * Copyright © 2002 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package util.wma.sms;
import util.wma.sms.SMSMessageConnection;

import java.io.IOException;

import util.wma.Connector;
import util.wma.MessageConnection;

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


