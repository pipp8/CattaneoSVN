/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.gui;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.TextField;


abstract public class ProgressScreen extends Form implements Runnable {
	private Displayable _next;

	private Progress _progress = null;

	private Gauge _gauge = null;

	private TextField _status = null;

	private Display _display;
	
	private static final int PROGRESS_STEPS=3;
	private static final int PROGRESS_DELAY=500;

	public ProgressScreen(Displayable next, String title, Display dis) {
		super(title);
		this._next = next;
		this._display = dis;
		_status = new TextField("", "starting", 30, TextField.UNEDITABLE);
		_gauge = new Gauge("", false, ProgressScreen.PROGRESS_STEPS, 0);
		append(_status);
		append(_gauge);
		_progress = new Progress(_gauge);
		_progress.start();
		Thread t1 = new Thread(this);
		t1.start();
	}

	public void setStatus(String s) {
		this._status.setString(s);
	}

	public void stop() {
		_progress.stop();
		_display.setCurrent(_next);
	}

	class Progress extends Thread {
		private int i;

		private boolean _running = true;

		private Gauge g = null;

		Progress(Gauge ga) {
			g = ga;
			i=0;
		}

		public void run() {
			while (_running) {
				g.setValue(i++);
				try {
					sleep(ProgressScreen.PROGRESS_DELAY);
				} catch (Exception e) {
				}
				if (i > ProgressScreen.PROGRESS_STEPS)
					i = 0;
			}
		}

		public void stop() {
			_running = false;
		}
	}

	public Displayable getNext() {
		return _next;
	}

	public Displayable get_next() {
		return _next;
	}

	public Display get_display() {
		return _display;
	}
}
