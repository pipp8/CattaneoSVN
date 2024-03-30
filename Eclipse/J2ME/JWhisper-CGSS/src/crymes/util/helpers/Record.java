/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package crymes.util.helpers;

/**
 * 	@author sd
 *	@date 27.04.2007
 */
public class Record{
	private int id;
	private byte[]record;
	public Record(int id,byte[] record){
		this.id=id;
		this.record=record;
	}
	public byte[] getData() {
		return record;
	}
	public void setRecord(byte[] record) {
		this.record = record;
	}
	public int getId() {
		return id;
	}
	
}

