package jwhisper.gui;

import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;

import jwhisper.helpers.CryptedRecordStore;


/**
 * 	@author Fabio
 *	@date 02.05.2007
 */
public class JWhisperSplash extends Form implements CommandListener {

	
	private final Command ok;

	private final JWhisperMidlet midlet;
	
	private Image image;

	private ImageItem imageItem;
	
	public JWhisperSplash(JWhisperMidlet midlet, String title) {
		
		super(title);
		
		this.midlet=midlet;

		try {
			image = Image.createImage("/Logo_png3.png");
			
		    imageItem = new ImageItem(null, image, ImageItem.LAYOUT_CENTER | ImageItem.LAYOUT_NEWLINE_AFTER, "My Image");
		      
			this.append(imageItem);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.append("Dip. di Informatica ed Applicazioni\n\"R.M. Capocelli\"");
		
		this.ok = new Command("Ok", Command.OK, 1);
		
		this.addCommand(this.ok);
		this.setCommandListener(this);
	}

	public void commandAction(Command co, Displayable arg1) {
		if (co == this.ok) {
			try 
			{
				//If the crypted record store exists, we already have done the setup
				if (CryptedRecordStore.hasPass()) {
					Display.getDisplay(this.midlet).setCurrent(new Login(this.midlet));
				} else {
					//From here we need to do the setup and the handshake.
					Display.getDisplay(this.midlet).setCurrent(new Setup(this.midlet));
				}
			} catch (Exception e) {
				this.midlet.showError(e.getMessage());
			}
		}
	}
}
