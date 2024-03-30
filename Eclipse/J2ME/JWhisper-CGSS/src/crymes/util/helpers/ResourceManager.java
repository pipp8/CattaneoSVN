/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package crymes.util.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import jwhisper.utils.Helpers;

/**
 * ResourceManager delivers StringItems for a locale
 * 
 * @author crypto
 */
@SuppressWarnings("unchecked")
public class ResourceManager {

	private Hashtable tokenMap;

	/**
	 * Initializes a ResourceManger with locale, stored in file
	 * "res/global/[locale]/CryptoSMS.res
	 * 
	 * @param locale
	 */
	public ResourceManager(String locale) throws IOException {
		if (locale.length()>2) locale=locale.substring(0,2);
		tokenMap = new Hashtable();

		// open file
		String file = "/global/" + locale + "/CryptoSMS.res";
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
