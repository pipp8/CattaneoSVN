package smsserver;

import smsserver.client.GSMClient;
import smsserver.client.WtkClient;


// Questo thread permette la comunicazione tra la gui e l'engine 
// nonchè l'aggiornamento delle info "realtime" 
public class GUIControl implements Runnable {

	private SmsFrame _smsFrame;
	private Engine _engine;
	
	
	public GUIControl(SmsFrame smsframe) {
		_smsFrame = smsframe;	
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
					setLabelEmulatorStatus("Connected. "+ _engine.getWtkClient().getPhoneNumber());
				else
					setLabelEmulatorStatus("Not connected.");
			}

			// Verifica se esiste la connessione al modem gsm
			// se non esiste tenta di crearla
			GSMClient g = _engine.getGSMClient();
			if (g==null) {
				if (_engine.createGSMModemConnection()){
					g = _engine.getGSMClient();
					setLabelGSMModemStatus("Connected. "+ g.getPhoneNumber());
					setGSMModemSignal(g.getGSMModemSignalLevel());
				}
				else{
					setLabelGSMModemStatus("Not connected.");
				}
			} else {
				// Aggiorna lo stato della signal
				setGSMModemSignal(g.getGSMModemSignalLevel());
			}
			
			
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	  public void setLabelEmulatorStatus(String str){
	    	_smsFrame.setLabelEmulatorStatus(str);
	    }
	    
	    public void setLabelGSMModemStatus(String str){
	    	
	    	_smsFrame.setLabelGSMModemStatus(str);
	    }
	    
	    public void setGSMModemSignal(int s){
	    	_smsFrame.setGSMModemSignal(s);	    	
	    }
	    
	    public void addMessageLog(String s){
	    	_smsFrame.addMessageLog(s);
	    }

	    public void addSMS(MyMessage m){	    	
	    	_smsFrame.addSMS(m);	    	
	    }
}
