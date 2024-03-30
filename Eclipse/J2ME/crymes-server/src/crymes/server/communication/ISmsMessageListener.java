/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package crymes.server.communication;

import java.util.EventListener;


// Interfaccia listener per le classi che devono/vogliono gestire
// gli sms in ingresso
public interface ISmsMessageListener extends EventListener
{
     public void notifyIncomingMessage(crymes.util.wma.BinaryMessage m, boolean isEmulator);
}

