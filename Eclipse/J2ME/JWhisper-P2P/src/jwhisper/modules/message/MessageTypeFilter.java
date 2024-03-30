/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.modules.message;

import java.io.IOException;

import javax.microedition.rms.RecordFilter;

public class MessageTypeFilter implements RecordFilter {
	private byte _mType = 0;

	public MessageTypeFilter(byte type) {
		_mType = type;
	}

	public boolean matches(byte[] record) {
		try {
			//Dummy rmsID -99
			CMessage m = new CMessage(record,-99);

			return  m.getType()==_mType;
		} catch (IOException e) {
			return false;
		}
	}
}
