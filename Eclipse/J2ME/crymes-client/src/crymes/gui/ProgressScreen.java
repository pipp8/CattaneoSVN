/*
 * Copyright (C) 2007 Crypto Messaging with Elliptic Curves Cryptography (CRYMES)
 *
 * This file is part of Crypto Messaging with Elliptic Curves Cryptography (CRYMES).
 *
 * Crypto Messaging with Elliptic Curves Cryptography (CRYMES) is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * any later version.
 *
 * Crypto Messaging with Elliptic Curves Cryptography (CRYMES) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Crypto Messaging with Elliptic Curves Cryptography (CRYMES); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package crymes.gui;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.TextField;

/**
 * @author sd
 */
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
