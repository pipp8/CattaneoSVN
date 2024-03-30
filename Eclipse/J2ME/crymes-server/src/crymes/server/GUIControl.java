/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package crymes.server;

import crymes.server.gui.MainFrame;
import crymes.server.handlers.GSMClient;
import crymes.server.handlers.WtkClient;


// Questo thread permette la comunicazione tra la gui e l'engine 
// nonchè l'aggiornamento delle info "realtime" 
public class GUIControl implements Runnable {
	
	private MainFrame _mainFrame;
	private Engine _engine;
	
	
	public GUIControl(MainFrame mainFrame) {
		_mainFrame = mainFrame;
	}
	
	public void setEngine(Engine e){
		_engine = e;
	}
	
	public void run() {

		while (true) {
	
			// Verifica se esiste la connessione all'emulatore
			// se non esiste tenta di crearla
			WtkClient w = _engine.getWtkClient();
			if (w==null) {
				if (_engine.createWTKConnection())
					setStatusEmulator(true, CSettings.getNumberSMSC_WTK());
				else
					setStatusEmulator(false, null);
			}

			// Verifica se esiste la connessione al modem gsm
			// se non esiste tenta di crearla
			GSMClient g = _engine.getGSMClient();
			if (g==null) {
				if (_engine.createGSMModemConnection()){
					g = _engine.getGSMClient();
					setStatusGSM(true, g.getPhoneNumber());
					setSignalGSMModem(g.getGSMModemSignalLevel());
				}
				else{
					setStatusGSM(false, null);
				}
			} else {
				// Aggiorna lo stato della signal
				int signal =g.getGSMModemSignalLevel();
				setSignalGSMModem(signal);
			}
			
			try {
				Thread.sleep(153000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	  public void setStatusEmulator(boolean connected, String number){
	    	_mainFrame.setStatusEmulator(connected, number);
	    }
	    
	  public void setStatusGSM(boolean connected, String number){
	    	_mainFrame.setStatusGSMModem(connected, number);
	    }
	    
	    
	  public void setSignalGSMModem(int s){
	    	_mainFrame.setSignalGSMModem(s);	    	
	  }
	    
	  public void adadMessageLog(String s){
	   	//_smsFrame.addMessageLog(s);
	   	_mainFrame.addMessageLog(s);
	  }

	   
}
