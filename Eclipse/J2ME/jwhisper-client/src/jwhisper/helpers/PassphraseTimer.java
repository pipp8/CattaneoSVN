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
package jwhisper.helpers;

import java.util.Timer;
import java.util.TimerTask;

import jwhisper.gui.JWhisperMidlet;


/**
 * @version $Revision: 1.1 $
 */
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
