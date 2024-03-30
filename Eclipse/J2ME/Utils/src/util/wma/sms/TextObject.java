/*
 * @(#)TextObject.java	1.5 02/06/27 1.5
 * Copyright © 2002 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package util.wma.sms;
import util.wma.sms.MessageObject;

import java.io.*;

import util.wma.*;

import util.wma.sms.TextEncoder;

/**
 * Implements an instance of a text message.
 */
class TextObject extends MessageObject
    implements TextMessage {

    /** Buffer to be used. */
    byte[] buffer;

    /**
     * Construct a text specific message.
     * @param  addr the destination address of the message.
     */
    public TextObject(String addr) {
	super(MessageConnection.TEXT_MESSAGE, addr);
    }

    /** 
     * Returns the message payload data as a <code>String</code>.
     *
     * @return the payload of this message, or <code>null</code> 
     * if the payload for the message is not set.
     * @see #setPayloadText
     */
    public String getPayloadText() {
	if (buffer == null) {
	    return null;
	}
	return TextEncoder.toString(buffer);
    }

    /**
     * Sets the payload data of this message. The payload data 
     * may be <code>null</code>.
     * @param data payload data as a <code>String</code>
     * @see #getPayloadText
     */
    public void setPayloadText(String data) {
	if (data != null) {
	    buffer = TextEncoder.toByteArray(data);
	} else {
	    buffer = null;
	}
	return;
    }

    /**
     * Gets the raw byte array.
     * @return an array of raw UCS-2 payload data
     * @see #getPayloadText
     * @see #setBytes
     */
    byte[] getBytes() {
	return buffer;
    }

    /**
     * Sets the raw byte array.
     * @param data an array of raw UCS-2 payload data.
     * @see #getBytes
     */
    void setBytes(byte[] data) {
	buffer = data;
    }

}


