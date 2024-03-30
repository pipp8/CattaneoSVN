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

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

/**
 * misc helper stuff, mostly String operations
 * 
 * @author malte, rk
 * 
 */
public class Helpers {

	// at least some J2MEs have no proper toString() for java.util.Date
	public static String dateToStringFull(Date date) {
		try {
			// be careful with the following, not all mobiles outside have a
			// com.sun.cldc.util.j2me.CalendarImpl
			Class calClass = Class
					.forName("com.sun.cldc.util.j2me.CalendarImpl");
			Calendar cal = (Calendar) calClass.newInstance();

			if (date != null)
				cal.setTime(date);
			else
				cal.setTime(new Date()); // now

			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH) + 1;
			int day = cal.get(Calendar.DAY_OF_MONTH);

			int hour = cal.get(Calendar.HOUR);
			int minute = cal.get(Calendar.MINUTE);
			int second = cal.get(Calendar.SECOND);

			// the preverifier doesnt like ( a ? b : c) statements returning
			// different
			// types - therefore this stupid ""+int contruct making sure it's
			// always String
			return ((hour < 10) ? "0" + hour : "" + hour) + ":"
					+ ((minute < 10) ? "0" + minute : "" + minute) + ":"
					+ ((second < 10) ? "0" + second : "" + second) + " "
					+ ((day < 10) ? "0" + day : "" + day) + "."
					+ ((month < 10) ? "0" + month : "" + month) + "." + year;
		} catch (Exception e) {
			// ok - failure - just return the timestamp as a String
			return "" + date.getTime();
		}
	}

	// compare 2 byte arrays
	public static boolean compareByteArray(byte[] a, byte[] b) {
		if (a.length == b.length) {
			for (int i = 0; i < a.length; i++) {
				if (a[i] != b[i])
					return false; // different content
			}
			return true;
		} else {
			return false; // different size
		}
	}

	public static boolean isWhitespace(char ch) {
		return (ch == '\t' || ch == ' ');
	}

	private static final int KEY_PART = 0;

	private static final int WS_PART = 1;

	private static final int VALUE_PART = 2;

	public static Hashtable splitTokens(String configString) {
		// System.out.println("splitTokens: "+configString.length());
		Hashtable map = new Hashtable();
		int lastindex = 0, index = 0;
		while ((index = configString.indexOf("\n", index)) > 0) {
			String line = configString.substring(lastindex, index);
			line = line.trim();
			if (!line.startsWith("#")) {
				// parse file: format:token[\t*]"value"
				int ptr = KEY_PART;
				String key = "", value = "";
				for (int i = 0; i < line.length(); i++) {
					char ch = line.charAt(i);

					switch (ptr) {
					case KEY_PART:
						if (Helpers.isWhitespace(ch))
							ptr = WS_PART;
						else
							key += ch;
						break;
					case WS_PART:
						if (!Helpers.isWhitespace(ch)) {
							ptr = VALUE_PART;
							value += ch;
						}
						break;
					case VALUE_PART:
						value += ch;
						break;
					default:
						break;
					}
				}
				if (value.startsWith("\""))
					value = value.substring(1);
				if (value.endsWith("\""))
					value = value.substring(0, value.length() - 1);
				// System.out.println(key+" = "+value);
				map.put(key, value);
			}
			lastindex = index;
			index += 2;
		}
		return map;
	}

	/**
	 * get the seperator eg(+,0) and number-part from a SMS url eg: get +5550000
	 * from sms://+5550000:6000/
	 * 
	 * @param smsUrl
	 * @return
	 */
	public static String getNumberFromUrl(String smsUrl) {

		if (smsUrl == null)
			return null;

		int t = 0;
		int len = smsUrl.length();

		// get the first digit
		while ((t < len) && (!Character.isDigit(smsUrl.charAt(t))))
			t++;
		if (t == len)
			return smsUrl; // only digits in string

		int first = t;

		// forward to the next none digit character or EOS
		while ((t < len) && (Character.isDigit(smsUrl.charAt(t))))
			t++;
		String number = smsUrl.substring(first, t);

		// if necessary add the traffic seperator
		if (number.charAt(0) != '0') {
			number = "+" + number;
		}

		return number;
	}
}
