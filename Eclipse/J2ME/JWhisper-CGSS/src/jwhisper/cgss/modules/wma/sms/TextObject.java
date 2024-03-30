/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.cgss.modules.wma.sms;
import jwhisper.cgss.modules.wma.MessageConnection;
import jwhisper.cgss.modules.wma.TextMessage;
import jwhisper.cgss.modules.wma.sms.MessageObject;
import jwhisper.cgss.modules.wma.sms.TextEncoder;

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


