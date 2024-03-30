package jwhisper;

import javax.microedition.midlet.MIDlet;

import jwhisper.utils.ResourceManager;

public abstract class JWhisperMidlet extends MIDlet {

	protected final byte _applicationType;
	
	public JWhisperMidlet(byte appType) {
		_applicationType = appType;
	}

	abstract public ResourceManager getResourceManager();
	abstract public void setTicketText(String string);
	abstract public void notify(Object o);
	abstract public void showError(String error);

	public byte getApplicationType(){
		return _applicationType;
	}
}
