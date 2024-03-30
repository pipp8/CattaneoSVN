/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package jwhisper.cgss;

public class CSettings {

	private static final int defaultSMSPort = 17002;
	private static final int maxTentativePassphrase = 3;
	
//	private static String numberSMSC_WIND = "+393804351160";
//	private static String numberSMSC_TIM = "+393338966733";
//	private static String numberSMSC_VODAFONE = "+393290904025";	
//	private static String numberSMSC_H3G = "+393928191659";
	
	private static String kvemHome;
	private static String numberSMSC_GSM;
	
	private static String numberSMSC_WTK;
	private static String gsmModemComPort;
	private static boolean timerPassphraseEnabled;
	private static int timerPassphraseDelay;
	
	public static String getKvemHome() {
		return kvemHome;
	}
	public static void setKvemHome(String kvemHome) {
		CSettings.kvemHome = kvemHome;
	}
	public static String getNumberSMSC_GSM() {
		return numberSMSC_GSM;
	}
	public static void setNumberSMSC_GSM(String numberSMSC_GSM) {
		CSettings.numberSMSC_GSM = numberSMSC_GSM;
	}
	public static String getNumberSMSC_WTK() {
		return numberSMSC_WTK;
	}
	public static void setNumberSMSC_WTK(String numberSMSC_WTK) {
		CSettings.numberSMSC_WTK = numberSMSC_WTK;
	}
	public static String getGsmModemComPort() {
		return gsmModemComPort;
	}
	public static void setGsmModemComPort(String gsmModemComPort) {
		CSettings.gsmModemComPort = gsmModemComPort;
	}
	public static boolean isTimerPassphraseEnabled() {
		return timerPassphraseEnabled;
	}
	public static void setTimerPassphraseEnabled(String timerPassphraseEnabled) {
		boolean b;
		if (timerPassphraseEnabled.compareTo("true") == 0)
			b = true;
		else
			b=false;
		CSettings.timerPassphraseEnabled = b;
	}
	public static int getTimerPassphraseDelay() {
		return timerPassphraseDelay;
	}
	public static void setTimerPassphraseDelay(String timerPassphraseDelay) {
		int i = 0;
		try {
			i = Integer.parseInt(timerPassphraseDelay);
		} catch (NumberFormatException e){
			setTimerPassphraseEnabled("false");
			return;
		}		
		CSettings.timerPassphraseDelay = i;
	}
	public static int getDefaultSMSPort() {
		return defaultSMSPort;
	}
	public static int getMaxTentativePassphrase() {
		return maxTentativePassphrase;
	}
//	public static String getNumberSMSC_WIND() {
//		return numberSMSC_WIND;
//	}
//	public static String getNumberSMSC_TIM() {
//		return numberSMSC_TIM;
//	}
//	public static String getNumberSMSC_VODAFONE() {
//		return numberSMSC_VODAFONE;
//	}
//	public static String getNumberSMSC_H3G() {
//		return numberSMSC_H3G;
//	}
	
	
	
		

}
