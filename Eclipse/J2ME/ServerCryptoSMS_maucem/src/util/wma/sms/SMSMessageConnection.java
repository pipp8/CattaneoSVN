/*
 * @(#)SMSMessageConnection.java	1.17 03/01/31 1.17
 * Copyright © 2002-2003 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package util.wma.sms;
import util.wma.sms.BinaryObject;
import util.wma.sms.MessageObject;
import util.wma.sms.TextObject;

import java.io.*;
import java.net.*;
import java.util.*;

import util.wma.*;

import util.wma.sms.DatagramRecord;
import util.wma.sms.TextEncoder;
/**
 * SMS message connection handler.
 */
public class SMSMessageConnection implements MessageConnection {

    /** Machine name - the parsed target address from the url. */
    protected String host = null;

    /** Port number from the url connection string. */
    protected String port;

    /** SMS Port number reserved for CBS messages. */
    protected String CBSPort;

    /** Datagram host for sending/receivin. */
    protected String clientHost;

    /** Datagram transport for sending. */
    protected int portOut;

    /** Datagram transport for sending. */
    protected int portIn;

    /** Phone number of the message sender. */
    protected String phoneNumber;

    /** Fragment size for large messages. */
    int fragmentsize;

    /** Datagram server connection. */
    DatagramSocket dgc; 

    /** Datagram buffer. */
    byte [] buf = new byte[1500];
    
    /** Datagram envelope for sending or receiving messages. */
    DatagramPacket mess = new DatagramPacket(buf, 1500);

    /**
     * Open flag indicates when the connection is closed, an subsequent
     * operations should throws an exception.
     */
    protected boolean open;
    /** Constructor for SMS message connection handling. */
    public SMSMessageConnection() {
	/* 
	 * Configurable parameters for low level transport.
	 * e.g. sms://12345:54321 maps to datagram://129.148.70.80:123
	 */

	CBSPort = "24680";
	clientHost = "localhost";
	portOut = 54321;
	portIn = 12345;
	phoneNumber = "+123456789";

	/* 
	 * Check for overrides in the "connections.prop"
	 * configuration file.
	 */

	try {
	    String temp;
	    Properties props = new Properties();
	    props.load(new FileInputStream("connections.prop"));
	    
	    temp = props.getProperty("CBSPort");
	    if (temp != null) {
		CBSPort = temp;
	    }
	    
	    temp = props.getProperty("DatagramHost");
	    if (temp != null) {
		clientHost = temp;
	    }
	    
	    temp = props.getProperty("DatagramPortOut");
	    if (temp != null) {
		try {
		    portOut = Integer.parseInt(temp);
		} catch (NumberFormatException nfe) {
		    // If there was no property file use defaults
		}
	    }

	    temp = props.getProperty("DatagramPortIn");
	    if (temp != null) {
		try {
		    portIn = Integer.parseInt(temp);
		} catch (NumberFormatException nfe) {
		    // If there was no property file use defaults
		}

	    }

	    temp = props.getProperty("PhoneNumber");
	    if (temp != null) {
		phoneNumber = temp;
	    }
	    
	} catch (IOException ioe) {
	    // If there was no property file use defaults
	}
    }
    /**
     * Opens a connection. This method is called from 
     * <code>Connector.open()</code> method to obtain the destination
     * address given in the <tt>name</tt> parameter.
     * <p>
     * The format for the <tt>name</tt> string for this method is:
     * <tt>sms://</tt><em>[phone_number</em><tt>:</tt><em>][port_number]</em> 
     * where the <em>phone_number:</em> is optional. 
     * If the <em>phone_number</em> 
     * parameter is present, the connection is being opened in 
     * "client" mode. This means that messages can be sent. 
     * If the parameter is absent, the connection is being opened in 
     * "server" mode. This means that messages can be sent and received.  
     * <p>
     * The connection that is opened is to a low-level transport mechanism
     * which can be any of the following:
     * <ul>
     * <li>a datagram Short Message Peer to Peer (SMPP) 
     * to a service center </li>
     * <li>a <tt>comm</tt> connection to a phone device with AT-commands</li>
     * <li>a native SMS stack</li>
     *  </ul>
     *
     * @param name the target of the connection
     * @return this connection
     * @throws IOException if the connection is closed or unavailable.
     */
    public MessageConnection openPrim(String name)
        throws IOException {
	/*
	 * If <code>host == null</code>, then we are a server endpoint at
	 * the supplied <code>port</code>.
	 *
	 * If <code>host != null</code> we are a client endpoint at a port
	 * decided by the system and the default address for
	 * SMS messages to be sent is <code>sms://host:port</code> .
	 */
	
	if (name.charAt(0) != '/' || name.charAt(1) != '/') {
	    throw new IllegalArgumentException(
			   "Missing protocol separator.");
	}
	
	int colon = name.indexOf(':');
	if (colon > 0) {
	    if (colon != 2) {
		host = name.substring(2, colon);
	    }
	    port = name.substring(colon + 1);
	} else {
	    if (name.length() > 2) {
		host = name.substring(2); 
	    }
	}

	/* Open the inbound server datagram connection. */
	dgc =  new DatagramSocket(portIn);
	
	open = true;
        return this;
    }

    /**
     * Constructs a new message object of a text or binary type. When the 
     * <code>TEXT_MESSAGE</code> constant is passed in, the created 
     * object implements the <code>TextMessage</code> interface.
     * When the <code>BINARY_MESSAGE</code> constant is passed in, the 
     * created object implements the <code>BinaryMessage</code> 
     * interface. 
     * <p>
     * If this method is called in a sending mode, a new 
     * <tt>Message</tt> object is requested from the connection. For example:
     * <p>
     * <tt>Message msg = conn.newMessage(TEXT_MESSAGE);</tt>
     * <p>
     * The newly created <tt>Message</tt> does not have the destination
     * address set. It must be set by the application before 
     * the message is sent.
     * <p>
     * If it is called in receiving mode, the <tt>Message</tt> object does have 
     * its address set. The application can act on the object to extract 
     * the address and message data. 
     * <p>
     * <!-- The <tt>type</tt> parameter indicates the number of bytes 
     * that should be
     * allocated for the message. No restrictions are placed on the application 
     * for the value of <tt>size</tt>.
     * A value of <tt>null</tt> is permitted and creates a 
     * <tt>Message</tt> object 
     * with a 0-length message. -->
     * 
     * @param  type either TEXT_MESSAGE or BINARY_MESSAGE.
     * @return      a new message
     */
    public Message newMessage(String type) {
        return newMessage(type, null);
    }

    /**
     * Constructs a new message object of a text or binary type and specifies
     * a destination address.
     * When the 
     * <code>TEXT_MESSAGE</code> constant is passed in, the created 
     * object implements the <code>TextMessage</code> interface.
     * When the <code>BINARY_MESSAGE</code> constant is passed in, the 
     * created object implements the <code>BinaryMessage</code> 
     * interface. 
     * <p>
     * The destination address <tt>addr</tt> has the following format:
     * <tt>sms://</tt><em>phone_number</em>:<em>port</em>.
     *
     * @param  type either TEXT_MESSAGE or BINARY_MESSAGE.
     * @param  addr the destination address of the message.
     * @return      a new <tt>Message</tt> object.
     */
    public Message newMessage(String type, String addr)  {
	/* Return the appropriate type of sub message. */
	if (type == MessageConnection.TEXT_MESSAGE) {
	    return  new TextObject(addr); 
	} else if (type == MessageConnection.BINARY_MESSAGE) {
	    return  new BinaryObject(addr);
	}
        return null; // message type not supported
    }

    /**
     * Sends a message over the connection. This method extracts
     * the data payload from 
     * the <tt>Message</tt> object so that it
     * can be sent as a datagram. 
     *
     * @param     dmsg a <tt>Message</tt> object.
     * @exception ConnectionNotFoundException  if the address is 
     *    invalid or if no address is found in the message.
     * @exception IOException  if an I/O error occurs.
     */
    public void send(Message dmsg) throws IOException {

	/** Formatted datagram record. */
	DatagramRecord dr = new DatagramRecord();

	/** Saved timestamp for use with multiple segment records. */
	long sendtime = System.currentTimeMillis();

	/** Offset in the sending buffer for the current fragment. */
	int offset = 0;

	byte[] buffer = null;
	String type = null;
	if (dmsg instanceof TextMessage) {
	    type = "text";
	    buffer = ((TextObject)dmsg).getBytes();
	} else if (dmsg instanceof BinaryMessage) {
	    buffer = ((BinaryMessage)dmsg).getPayloadData();
	    type = "binary";
	}
	/* Total length of the multisegment transmission. */
	int length;

	/** Number of segments that need to be sent. */
	int segments;

	/** Extra header size for concatenated messages. */
	int headersize;

	/** 
	 * Extra header space used for port number for
	 * destination address. All TCK messages include port number.
	 */
	int portheadersize = 7;

	/*
	 * For text messages choose between UCS-2 or GSM 7-bit
	 * encoding.
	 */
	if (type.equals("text")) {
	    byte[] gsm7bytes = TextEncoder.encode(buffer);
	    if (gsm7bytes != null) {
		// 160/152/145
		dr.setHeader("Text-Encoding", "gsm7bit");
		fragmentsize = 160;
		headersize = 8;
		buffer = gsm7bytes;
	    } else {
		// 140/132/126
		dr.setHeader("Text-Encoding", "ucs2");
		fragmentsize = 140;
		headersize = 8;
		if (portheadersize != 0) {
		    portheadersize = 6;
		}
	    }
	} else {
	    // 140/133/126
	    fragmentsize = 140;
	    headersize = 7;
	    if (portheadersize != 0) {
		portheadersize = 6;
	    }
	}


	fragmentsize = fragmentsize - portheadersize;
	if (buffer.length < fragmentsize) {
	    segments = 1;
	} else {
	    fragmentsize = fragmentsize - headersize;
	    segments = (buffer.length + fragmentsize - 1) / fragmentsize;
	}

	length = buffer.length;
	/* Fragment the data buffer into multiple segments. */
	for (int i = 0; i < segments; i++) {
	    /* Datagram envelope for sending messages. */
	    mess = new DatagramPacket(buf, 1500);
	    mess.setAddress(InetAddress.getByName(clientHost));
	    mess.setPort(portOut);

	    dr.setHeader("Date", String.valueOf(sendtime));
	    String address = dmsg.getAddress();
	    if (address.startsWith("cbs://")) {
		dr.setHeader("CBSAddress", dmsg.getAddress());
		dr.setHeader("Address", "sms://" + clientHost + ":" + CBSPort);
	    } else {
		dr.setHeader("Address", dmsg.getAddress());
	    }
	    if (host == null) {
		dr.setHeader("SenderAddress", "sms://" + phoneNumber);
	    } else{
		dr.setHeader("SenderAddress", "sms://" + phoneNumber + ":" + port);
	    } 
	    dr.setHeader("Content-Type", type);
	    dr.setHeader("Content-Length", String.valueOf(buffer.length));
	    dr.setHeader("Segments", String.valueOf(segments));
	    
	    if (segments > 1) {
		offset = i* fragmentsize;
		length =  (i < (segments -1) ? fragmentsize :
			   buffer.length - (fragmentsize * i));
		dr.setHeader("Fragment", String.valueOf(i));
		dr.setHeader("Fragment-Size", String.valueOf(length));
		dr.setHeader("Fragment-Offset", String.valueOf(offset));
	    }

	    byte[] buf = new byte[length];
	    System.arraycopy(buffer, offset, buf, 0, length);
	    dr.setData(buf);
	    byte [] messdata = dr.getFormattedData();
	    // DEBUG: System.out.println (dr.toString());

	    mess.setData(messdata, 0, messdata.length);

	    mess.setAddress(InetAddress.getByName(clientHost));
	    
	    dgc.send(mess);
	}
    }
    
    /**
     * Receives the bytes that have been sent over the connection, 
     * constructs a <tt>Message</tt> object, and returns it. 
     * <p>
     * If there are no <code>Message</code>s waiting on the connection, 
     * this method will block until a message 
     * is received, or the <code>MessageConnection</code> is closed.
     *
     * @return a <code>Message</code> object
     * @exception IOException  if an I/O error occurs.
     */
    public synchronized Message receive()
        throws IOException {
	DatagramRecord previous  = null;
	DatagramRecord current = null;
	while (true) {
	    dgc.receive(mess);
	    current = new DatagramRecord();
	    
	    if (current.parseData(mess.getData(), mess.getLength())) {
		/* Mulitple segments need to be aggregated together. */
		if (current.addData(previous)) {
		    /* 
		     * Break out of the loop when a multi-part 
		     * transmission is complete.
		     */
		    break;
		}
	    } else {
		break;
	    }
	    /* Save a pointer to this packet in case there are more. */
	    previous = current;
	}

	String addr = current.getHeader("Address");
	long time = 0;
	try {
	    time = Long.parseLong(current.getHeader("Date"));
	} catch (NumberFormatException nfe) {
	    // TEMP - ignore NFE
	}
	String type = current.getHeader("Content-Type");

	System.out
	    .println("Received message:\n" + current.toString());

	boolean portMatch = false;
	Message msg = null;
	while (!portMatch) {
	    int colon = addr.lastIndexOf(':');
	    if (colon > 0) {
		portMatch = addr.substring(colon + 1).equals(port);
	    }
	    
	    if (type.equals("text")) {
		TextMessage textmsg = (TextMessage) 
		    newMessage(MessageConnection.TEXT_MESSAGE,
			       addr);
		byte[] buf = current.getData();

		/* Always store text messages as UCS 2 bytes. */
		String te = current.getHeader("Text-Encoding");
		if (te != null && te.equals("gsm7bit")) {
		    buf = TextEncoder.decode(buf);
		}
		((TextObject)textmsg).setBytes(buf);
		msg = textmsg;
	    } else {
		BinaryMessage binmsg = (BinaryMessage) 
		    newMessage(MessageConnection.BINARY_MESSAGE,
			       addr);
		byte[] buf = current.getData();
		binmsg.setPayloadData(buf);
		msg = binmsg;
	    }
	    ((MessageObject) msg).setTimeStamp(time);
	}

	return msg; 
    }


    /**
     * Closes the connection. Reset the connection is open flag
     * so methods can be checked to throws an appropriate exception
     * for operations on a closed connection.
     *
     * @exception IOException  if an I/O error occurs.
     */
    public void close() throws IOException {

        if (open) {
	    dgc.close();
	    dgc = null;
            open = false;
        }
    }
	
}
