/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.utils;

import java.util.Timer;
import java.util.TimerTask;

import jwhisper.JWhisperMidlet;


/*
 * timer task to reset the ticker to ""
 *
 */
public class TickerTask extends TimerTask {

	private JWhisperMidlet	midlet=null;
	private Timer			timer=null;
	private	int				counter=0;
	private final int		MAX_COUNTER=20;
	
	public TickerTask(JWhisperMidlet midlet) {
		this.midlet=midlet;
		this.timer=new Timer();
		this.counter=0;
		
		this.timer.scheduleAtFixedRate(this, 1000, 1000);
	}
	
	public void run() {
		incrCounter();
		if (readCounter()>MAX_COUNTER) {
			midlet.setTicketText("");
		}
	}

	public void incrCounter() {
		accessCounter(2);
	}
	
	public int readCounter() {
		return accessCounter(1);
	}
	
	public void resetCounter() {
		accessCounter(0);
	}
	
	private synchronized int accessCounter(int job) {
		switch (job) {
		case 0:	// reset
			this.counter=0;
			break;
		case 1: // read out
			return this.counter;
		case 2: // increment
			this.counter++;
			break;
		}
		return 0;
	}
}
