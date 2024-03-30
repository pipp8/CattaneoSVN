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

import java.util.Hashtable;

import org.cryptosms.helpers.Helpers;

public class TestHelpers {

	private static String NL = "\r\n";

	private static String A_KEY = "A_KEY";

	private static String A_VALUE = "a value";

	public static void main(String[] args) {

		String testConfig = "# comment" + NL + A_KEY + " \t \"" + A_VALUE + "\"" + NL;

		Hashtable tokens = Helpers.splitTokens(testConfig);
		System.out.println("Test: " + A_KEY + " " + tokens.containsKey(A_KEY));
		System.out.println("Test: " + A_VALUE + "=" + tokens.get(A_KEY) + " " + A_VALUE.equals(tokens.get(A_KEY)));

	}

}
