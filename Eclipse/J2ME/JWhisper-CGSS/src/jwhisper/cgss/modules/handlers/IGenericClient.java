/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.cgss.modules.handlers;

import java.io.IOException;


public interface IGenericClient  {
	
	String getPhoneNumber();
	boolean send(jwhisper.cgss.modules.wma.BinaryMessage m)  throws IOException;
	//WMAMessage receive() throws IOException;;
}
