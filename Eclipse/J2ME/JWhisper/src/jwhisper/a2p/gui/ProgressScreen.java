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
package jwhisper.a2p.gui;

import javax.microedition.lcdui.Font;

import gr.bluevibe.fire.components.FGauge;
import gr.bluevibe.fire.components.Panel;
import gr.bluevibe.fire.components.Row;
import gr.bluevibe.fire.displayables.FireScreen;

/**
 * @author sd
 */
abstract public class ProgressScreen extends Panel implements Runnable {
	private Panel _next;

	private Progress _progress = null;

	private FGauge _gauge = null;

	private Row _status = null;

	private FireScreen _display;
	
	private static final int PROGRESS_STEPS=3;
	private static final int PROGRESS_DELAY=500;

	public ProgressScreen(Panel next, String title, FireScreen fscreen) {
		this.setLabel(title);
		this._next = next;
		this._display = fscreen;
		_status = new Row("Avvio...");
		_gauge = new FGauge();

		this.add(_status);
		this.add(_gauge);
		
		this.add(new Row());
		this.add(new Row());
		Row waitRow = new Row("Attendere prego");
		waitRow.setFont(Font.getFont(Font.FACE_MONOSPACE,Font.STYLE_BOLD,Font.SIZE_SMALL)); // change the font
		waitRow.setAlignment(FireScreen.CENTRE); // allign the text to the centre.
		this.add(waitRow);
		
		fscreen.setBusyMode(true);
		
		_progress = new Progress(_gauge);
		_progress.start();
		Thread t1 = new Thread(this);
		t1.start();
	}

	public void setStatus(String s) {
		this._status.setText(s);
	}

	public void stop() {
		_progress.stop();
		_display.setBusyMode(false);
		_display.setCurrent(_next);
	}

	class Progress extends Thread {
		private int i;

		private boolean _running = true;

		private FGauge g = null;

		Progress(FGauge ga) {
			g = ga;
			i=0;
		}

		public void run() {
			while (_running) {
				g.clock();
				g.validate();
				_display.repaint();
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

	public Panel getNext() {
		return _next;
	}

	public Panel get_next() {
		return _next;
	}

	public FireScreen get_display() {
		return _display;
	}
}
