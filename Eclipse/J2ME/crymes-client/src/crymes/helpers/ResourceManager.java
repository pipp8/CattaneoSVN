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
package crymes.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

/**
 * ResourceManager delivers StringItems for a locale
 * 
 * @author crypto
 */

public class ResourceManager {

	private Hashtable tokenMap;

	/**
	 * Initializes a ResourceManger with locale, stored in file
	 * "res/global/[locale]/Crymes.res
	 * 
	 * @param locale
	 */
	public ResourceManager(String locale) throws IOException {
		if (locale.length()>2) locale=locale.substring(0,2);
		tokenMap = new Hashtable();

		// open file
		String file = "/global/" + locale + "/Crymes.res";
		InputStream is = getClass().getResourceAsStream(file);
		if (is!=null) {
			byte[] data = new byte[is.available()];
			is.read(data);
			String configString = new String(data, "utf-8");
			tokenMap=Helpers.splitTokens(configString);
			is.close();
		}
		else throw new IOException("Could not open " +file );
	}

	/**
	 * @param resourceToken
	 *            to look up
	 * @return token or errorstring.
	 */
	public String getString(String resourceToken) {
		if (resourceToken == null)
			return "<token null>";
		String returnToken = "<" + resourceToken + ">";
		if (tokenMap.containsKey(resourceToken))
			returnToken = (String) tokenMap.get(resourceToken);
		return returnToken;
	}

	/**
	 * @param iconToken
	 *            to look up
	 * @return byte[] with icondata or null, if it can not be found
	 */
	public byte[] getIcon(String iconToken) {
		// TODO implement
		return null;
	}

}
