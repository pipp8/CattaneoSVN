/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */
 
package jwhisper.cgss.modules.recordstore;


public interface RecordComparator
{

  static final int EQUIVALENT = 0;
  static final int FOLLOWS = 1;
  static final int PRECEDES = -1;
  
  int compare(byte[] rec1, byte[] rec2);

}

