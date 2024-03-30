/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package crymes.book;

import java.io.IOException;

import jwhisper.cgss.modules.recordstore.RecordFilter;
import jwhisper.modules.message.CMessage;


/**
 * 
 * 
 * @author sd
 */
public class MessageTypeFilter implements RecordFilter {
	private byte _mType = 0;

	/**
	 * Creates a new MessageTypeFilter object.
	 */
	public MessageTypeFilter(byte type) {
		_mType = type;
	}

	public boolean matches(byte[] record) {
		//Dummy rmsID -99
		try {
			CMessage m = new CMessage(record,-99);

			return  m.getType()==_mType;
		} catch (IOException e) {
			return false;
		}
	}
}
