package org.smslib;

import smsserver.utils.Utils;

public class COutgoingBinaryMessage extends COutgoingMessage {

	private static final long serialVersionUID = 2L;
	
	public COutgoingBinaryMessage() {
		super();
		setDcs(4);
		setMessageEncoding(MessageEncoding.EncCustom);
	}
	
	public void setPayload(byte[] binaryData){
		
		String hexStr = Utils.bytesToHexString(binaryData);
		
		setText(hexStr);
		setMessageEncoding(MessageEncoding.EncCustom);
	}
	
}
