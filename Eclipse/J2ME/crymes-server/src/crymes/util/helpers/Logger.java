/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */


package crymes.util.helpers;

import crymes.server.gui.MainFrame;

/**
 * simple logger with ringbuffer
 * 
 * @author malte
 *
 */
public class Logger {

	private static Logger instance=null;
	private static final int RINGBUFFER_SIZE=256;
	private static String[]	buffer;
	private static int next=0;
	private static boolean turnaround=false;
	private static MainFrame _mf;
	private Logger() {
		buffer=new String[RINGBUFFER_SIZE];
		reset();
	}
	
	public static Logger getInstance(MainFrame mf) {
		_mf = mf;
		if (Logger.instance==null) {
			Logger.instance=new Logger();
	}
		return Logger.instance;
	}
	
	public static Logger getInstance() {
	if (Logger.instance==null) {
			Logger.instance=new Logger();
	}
		return Logger.instance;
	}
	
	public void write(String msg) {
		//System.out.println("Log: "+msg);
		if (_mf!=null)
		_mf.addMessageLog(msg);
		else
			System.out.println("Log: "+msg);
		Logger.buffer[next]=msg;
		Logger.next++;
		if (Logger.next>=Logger.RINGBUFFER_SIZE) {
			Logger.next=0;
			Logger.turnaround=true;
		}
	}
	
	public String[] readout() {
		int used=((Logger.turnaround) ? Logger.RINGBUFFER_SIZE : Logger.next);
		String[]	result=new String[used];
		int			run=0;
		if ((Logger.next>0) || (turnaround)) {
			if (turnaround) {
				for (int i=next ; i<Logger.RINGBUFFER_SIZE; i++){
					result[run]=buffer[i];
					run++;
				}
			}
			for (int i=0; i<Logger.next; i++) {
				result[run]=buffer[i];
				run++;
			}
		}
		
		return result;
	}
	
	public void reset() {
		next=0;
		turnaround=false;
	}
}
