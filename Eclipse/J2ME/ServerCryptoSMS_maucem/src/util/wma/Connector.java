/*
 * @(#)Connector.java	1.7 02/06/14 1.7
 * Copyright © 2002 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package util.wma;
import java.io.IOException;

/**
 * A factory class for creating new MessageConnection objects.
 */

public interface Connector {

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
    public MessageConnection open(String name) throws IOException;
}


