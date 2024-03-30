/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package crymes.util.wma.sms;
import crymes.util.wma.BinaryMessage;
import crymes.util.wma.MessageConnection;
import crymes.util.wma.sms.MessageObject;


/**
 * Implements an instance of a binary message.
 */
public class BinaryObject extends MessageObject
    implements BinaryMessage {

    /** Buffer to be used. */
    byte[] buffer;

    /**
     * Construct a binary specific message.
     * @param  addr the destination address of the message.
     */
    public BinaryObject(String addr) {
	super(MessageConnection.BINARY_MESSAGE, addr);
    }

    /** 
     * Returns the message payload data as an array
     * of bytes.
     * 
     * <p>Returns <code>null</code>, if the payload for the message 
     * is not set.
     * </p>
     * <p>The returned byte array is a reference to the 
     * byte array of this message and the same reference
     * is returned for all calls to this method made before the
     * next call to <code>setPayloadData</code>.
     *
     * @return the payload data of this message, or
     * <code>null</code> if the data has not been set 
     * @see #setPayloadData
     */
    public byte[] getPayloadData() {
	return buffer;
    }

    /**
     * Sets the payload data of this message. The payload may
     * be set to <code>null</code>.
     * <p>Setting the payload using this method only sets the 
     * reference to the byte array. Changes made to the contents
     * of the byte array subsequently affect the contents of this
     * <code>BinaryMessage</code> object. Therefore, applications
     * shouldn't reuse this byte array before the message is sent and the
     * <code>MessageConnection.send</code> method returns.
     * </p>
     * @param data payload data as a byte array
     * @see #getPayloadData
     */
    public void setPayloadData(byte[] data) {
	buffer = data;
	return;
    }

}


