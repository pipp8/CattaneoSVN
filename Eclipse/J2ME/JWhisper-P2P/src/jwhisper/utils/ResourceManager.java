/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;


public class ResourceManager {

	private Hashtable tokenMap;

	
	public ResourceManager(String locale) throws IOException {
		if (locale.length()>2) locale=locale.substring(0,2);
		tokenMap = new Hashtable();

		// open file
		String file = "/global/" + locale + "/jwhisper-p2p.res";
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

	
	public String getString(String resourceToken) {
		if (resourceToken == null)
			return "<token null>";
		String returnToken = "<" + resourceToken + ">";
		if (tokenMap.containsKey(resourceToken))
			returnToken = (String) tokenMap.get(resourceToken);
		return returnToken;
	}


}
