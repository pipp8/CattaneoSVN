/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */



package jwhisper.utils;

public class Logger {

	private static Logger instance=null;
	protected static final int RINGBUFFER_SIZE=256;
	protected static String[]	buffer;
	protected static int next=0;
	protected static boolean turnaround=false;
	
	protected Logger() {
		buffer=new String[RINGBUFFER_SIZE];
		reset();
	}
	
	public static Logger getInstance() {
		if (Logger.instance==null) 
			Logger.instance=new Logger();
		return Logger.instance;
	}
	
	
	
	public void write(String msg) {
		System.out.println("Log: " + msg + "\n");
		Logger.buffer[next] = msg;
		Logger.next ++;
		if (Logger.next >= Logger.RINGBUFFER_SIZE) {
			Logger.next = 0;
			Logger.turnaround = true;
		}
	}
	
	public String[] readout() {
		int used = ((Logger.turnaround) ? Logger.RINGBUFFER_SIZE : Logger.next);
		String[]	result = new String[used];
		int			run=0;
		
		if ((Logger.next > 0) || (turnaround)) {
			if (turnaround) {
				for (int i=next ; i < Logger.RINGBUFFER_SIZE; i++){
					result[run] = buffer[i];
					run++;
				}
			}
			for (int i=0; i<Logger.next; i++) {
				result[run] = buffer[i];
				run++;
			}
		}
		
		return result;
	}
	
	public void reset() {
		next = 0;
		turnaround = false;
	}
}
