/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.modules.message;


public class MessageType {
	public static final byte ENCRYPTED_TEXT_INBOX = 1;

	public static final byte ENCRYPTED_KEY = 2;

	public static final byte KEY = 3;
	
	public static final byte SYM_ENCRYPTED_TEXT_OUTBOX = 4;
	
	public static final byte SYM_ENCRYPTED_TEXT_INBOX = 5;
	
	public static final byte PK_REGISTER_REQUEST = 6;
	
	public static final byte PK_SERVER_CHALLENGE  = 7;
	
    public static final byte PK_CHALLENGE_RESPONSE = 8;
	
	public static final byte MAX_MESSAGE_TYPE  = 9;
}
