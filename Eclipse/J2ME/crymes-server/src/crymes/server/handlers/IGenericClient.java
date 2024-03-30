/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package crymes.server.handlers;

import java.io.IOException;


public interface IGenericClient  {
	
	String getPhoneNumber();
	boolean send(crymes.util.wma.BinaryMessage m)  throws IOException;
	//WMAMessage receive() throws IOException;;
}
