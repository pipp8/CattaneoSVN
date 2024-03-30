/*
 * Copyright (C) 2007 cryptosms.org
 *
 * This file is part of cryptosms.org.
 *
 * cryptosms.org is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * any later version.
 *
 * cryptosms.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with cryptosms.org; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.cryptosms.helpers;

import java.util.Timer;
import java.util.TimerTask;

//import org.cryptosms.gui.CryptoSMSMidlet;

/**
 * timer task to reset the ticker to ""
 * 
 * @author malte
 *
 */
@SuppressWarnings("unused")
public class TickerTask extends TimerTask {

	//private CryptoSMSMidlet	midlet=null;
	private Timer			timer=null;
	private	int				counter=0;
	private final int		MAX_COUNTER=20;
	
	//public TickerTask(CryptoSMSMidlet midlet) {
		public TickerTask() {
		//this.midlet=midlet;
		this.timer=new Timer();
		this.counter=0;
		
		this.timer.scheduleAtFixedRate(this, 1000, 1000);
	}
	
	public void run() {
		incrCounter();
//		if (readCounter()>MAX_COUNTER) {
//			midlet.setTicketText("");
//		}
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
