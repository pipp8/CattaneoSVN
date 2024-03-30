/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */


package jwhisper.cgss.utils;

import jwhisper.cgss.gui.MainFrame;
import jwhisper.utils.Logger;

public class CGSSLogger extends Logger{

		
	private static MainFrame _mf;
	private static CGSSLogger instance=null;
	
	private CGSSLogger(){
		super();
	}
	
	public static CGSSLogger getInstance(MainFrame mf) {
		
		if (CGSSLogger.instance==null) {
			CGSSLogger.instance=new CGSSLogger();
		}
		_mf = mf;
		
		return CGSSLogger.instance;
	}
	
	public static CGSSLogger getInstance() {
		if (CGSSLogger.instance==null) 
			CGSSLogger.instance=new CGSSLogger();
		return CGSSLogger.instance;
	}
	
	public void write(String msg) {
		System.out.println("Log: "+msg);
		if (_mf!=null)
			_mf.addMessageLog(msg);
		else
			System.out.println("Log: "+msg);
		CGSSLogger.buffer[next]=msg;
		CGSSLogger.next++;
		if (CGSSLogger.next>=CGSSLogger.RINGBUFFER_SIZE) {
			CGSSLogger.next=0;
			CGSSLogger.turnaround=true;
		}
	}
	
}
