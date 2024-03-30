package jwhisper.a2p.gui;

import gr.bluevibe.fire.components.Panel;
import gr.bluevibe.fire.displayables.FireScreen;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import jwhisper.a2p.ConstantA2P;
import jwhisper.utils.Logger;


public class ModalAlert extends Alert implements CommandListener
{
	private Panel dspBACK;
	private FireScreen d;
	
	private menuRegistrazione mr;
	
	//Comando di uscita
	protected Command CMD_ESCI = new Command("Esci", Command.EXIT, 0);
	
	//Comandi di Re-invio richiesta ed primo invio
	//Sta roba serve perche' l'alert non e' modale e non ferma l'esecuzione del programma
	protected Command CMD_REINVIO = new Command("Continua", Command.OK, 1);
	protected Command CMD_INVIO = new Command("Continua", Command.OK, 1);
	
	public ModalAlert(String title, String text, AlertType type, FireScreen d, Panel next, menuRegistrazione mr, byte tp)
	{
		super(title, text, null, type);

		if (tp == ConstantA2P.COMMAND_TYPE_REINVIO) {
			this.addCommand(CMD_ESCI);
			this.addCommand(CMD_REINVIO);
		}
		else if (tp == ConstantA2P.COMMAND_TYPE_INVIO) {
			this.addCommand(CMD_ESCI);
			this.addCommand(CMD_INVIO);
		}
		else if (tp == ConstantA2P.COMMAND_TYPE_INFO) {
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
	}

	public void commandAction(Command cmd, Displayable dsp)
	{
		if(cmd == Alert.DISMISS_COMMAND || cmd == CMD_ESCI) {		
			Logger.getInstance().write("ESCI");
		}
		
		else if (cmd == CMD_REINVIO) {
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
	}
}
