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

/**
 * simple logger with ringbuffer
 * 
 * @author malte
 *
 */
package jwhisper.helpers;

public class Logger {

	private static Logger instance=null;
	private static final int RINGBUFFER_SIZE=256;
	private static String[]	buffer;
	private static int next=0;
	private static boolean turnaround=false;
	
	private Logger() {
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
