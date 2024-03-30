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

import jwhisper.gui.JWhisperMidlet;


public class PassphraseTimer {
	private final Timer timer = new Timer();

	private Task task;

	private JWhisperMidlet observing;

	public void start() {
		this.task = new Task(this);
		this.task.valid = true;
		this.timer.schedule(this.task, 1200000);
	}

	public void notifyObserver() {
		if (this.observing != null)
			this.observing.notify(new Boolean(this.task.valid));
	}

	public void register(JWhisperMidlet midlet) {
		this.observing=midlet;
	}

	public class Task extends TimerTask {
		boolean valid = false;

		private PassphraseTimer runtime;

		Task(PassphraseTimer runtime) {
			this.runtime = runtime;
		}

		public void run() {
			this.valid = false;
			this.runtime.notifyObserver();
		}
	}
}
