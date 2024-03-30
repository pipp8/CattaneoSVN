package jwhisper.a2p.gui;

import java.util.Calendar;
import java.util.Date;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Font;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;


import jwhisper.SettingsA2P;
import jwhisper.a2p.ConstantA2P;
import jwhisper.crypto.Cipher;
import jwhisper.crypto.IKeyPairGenerator;
import jwhisper.crypto.KeyPair;
import jwhisper.crypto.Utils;
import jwhisper.modules.addressBook.AddressService;


import jwhisper.modules.message.CMessage;
import jwhisper.modules.message.MessageType;
import jwhisper.modules.connection.MessageSender;


import jwhisper.utils.Logger;

import jwhisper.utils.ResourceTokens;
import jwhisper.utils.ResourceManager;

import gr.bluevibe.fire.components.Component;
import gr.bluevibe.fire.components.Panel;
import gr.bluevibe.fire.components.Row;
import gr.bluevibe.fire.displayables.FireScreen;
import gr.bluevibe.fire.util.CommandListener;

public class menuRegistrazione extends Panel implements CommandListener {

	private final Command exit, request, confirm;

	private final JWhisperA2PMidlet _midlet;

	private final String[] mainMenu = { "Richiedi l'attivazione", 
										"Conferma la registrazione"   };	
	private String SMSC;

	
	public menuRegistrazione(JWhisperA2PMidlet parent) {
		this.setLabel("Registrazione");
		
		ResourceManager rm = parent.getResourceManager();
		
		this._midlet = parent;

		this.exit = new Command(rm.getString(ResourceTokens.STRING_EXIT), Command.EXIT, 1);
		request = new Command("",Command.OK,1);
		confirm = new Command("",Command.OK,1);

		this.addCommand(this.exit);
		this.setCommandListener(this);
		this.setTicker(parent.getTicker());
		
//		SMSC = System.getProperty("wireless.messaging.sms.smsc");
//		SMSC = "+393804351160";
		
		//TODO mau 
		//
		
		if (isEmulator())
			SMSC = "+5554827";
		else {
			SMSC = ((SettingsA2P) _midlet.getSettings()).getSMSCNumber();
		}
		
		buildList();
	}
	
	private boolean isEmulator () {
		Logger.getInstance().write(System.getProperty("microedition.platform"));
		
		String platform = System.getProperty("microedition.platform");
		boolean isEmulated = (platform != null && (platform.endsWith("JAVASDK") || platform.equals("j2me") || platform.equals("SunMicrosystems_wtk")));
		
		return isEmulated;
	}

	private void buildList() {
		Row requestRow = new Row(mainMenu[0]);
		requestRow.setFont(Font.getFont(Font.FACE_MONOSPACE,Font.STYLE_BOLD,Font.SIZE_MEDIUM)); // change the font
		requestRow.setAlignment(FireScreen.CENTRE);
		requestRow.addCommand(request);
		requestRow.setCommandListener(this);
		
		Row confirmRow = new Row(mainMenu[1]);
		confirmRow.setFont(Font.getFont(Font.FACE_MONOSPACE,Font.STYLE_BOLD,Font.SIZE_MEDIUM)); // change the font
		confirmRow.setAlignment(FireScreen.CENTRE);
		confirmRow.addCommand(confirm);
		confirmRow.setCommandListener(this);
		
		this.add(requestRow);
		this.add(confirmRow);
	}
	
	public void commandAction(Command cmd, Component c) {
		if (cmd == request) {
			richiestaAttivazione();
		}
		else if (cmd == confirm) {
			confermaAttivazione();
		}
		else {//if (cmd == this.exit) 
			_midlet.showMenu();
		}
	}
	
	public void cancellaRichieste() {
		try {
			Logger.getInstance().write("Elimininazione richieste");
			CMessage[] sent = AddressService.getPKRegisterRequest();
			for (int i = 0; i < sent.length; i++) {
				AddressService.deleteMessage(sent[i]);
			}
		} catch (Exception e) {
			_midlet.showError(e.getMessage());
		}
	}
	
	public void richiestaAttivazione() {
		try {
			
			if (richiestaGiaInviata() == true) {
				new ModalAlert("Attenzione", 
						   "Richiesta gia' inviata.\nSi vuole procedere al re-invio?", 
						   AlertType.INFO, 
						   _midlet.getFireScr(),
						   _midlet.getFireScr().getCurrentPanel(),
						   this,
						   ConstantA2P.COMMAND_TYPE_REINVIO);
				return;	
			}
			
			byte[] data = _midlet.getSettings().getPublicKey();
			
			CMessage n = new CMessage(_midlet.getSettings().getNumber(), SMSC, MessageType.PK_REGISTER_REQUEST, data, new Date());
			
			if (MessageSender.getInstance() != null)
				MessageSender.getInstance().send(SMSC, n.prepareForTransport());
			
			AddressService.saveMessage(n);
			
//			new ModalAlert("Info", 
//					   "Richiesta di attivazione inviata.\nAttendere la richiesta di conferma del server.", 
//					   AlertType.INFO, 
//					   _midlet.getFireScr(),
//					   _midlet.getFireScr().getCurrentPanel(),
//					   this,
//					   ConstantA2P.COMMAND_TYPE_INFO);
//			_midlet.setTicketText("Richiesta di attivazione inviata. Attendere la richiesta di conferma del server.");
			
			_midlet.getFireScr().getCurrentPanel().showAlert("Richiesta di attivazione inviata. Attendere la richiesta di conferma del server.", null);
		}
		
		catch (Exception e) {
			e.printStackTrace();
			_midlet.showError(e.getMessage());
		}
	}
	

	
	private boolean richiestaGiaInviata() {
		
		try {
			CMessage[] sent = AddressService.getPKRegisterRequest();
			
			if (sent.length > 0) {				
				return true;
			}
		} 
		
		catch (Exception e) {
			_midlet.showError(e.getMessage());
		}
		
		return false;
	}
	
	public void confermaAttivazione() {
		// Verifica se la richiesta non e' gia stata inviata
		if (richiestaGiaInviata() == false) {
			new ModalAlert("Attenzione", 
					   "Richiesta di attivazione non inviata.\nSi vuole procedere all'invio?", 
					   AlertType.INFO, 
					   _midlet.getFireScr(),
					   _midlet.getFireScr().getCurrentPanel(),
					   this,
					   ConstantA2P.COMMAND_TYPE_INVIO);
			Logger.getInstance().write("++++++++++++++++++++++++");
			return;	
		}
		
		// Controlla se il server ha inviato la challenge
		CMessage challenge = ottieniChallenge();
		if (challenge == null) {
//			new ModalAlert("Attenzione", 
//					   "Il server non ha ancora inviato la richiesta di conferma. Attendere", 
//					   AlertType.INFO, 
//					   _midlet.getFireScr(),
//					   _midlet.getFireScr().getCurrentPanel(),
//					   this,
//					   ConstantA2P.COMMAND_TYPE_INFO);
			
			_midlet.getFireScr().getCurrentPanel().showAlert("Il server non ha ancora inviato la richiesta di conferma. Attendere", null);
			
			return;	
		}
		
		completaRegistrazione(challenge);
	}
	
		
	public void completaRegistrazione(final CMessage challenge) {
		
		// La challenge e' stata inviata... lanciamo tutto il processo in una progress bar fino all'invio della
		// "challenge response"

		FireScreen d = _midlet.getFireScr();
		d.setCurrent(new ProgressScreen(this , "Elaborazione in corso", _midlet.getFireScr()) {
			
			public void run() {
					
				try {
					this.setStatus("Decifro la challenge2");
					
					
					
					
					Cipher _cipher = Utils.GetCipher(_midlet.getSettings().getAlghoritm());
					IKeyPairGenerator _keyPairGenerator = Utils.GetKeyPairGenerator(_midlet.getSettings().getAlghoritm());
					
					KeyPair kp = _keyPairGenerator.getKeyPairFromABytePair( _midlet.getSettings().getPublicKey(), _midlet.getSettings().getPrivateKey());
					
					byte [] challenge_dec= _cipher.decrypt(challenge.getData(), kp);
					
					if (challenge_dec == null) {	
						throw new Exception("Challenge decryption failed");
					}
					Logger.getInstance().write(new String(challenge_dec));
					
					// Completa la registrazione
					HMac mac = new HMac(new SHA1Digest());
					mac.init(new KeyParameter(challenge_dec));
					
					
					String challengeResp = new String("Decriptato con successo");
					mac.update(challengeResp.getBytes(), 0, challengeResp.getBytes().length);
					byte[] HMACResp = new byte[mac.getUnderlyingDigest().getDigestSize()];
					mac.doFinal(HMACResp, 0);
					
					byte[] aaaa = challengeResp.getBytes();
					byte[] lastMess = new byte[aaaa.length + HMACResp.length];
					System.arraycopy(aaaa, 0, lastMess, 0, aaaa.length);
					System.arraycopy(HMACResp, 0, lastMess, aaaa.length, HMACResp.length);
					
					// Invia l'ultimo messaggio al server: challengeResp+HMAC
					//this.setStatus("Invio la conferma alla registrazione");
					CMessage m = new CMessage(_midlet.getSettings().getNumber(), SMSC, MessageType.PK_CHALLENGE_RESPONSE, lastMess, new Date());
					
					
					if (MessageSender.getInstance() != null) 
							MessageSender.getInstance().send(SMSC, m.prepareForTransport());					
				}
				catch (InvalidCipherTextException e) {
					Logger.getInstance().write((_midlet.getResourceManager().getString(ResourceTokens.STRING_ERROR_DECRYPT  + "\n" + e.getMessage())));
				}
				catch (Exception e) {	
					
					Logger.getInstance().write((_midlet.getResourceManager().getString(ResourceTokens.STRING_ERROR_THREAD  + "\n"+ e.getMessage())));
					e.printStackTrace();
				} 
				finally {	 
					stop();
//					new ModalAlert("Info", 
//							   "La conferma e' stata inviata.\nUn SMS cifrato confermera' l'attivazione del servizio.", 
//							   AlertType.INFO, 
//							   get_display(),
//							   get_next(),
//							   null,
//							   ConstantA2P.COMMAND_TYPE_INFO);
//					_midlet.setTicketText("La conferma e' stata inviata. Un SMS cifrato confermera' l'attivazione del servizio");

					_midlet.getFireScr().getCurrentPanel().showAlert("La conferma e' stata inviata. Un SMS cifrato confermera' l'attivazione del servizio.", null);
				}  
			}	
		}) ;
		
		System.out.println("completaRegistrazione....exiting");
	}
	
	public CMessage ottieniChallenge() {	
		
		CMessage ch = null;
		try {
			CMessage[] challenges;
			challenges = AddressService.getPKServerChallenge();
			
			//restituisci solo la challenge pi� recente e cancella le altre
			
			if (challenges.length  > 0) {
				ch = challenges[0];
				AddressService.deleteMessage(challenges[0]);
				
				for (int i = 1; i < challenges.length; i++) {
					if (challenges[i].getSender().compareTo(SMSC) == 0 ) {
						Calendar c1 = Calendar.getInstance();
						c1.setTime(ch.getTime());
						
						if (c1.before(challenges[i].getTime()))
							ch = challenges[i];
						
						AddressService.deleteMessage(challenges[i]);
					}
				}
				//Cancella le richieste di challenge visto che ne abbiamo ricevuta una
				cancellaRichieste();
				
				return ch;
			}
		} 
		catch (Exception e) {
			_midlet.showError(e.getMessage());
		}
		
		return null;
	}
}