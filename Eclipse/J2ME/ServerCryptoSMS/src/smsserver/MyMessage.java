package smsserver;

import java.util.Date;

public class MyMessage {
	
	// public static final byte	supportedVersion=1;
	
	// private int _rmsID;

	private boolean _isText;
	private Date _time;

	private byte[] _data;
	private String _text;

	// Numero mittente senza protocollo e porta
	private String _sender;

	// numero dest +3931231123
	private String _receiver;

	// non usato
	private byte _type;
	
	// Porta del mittente
	private int _fromPort = -1;
	
	// Porta del destinatario
	private int _toPort = -1;

	//private byte _version;

	public MyMessage(){
		
	}
	
	
	
	public MyMessage(String sender, String receiver, 
			 String text, Date time) {

		
		setSender(sender);
		setReceiver(receiver);
		setText(text);
		setTime(time);
		
		this._type = -1;
		

		
		//this._version = Message.supportedVersion;
		
		// this._rmsID=-1; // meaning not yet stored to record store
	}

	public Date getTime() {
		return _time;
	}

	public void setTime(Date time) {
		this._time = time;
	}

	public byte[] getData() {
		return _data;
	}

	public void setData(byte[] data) {
		this._data = data;
	}

	public String getText() {
		return _text;
	}

	public void setText(String text) {
		this._text = text;
		this._isText = true;
		_data = text.getBytes();
	}
	
	public String getSender() {
		return _sender;
	}

	public void setSender(String sender) {
		this._sender = sender;
		if ( sender.substring(0, 6).compareTo("sms://") == 0 ){
			this._sender = sender.substring(6);
		}
		String[]s = this._sender.split(":");
		if (s.length==2){
			this._sender = s[0];
			this._fromPort = Integer.parseInt(s[1]);
		} else {
			this._fromPort = -1;
		}
	}

	public String getReceiver() {
		return _receiver;
	}

	public void setReceiver(String receiver) {
		this._receiver = receiver;
		if (this._receiver != null ) {
			if ( this._receiver.substring(0, 6).compareTo("sms://") == 0 ){
				this._receiver = this._receiver.substring(6);
			}
			String[]s = this._receiver.split(":");
			if (s.length==2){
				this._receiver = s[0];
				this._toPort = Integer.parseInt(s[1]);
			} else {
				this._toPort = -1;
			}
		}
	}

	public byte getType() {
		return _type;
	}

	public void setType(byte type) {
		this._type = type;
	}

	public boolean isText() {
		return _isText;
	}

	public void setIsText(boolean isText) {
		this._isText = isText;
	}
	
	public int getFromPort() {
		return _fromPort;
	}

	public void setFromPort(int port) {
		_fromPort = port;
	}

	public int getToPort() {
		return _toPort;
	}

	public void setToPort(int port) {
		_toPort = port;
	}

	public String getSenderFullQualified(){
		String str = "sms://" + _sender;
		if (_fromPort != -1){
			str += ":" + _fromPort;
		}
		return str;
	}
	
	
	public String getReceiverFullQualified(){
		String str = "sms://" + _receiver;
		if (_toPort != -1){
			str += ":" + _toPort;
		}
		return str;
	}


}
