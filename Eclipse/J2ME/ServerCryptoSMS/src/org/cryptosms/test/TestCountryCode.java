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

package org.cryptosms.test;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.cryptosms.helpers.CountryCodeStore;
import org.cryptosms.helpers.Helpers;

public class TestCountryCode extends MIDlet implements CommandListener {
	protected Display display;

	protected Form form;

	protected static final Command CMD_EXIT = new Command("Exit", Command.EXIT, 1);

	public void commandAction(Command c, Displayable d) {
		if (c == CMD_EXIT) {
			destroyApp(false);
			notifyDestroyed();
		}
	}

	protected void startApp() throws MIDletStateChangeException {
		display = Display.getDisplay(this);
		form = new Form("TestCountryCode");
		
		String[] urls={"5550000:6000","sms://5550000","sms://+5550000","sms://+017950000","sms://+4917950000","sms://4917950000"};
		for (int i=0;i<urls.length;i++){
					form.append(urls[i]+ "  H->  "+ Helpers.getNumberFromUrl(urls[i]));
			
		}
		NumberPair[] nps={new NumberPair("5550000","+5550000")
		,new NumberPair("017955555","+4917955555")
		,new NumberPair("017955555","+4917956555")
		,new NumberPair("5550000","+5550000")
		,new NumberPair("5550000","+5550000")};
		
		for (int i=0;i<nps.length;i++){
			form.append(nps[i].toString()+ "  CC->  "+new Boolean(CountryCodeStore.compareNumbers(nps[i].getN1(), nps[i].getN2())).toString());
			}

		
		
		form.addCommand(CMD_EXIT);
		form.setCommandListener(this);
		if (display != null)
			display.setCurrent(form);
	}

	protected void destroyApp(boolean arg0) {
	}

	protected void pauseApp() {
	}

	class NumberPair{
		private String n1;
		private String n2;
		NumberPair(String n1,String n2){
			this.n1=n1;
			this.n2=n2;
		}
		public String getN1() {
			return n1;
		}
		public String getN2() {
			return n2;
		}
		public String toString(){
			
			return n1 +" , " +n2;
		}
	}
}
