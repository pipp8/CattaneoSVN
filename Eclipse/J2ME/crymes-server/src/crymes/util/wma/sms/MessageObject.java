/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package crymes.util.wma.sms;
import java.util.Date;

import crymes.util.wma.Message;
import crymes.util.wma.MessageConnection;


/**
 * Implements a SMS message for the SMS message connection. This class
 * contains methods for manipulating message objects and their contents.
 * Messages can be composed of data and an address. MessageObject contains 
 * methods that can get and set the data and the address parts of a message 
 * separately. The data part can be either
 * text or binary format. The address part has the format:
 * <p> 
 * <tt>sms://</tt>[<em>phone_number</em><tt>:</tt>][<em>port_number</em>]
 * <p>
 * and represents the address of a port that can accept or 
 * receive SMS messages. 
 *<p>
 * <tt>MessageObject</tt>s are instantiated when they are received from the
 * {@link crymes.util.wma.MessageConnection MessageConnection} 
 * or by using the 
 * {@link MessageConnection#newMessage(String type)
 *  MessageConnection.newMessage} 
 * message factory. Instances are freed when they are garbage collected or 
 * when they go out of scope. 
 */
class MessageObject  implements Message {

    /** High level message type. */
    String messtype;

    /** High level message address. */
    String messaddr;

    /** Timestamp when the message was sent. */
    long sentAt;

    /**
     * Creates a Message Object without a buffer.
     * @param type text or binary message type.
     * @param  addr the destination address of the message.
     *
     */
    public MessageObject(String type, String addr) {
	messtype = type;
	messaddr = addr;


    }

    /**
     * Gets the address from the message object as a <tt>String</tt>. If no 
     * address is found in the message, this method returns 
     *<code>null</code>. If the method
     * is applied to an inbound message, the source address is returned. 
     * If it is applied to an outbound message, the destination addess 
     * is returned. 
     * <p>
     * The following code sample retrieves the address from a received
     *  message.
     * <pre>
     *    ...
     *    Message msg = conn.receive();
     *    String addr = msg.getAddress();
     *    ...
     * </pre>
     * @return the address in string form, or <code>null</code> if no 
     *         address was set
     *
     * @see #setAddress
     */
    public String getAddress() {
	return messaddr;
    }

    /**
     * Sets the address part of the message object. The address is a 
     * <tt>String</tt> and must be in the format:  
     * <tt>sms://</tt>[<em>phone_number</em><tt>:</tt>][<em>port</em>]. The 
     * following code sample assigns an SMS URL address to the 
     * <tt>Message</tt> object. 
     * <pre>
     *    ...
     *    String addr = "sms://+358401234567";
     *    Message msg = newMessage(TEXT_MESSAGE); 
     *    msg.setAddress(addr);
     *    ...
     * </pre>
     * <p>
     * @param addr address of the target device. 
     * @exception IllegalArgumentException if the address is not valid.
     *
     * @see #getAddress
     */
    public void setAddress(String addr) {

	if (!addr.startsWith("sms://") && !addr.startsWith("cbs://")) {
	    throw new IllegalArgumentException("Invalid scheme");
	}
	
	messaddr = addr;
    }

    /**
     * (May be deleted) Set message address, copying the address 
     * from another message.
     *
     * @param reference the message who's address will be copied as
     * the new target address for this message.
     * @exception IllegalArgumentException if the address is not valid
     *
     * @see #getAddress
     */
    public void setAddress(Message reference) {
	setAddress(reference.getAddress());
    }

    /**
     * Returns the timestamp indicating when this message has been
     * sent.  
     * 
     * @return Date indicating the timestamp in the message or
     *         <code>null</code> if the timestamp is not set.
     */
    public java.util.Date getTimestamp() {
	return new Date(sentAt); // NYI 
    }

    /**
     * Sets the timestamp for inbound SMS messages.
     *
     * @param timestamp  the date indicating the timestamp in the message 
     * @see #getTimeStamp
     */
    public void setTimeStamp(long timestamp) {
	sentAt = timestamp;
    }

}


