package crymes.gui;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

import crymes.helpers.Logger;


public class ModalAlert extends Alert implements CommandListener
{
//	private boolean isReady = false;
//	
//	private boolean result;

	private Displayable dspBACK;
	
	private Display d;
	
	private menuRegistrazione mr;
	
	//Comando di uscita
	protected Command CMD_ESCI = new Command("Esci", Command.EXIT, 0);
	
	//Comandi di Re-invio richiesta ed primo invio
	//Sta roba serve perche' l'alert non è modale e non ferma l'esecuzione del programma
	protected Command CMD_REINVIO = new Command("Continua", Command.OK, 1);
	protected Command CMD_INVIO = new Command("Continua", Command.OK, 1);
	
	public ModalAlert(String title, String text, AlertType type, Display d, Displayable next, menuRegistrazione mr, byte tp)
	{
		super(title, text, null, type);

		if (tp == TipoComando.REINVIO) {
			this.addCommand(CMD_ESCI);
			this.addCommand(CMD_REINVIO);
		}
		else if (tp == TipoComando.INVIO) {
			this.addCommand(CMD_ESCI);
			this.addCommand(CMD_INVIO);
		}
		else if (tp == TipoComando.INFO) {
			this.addCommand(CMD_ESCI);
		}
		
		this.setTimeout(Alert.FOREVER);
		this.setCommandListener(this);
		
		this.mr = mr;
		this.d = d;

		// Display Precedente
		dspBACK = next;

		// Mostra l'alert
		d.setCurrent(this);

		// Attendi la conferma di chiusura
		//waitForDone();

		// Visualizza il precedente Display
		//d.setCurrent(dspBACK);
	}

//	private void waitForDone()
//	{
//		try
//		{
//			while(!isReady)
//			{
//				synchronized(this)
//				{
//					this.wait();
//
//				}
//			}
//		}
//		catch(Exception error)
//		{
//
//		}
//	}

	public void commandAction(Command cmd, Displayable dsp)
	{
		if(cmd == Alert.DISMISS_COMMAND || cmd == CMD_ESCI) {		
//			result = false;
			Logger.getInstance().write("ESCI");
		}
		
		else if (cmd == CMD_REINVIO) {
//			result = true;
			Logger.getInstance().write("Re-Invio");
			mr.cancellaRichieste();
			mr.richiestaAttivazione();
		}
		
		else if (cmd == CMD_INVIO) {
			Logger.getInstance().write("primo invio");
			mr.richiestaAttivazione();
		}
		
		Logger.getInstance().write("TERMINE ALERT");
		d.setCurrent(dspBACK);
		
//		isReady = true;
//		
//		synchronized(this)
//		{
//			this.notify();
//		}
	}
	
//	public boolean getResult() {
//		return result;
//	}
}
