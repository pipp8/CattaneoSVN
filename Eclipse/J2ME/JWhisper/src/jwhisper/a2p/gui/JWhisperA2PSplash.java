package jwhisper.a2p.gui;



import javax.microedition.lcdui.Command;
// import javax.microedition.lcdui.CommandListener; Uso il listener di Fire screen
import javax.microedition.lcdui.Font;

import jwhisper.modules.recordStore.CryptedRecordStore;

import gr.bluevibe.fire.components.Component;
import gr.bluevibe.fire.components.Panel;
import gr.bluevibe.fire.components.Row;
import gr.bluevibe.fire.displayables.FireScreen;
import gr.bluevibe.fire.util.CommandListener;
import gr.bluevibe.fire.util.FireIO;
import gr.bluevibe.fire.util.Lang;

/**
 * 	@author Fabio
 *	@date 02.05.2007
 */
public class JWhisperA2PSplash extends Panel implements CommandListener {

	
	private final Command ok;

	private final JWhisperA2PMidlet midlet;
	
	public JWhisperA2PSplash(JWhisperA2PMidlet midlet, String title) {
		
		//super(title);
		setLabel(title); // set label (displayed on the top of the screen)
	
		this.midlet=midlet;

		// ok we have a panel now, lets add some functionallity
		ok = new Command(Lang.get("Continua"),Command.EXIT,1);
		this.addCommand(ok); // add the command to the panel, 
		// it will be displayed on the bottom bar on the right, and assigned to the righ softkey.
		// The Panel checks the type of the command added. If it is Command.BACK 
		// it is assigned to the left softkey, otherwise it goes to the right. 
		
		// now set the CommandListener of the panel.
		this.setCommandListener(this);
		// this class implements a CommandListener.
		// for notes on the listener go to the commandAction method.
		
		this.add(new Row()); // you can comment this out to see the difference.
		
		// ok, up to now we have a very simple application. 
		// next step is to add a couple of rows to our panel.
		Row imageText = new Row("Application to Person", FireIO.getLocalImage("fire"));
		this.add(imageText);
		
		
		// in order to make the UI clearer we can add an empty row.
		this.add(new Row()); // you can comment this out to see the difference.
		
		// another type of seperator or header can be a line of color, with or without a text label
		Row seperator = new Row();
		seperator.setFilled(new Integer(FireScreen.defaultFilledRowColor)); // use the default color of the theme.
		seperator.setBorder(true); // borders look nice :)
		seperator.setText(Lang.get("Dipartimento di Informatica ed Applicazioni \"R.M. Capocelli\"")); // a string can be usefull on a header.
		seperator.setFont(Font.getFont(Font.FACE_MONOSPACE,Font.STYLE_BOLD,Font.SIZE_SMALL)); // change the font
		//seperator.setAlignment(FireScreen.CENTRE); // allign the text to the centre.
		seperator.setTextHpos(FireScreen.CENTRE);
		seperator.setTextVpos(FireScreen.BOTTOM);
		this.add(seperator);
		
//		try {
//			image = Image.createImage("/Logo_A2P.png");
//			
//		    imageItem = new ImageItem(null, image, ImageItem.LAYOUT_CENTER | ImageItem.LAYOUT_NEWLINE_AFTER, "My Image");
//		      
//			this.append(imageItem);
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		this.append("Dip. di Informatica ed Applicazioni\n\"R.M. Capocelli\"");
//		
//		this.ok = new Command("Ok", Command.OK, 1);
//		
//		this.addCommand(this.ok);
//		this.setCommandListener(this);
	}

	public void commandAction(Command cmd, Component c) {
		if (cmd == this.ok) {
			try 
			{
				//If the crypted record store exists, we already have done the setup
				if (CryptedRecordStore.hasPass()) {
//					Display.getDisplay(this.midlet).setCurrent(new Login(this.midlet));
					midlet.getFireScr().setCurrent(new Login(this.midlet)); // set the current panel on the FireScreen.
				} else {
					//From here we need to do the setup and the handshake.
					//Display.getDisplay(this.midlet).setCurrent(new Setup(this.midlet));
					midlet.getFireScr().setCurrent(new Setup(this.midlet));
				}
			} catch (Exception e) {
				this.midlet.showError(e.getMessage());
			}
		}
	}
}
