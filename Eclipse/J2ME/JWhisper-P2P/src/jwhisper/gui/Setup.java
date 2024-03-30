/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 * JWhisper P2P
 * 
 ************* *********** ******* ***** *** ** */


package jwhisper.gui;


import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;



import jwhisper.crypto.IKeyPairGenerator;
import jwhisper.crypto.KeyPair;
import jwhisper.crypto.Utils;


import jwhisper.modules.Settings;
import jwhisper.modules.addressBook.AddressService;
import jwhisper.utils.Logger;
import jwhisper.utils.ResourceManager;
import jwhisper.utils.ResourceTokens;





public class Setup extends Form implements CommandListener {
	private final Command ok;
	private IKeyPairGenerator _keyPairGenerator;
	private String _algorithm = "ECIES";
	
	TextField name;

	TextField number;

	TextField pass;
	TextField repeatPass;
	
	JWhisperMidlet midlet;

	public Setup(JWhisperMidlet midlet) {
		super(midlet.getResourceManager().getString(ResourceTokens.STRING_INITIALIZATION));
		ResourceManager rm = midlet.getResourceManager();
		_keyPairGenerator = Utils.GetKeyPairGenerator(_algorithm);
		this.midlet = midlet;
		name = new TextField(rm.getString(ResourceTokens.STRING_YOUR_NAME), "", 20, TextField.ANY);
		number = new TextField(rm.getString(ResourceTokens.STRING_YOUR_NUMBER), "", 20, TextField.PHONENUMBER);
		pass = new TextField(rm.getString(ResourceTokens.STRING_ENTER_PASSPHRASE), "", 20, TextField.PASSWORD);
		repeatPass = new TextField(rm.getString(ResourceTokens.STRING_FORM_REPEAT_PASSPHRASE), "", 20, TextField.PASSWORD);
		this.append(name);
		this.append(number);
		this.append(pass);
		this.append(repeatPass);
		this.ok = new Command(midlet.getResourceManager().getString(ResourceTokens.STRING_OK), Command.OK, 0);
		this.addCommand(this.ok);
		this.setCommandListener(this);
		this.setTicker(midlet.getTicker());
		
	}

	public void commandAction(Command co, Displayable disp) {
		if (co == this.ok) {
			
			// Controlla se tutti i valori inseriti sono validi
			
			if(this.name.getString().length()<2 || this.number.getString().length()<7|| this.pass.getString().length()<5||!pass.getString().equals(repeatPass.getString())){
				StringBuffer e=new StringBuffer();
				if(this.name.getString().length()<2){
					e.append(midlet.getResourceManager().getString(ResourceTokens.STRING_FORM_NAME_MIN_2_CHARS));
					e.append("\n");
				}
				if(this.number.getString().length()<7 ){
					e.append(midlet.getResourceManager().getString(ResourceTokens.STRING_FORM_NUMBER_MIN_7_CHARS));
					e.append("\n");
				}
				if(this.pass.getString().length()<5){
					e.append(midlet.getResourceManager().getString(ResourceTokens.STRING_FORM_PASS_MIN_5_CHARS));
					e.append("\n");
				}
				if(!pass.getString().equals(repeatPass.getString())){
					e.append(midlet.getResourceManager().getString(ResourceTokens.STRING_FORM_PASS_NOT_MATCH_REPEAT));
					e.append("\n");
				}
				midlet.showInfo(e.toString());
			} 
			else {	
				try {				
				midlet.initAddressService(pass.getString());
				Display d = Display.getDisplay(midlet);
				d.setCurrent( new ProgressScreen(midlet.getMenu() , midlet.getResourceManager().getString(ResourceTokens.STRING_GENERATING_KEYPAIR),d){
				
				  public void run(){
					  this.setStatus("progressbar opens");
					  try 
					  {
						  //TODO aggiornare settings e tutti i metodi che lo usano
						  KeyPair k = _keyPairGenerator.generateKeyPair();
						  
						  Settings s=AddressService.saveSettings(_algorithm, name.getString(), number.getString(), k);
						  //Settings s=AddressService.saveSettings(name.getString(), number.getString(), CryptoHelper.generateKeyPairHelper(this));
						  midlet.setSettings(s);
						  this.setStatus("progressbar closes");				    
						  midlet.finishInit();
					  } 
					  
					  catch (Exception e) 
					  {
						  Logger.getInstance().write((midlet.getResourceManager().getString(ResourceTokens.STRING_ERROR_THREAD  + "\n"+ e.getMessage())));					
					  }
					  
					  finally
					  {
						  stop();
					  }
				  }
				} );
				//AddressService.initSettings(name.getString(), number.getString(), CryptoHelper.generateKeyPairHelper());
				//midlet.showMenu();
			} catch (Exception e) {
				midlet.showError(e.getMessage());
			}
			}
		}
	}
}