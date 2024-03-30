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

/**
 * 	@author sd
 *	@date 04.05.2007
 */
public class CountryCode {
private String country;
private String code;
private String replacement;

public CountryCode(String country,String code,String replacement ) {
	this.code=code;
	this.country=country;
	this.replacement=replacement;
}
public String getCode() {
	return code;
}

public String getCountry() {
	return country;
}
public String getReplacement() {
	return replacement;
}

}
