package jwhisper.gui;

import java.util.Calendar;
import java.util.Date;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import jwhisper.book.AddressService;
import jwhisper.book.CMessage;
import jwhisper.book.MessageType;
import jwhisper.connection.MessageSender;
import jwhisper.helpers.CryptoHelper;
import jwhisper.helpers.Logger;
import jwhisper.helpers.ResourceManager;
import jwhisper.helpers.ResourceTokens;


import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;


public class menuRegistrazione extends List implements CommandListener {

	private final Command exit;

	private final Command ok;

	private final JWhisperMidlet gui;

	private final String[] mainMenu = { "Richiedi l'attivazione al servizio", 
										"Crea una fake challenge del server",
										"Conferma la registrazione"
									   };
	private String SMSC;

	
	public menuRegistrazione(JWhisperMidlet parent) {
		super("Registrazione", Choice.IMPLICIT);
		
		ResourceManager rm = parent.getResourceManager();
		
		this.gui = parent;
		
		this.ok = new Command(rm.getString(ResourceTokens.STRING_OK), Command.OK, 1);
		this.exit = new Command(rm.getString(ResourceTokens.STRING_EXIT), Command.EXIT, 1);

		this.addCommand(this.ok);
		this.addCommand(this.exit);
		this.setCommandListener(this);
		
		this.setTicker(parent.getTicker());
		
//		SMSC = System.getProperty("wireless.messaging.sms.smsc");
//		SMSC = "+393804351160";
		
		if (isEmulator())
			SMSC = "+5554827";
		else
			SMSC = "+393804351160";
		
		buildList();
	}
	
	private boolean isEmulator () {
		Logger.getInstance().write(System.getProperty("microedition.platform"));
		
		String platform = System.getProperty("microedition.platform");
		boolean isEmulated = (platform != null && (platform.endsWith("JAVASDK") || platform.equals("j2me") || platform.equals("SunMicrosystems_wtk")));
		
		return isEmulated;
	}

	private void buildList() {
		this.deleteAll();

		for (int i = 0; i < mainMenu.length; i++) {
			this.append(mainMenu[i], null);
		}
	}

	public void commandAction(Command co, Displayable disp) {
		if (co == this.ok) {
			int i = this.getSelectedIndex();
			switch (i) {
			case 0:
				richiestaAttivazione();
				break;
			case 1:
				creaFakeChallenge();
				break;
			case 2: 
				confermaAttivazione();
				break;
			default:
				break;
			}
		}
		if (co == this.exit) {
			gui.showMenu();
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
			gui.showError(e.getMessage());
		}
	}
	
	public void richiestaAttivazione() {
		try {
			if (richiestaGiaInviata() == true) {
				new ModalAlert("Attenzione", 
						   "Richiesta gi� inviata.\nSi vuole procedere al re-invio?", 
						   AlertType.INFO, 
						   Display.getDisplay(gui),
						   Display.getDisplay(gui).getCurrent(),
						   this,
						   TipoComando.REINVIO);
				return;	
			}
			Displayable d = Display.getDisplay(gui).getCurrent();
			
			byte[] data = gui.getSettings().getPublicKey();
			CMessage n = new CMessage(gui.getSettings().getNumber(), SMSC, MessageType.PK_REGISTER_REQUEST, data, new Date());
			
			if (MessageSender.getInstance() != null)
				MessageSender.getInstance().send(SMSC, n.prepareForTransport());
			
			AddressService.saveMessage(n);
			
			new ModalAlert("Info", 
					   "Richiesta di attivazione inviata.\nAttendere la richiesta di conferma del server.", 
					   AlertType.INFO, 
					   Display.getDisplay(gui),
					   d,
					   this,
					   TipoComando.INFO);
			
			gui.setTicketText("Richiesta di attivazione inviata. Attendere la richiesta di conferma del server.");
		}
		
		catch (Exception e) {
			gui.showError(e.getMessage());
		}
	}
	
	private void creaFakeChallenge(){
		Display d = Display.getDisplay(gui);
		d.setCurrent( new ProgressScreen(this , "Elaborazione in corso fake challenge", d) {

			public void run() {
				this.setStatus("cifro la challenge");
				
				String c = "J2ME mi sembra un po troppo acerbo...";

				try { 
					byte[] data = CryptoHelper.encrypt(c.getBytes(), gui.getSettings().getPublicKey());
					if (data == null) {	
						throw new Exception("Aieeh ... encyption failed");
					}

					Logger.getInstance().write("Lunghezza dato cifrato " + data.length);
					CMessage fakechallenge = new CMessage(SMSC, gui.getSettings().getNumber(), MessageType.PK_SERVER_CHALLENGE, data, new Date());

					AddressService.saveMessage(fakechallenge);
					
					gui.setTicketText("Fake challenge creata ed inserita nello store.");
				} 
				catch (Exception e) {	
					Logger.getInstance().write((gui.getResourceManager().getString(ResourceTokens.STRING_ERROR_THREAD  + "\n"+ e.getMessage())));	
				}
				finally {	 
					stop();	
				}  
			}	
		} );			
	}
	
	private boolean richiestaGiaInviata() {
		
		try {
			CMessage[] sent = AddressService.getPKRegisterRequest();
			
			if (sent.length > 0) {				
				return true;
			}
		} 
		
		catch (Exception e) {
			gui.showError(e.getMessage());
		}
		
		return false;
	}
	
	public void confermaAttivazione() {
		// Verifica se la richiesta non e' gia stata inviata
		if (richiestaGiaInviata() == false) {
			new ModalAlert("Attenzione", 
					   "Richiesta di attivazione non inviata.\nSi vuole procedere all'invio?", 
					   AlertType.INFO, 
					   Display.getDisplay(gui),
					   Display.getDisplay(gui).getCurrent(),
					   this,
					   TipoComando.INVIO);
			Logger.getInstance().write("++++++++++++++++++++++++");
			return;	
		}
		
		// Controlla se il server ha inviato la challenge
		CMessage challenge = ottieniChallenge();
		if (challenge == null) {
			new ModalAlert("Attenzione", 
					   "Il server non ha ancora inviato la richiesta di conferma. Attendere", 
					   AlertType.INFO, 
					   Display.getDisplay(gui),
					   Display.getDisplay(gui).getCurrent(),
					   this,
					   TipoComando.INFO);
			return;	
		}
		
		completaRegistrazione(challenge);
	}
	
		
	public void completaRegistrazione(final CMessage challenge) {
		
		// La challenge � stata inviata... lanciamo tutto il processo in una progress bar fino all'invio della
		// "challenge response"
		Display d = Display.getDisplay(gui);
		d.setCurrent(new ProgressScreen(this , "Elaborazione in corso", d) {
			
			public void run() {
					
				try {
					this.setStatus("Decifro la challenge2");
					
					byte [] challenge_dec = CryptoHelper.decrypt(challenge.getData(), gui.getSettings().getPair().getKeyPair());
					if (challenge_dec == null) {	
						throw new Exception("Aieeh ... encyption failed");
					}
					Logger.getInstance().write(new String(challenge_dec));
					
					// Completa la registrazione
					//this.setStatus("Costruisco la conferma alla registrazione");
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
					CMessage m = new CMessage(gui.getSettings().getNumber(), SMSC, MessageType.PK_CHALLENGE_RESPONSE, lastMess, new Date());
					
					
					if (MessageSender.getInstance() != null) 
							MessageSender.getInstance().send(SMSC, m.prepareForTransport());					
				}
				catch (InvalidCipherTextException e) {
					Logger.getInstance().write((gui.getResourceManager().getString(ResourceTokens.STRING_ERROR_DECRYPT  + "\n" + e.getMessage())));
				}
				catch (Exception e) {	
					Logger.getInstance().write((gui.getResourceManager().getString(ResourceTokens.STRING_ERROR_THREAD  + "\n"+ e.getMessage())));
					e.printStackTrace();
				} 
				finally {	 
					stop();

					new ModalAlert("Info", 
							   "La conferma � stata inviata.\nUn SMS cifrato confermer� l'attivazione del servizio.", 
							   AlertType.INFO, 
							   get_display(),
							   get_next(),
							   null,
							   TipoComando.INFO);
					
					gui.setTicketText("La conferma � stata inviata. Un SMS cifrato confermer� l'attivazione del servizio");
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
			gui.showError(e.getMessage());
		}
		
		return null;
	}
}