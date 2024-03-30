/*
 * Copyright (C) 2007 cryptosms.org
 *
 * This file is part of cryptosms.org.
 *
 * cryptosms.org is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * any later version.
 *
 * cryptosms.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with cryptosms.org; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.cryptosms.book;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import util.wma.BinaryMessage;

import org.cryptosms.helpers.Helpers;
import org.cryptosms.helpers.Logger;
import org.cryptosms.helpers.NullEncoder;

/**
 * @author sd
 */
public class CMessage {
	
	public static final byte	supportedVersion=1;
	
	private int _rmsID;

	private Date _time;

	private byte[] _data;

	private String _sender;

	private String _receiver;

	private byte _type;

	private byte _version;

	/**
	 * @param sender
	 * @param receiver
	 * @param type
	 * @param version
	 * @param data
	 *            TODO get version from settings!!!
	 */
	public CMessage(String sender, String receiver, byte type,
			byte[] data, Date time) {

		this._receiver = receiver;
		this._sender = sender;
		this._data = data;
		this._type = type;
		this._version = CMessage.supportedVersion;
		this._time = time;
		this._rmsID=-1; // meaning not yet stored to record store
	}

	public CMessage(byte[] record, int rmsID) throws IOException {
		ByteArrayInputStream Bstrm = new ByteArrayInputStream(record);
		DataInputStream Dstrm = new DataInputStream(Bstrm);
		this._rmsID = rmsID;
		// Length
		short len = Dstrm.readShort();
		if (len > record.length)
			throw new IOException("Record has invalid length");

		// header
		byte[] b = new byte[len + 2];
		Dstrm.read(b, 0, len + 2);
		_version = b[0];
		_type = b[1];

		// data
		_data = new byte[len];
		System.arraycopy(b, 2, _data, 0, len);
		_sender = Dstrm.readUTF();
		_receiver = Dstrm.readUTF();
		_time = new Date(Dstrm.readLong());
	}

	public byte getType() {
		return _type;
	}

	public void setType(byte type) {
		this._type=type;
	}
	
	public String getSender() {
		return _sender;
	}

	public byte[] getData() {
		return _data;
	}

	public byte[] getBytes() throws Exception {
		ByteArrayOutputStream bStrm = new ByteArrayOutputStream();
		DataOutputStream dStrm = new DataOutputStream(bStrm);
		dStrm.writeShort(_data.length);
		dStrm.write(_version);
		dStrm.write(_type);
		dStrm.write(_data);
		dStrm.writeUTF(_sender);
		dStrm.writeUTF(_receiver);
		dStrm.writeLong(_time.getTime());

		return bStrm.toByteArray();
	}

	public String getReceiver() {
		return _receiver;
	}
	public void setData(byte[] data) {
			this._data = data;
		}
	public int getRmsID() {
		return _rmsID;
	}

	public Date getTime() {
		return _time;
	}

	public void setTime(Date time) {
		this._time = time;
	}
	
	public byte getVersion() {
		return this._version;
	}
	
	/**
	 * pack the crypted message object into a byte array for transport 
	 * via SMS. The output format is 
	 * byte version
	 * byte type
	 * long timestamp
	 * byte[] data/payload
	 * 
	 * @param msg
	 * @return
	 */
	public byte[] prepareForTransport() throws IOException {
		
		byte[]	data=this.getData();
		long 	timestamp;

		Logger 	log=Logger.getInstance();
		
		if (this.getTime() != null) timestamp=this.getTime().getTime();
		else timestamp=new Date().getTime();
		
		ByteArrayOutputStream bStrm = new ByteArrayOutputStream();
		DataOutputStream dStrm = new DataOutputStream(bStrm);
		dStrm.write(_version);
		dStrm.write(_type);
		dStrm.writeLong(timestamp);
		dStrm.write(data);

		if (log != null) log.write("encoded :"+bStrm.size());
		
		return NullEncoder.encode(bStrm.toByteArray());
		
	}
	
	public static CMessage createFromTransport(BinaryMessage msg) throws Exception {
		if (msg != null) {
			byte[] 	data=NullEncoder.decode(msg.getPayloadData());
			String 	sender=msg.getAddress();
			String 	receiver="-";
			byte	version;
			byte	type;
			long	timestamp;
			
			Logger 	log=Logger.getInstance();
			if (log!=null) log.write("sender: "+sender+" length: "+data.length);
			
			if (sender != null) sender=Helpers.getNumberFromUrl(sender);
						
			ByteArrayInputStream Bstrm = new ByteArrayInputStream(data);
			DataInputStream Dstrm = new DataInputStream(Bstrm);

			// header
			
			version=Dstrm.readByte();
			if (version != supportedVersion) return null;
			
			type=Dstrm.readByte();
			timestamp=Dstrm.readLong();
			// data
			int 	len=Dstrm.available();
			byte[] 	payload=new byte[len];
			
			Dstrm.read(payload);

			if (Settings.findSettings() != null) receiver=Settings.findSettings().getName();
		
			if (log!=null) log.write("creating CMessage: "+len);
			return new CMessage(sender,receiver,type,payload,new Date(timestamp));
			
		}
		return null;
	}
	//this method is for testing purpose only
	public static CMessage createFromTransport(byte[] msg) throws Exception {
		System.out.println("only for testing!!!!!!!!!!");
		if (msg != null) {
			byte[] 	data=msg;
			String 	sender="5550000";
			String	receiver="-";
			byte	version;
			byte	type;
			long	timestamp;
			
			System.out.println("sender: "+sender+" length: "+data.length);
			Logger 	log=Logger.getInstance();
			if (log!=null) log.write("sender: "+sender+" length: "+data.length);
			
			if (sender != null) sender=Helpers.getNumberFromUrl(sender);
						
			ByteArrayInputStream Bstrm = new ByteArrayInputStream(data);
			DataInputStream Dstrm = new DataInputStream(Bstrm);

			// header
			
			version=Dstrm.readByte();
			if (version != supportedVersion) return null;
			
			type=Dstrm.readByte();
			timestamp=Dstrm.readLong();
			// data
			int 	len=Dstrm.available();
			byte[] 	payload=new byte[len];
			
			Dstrm.read(payload);
			
			if (log!=null) log.write("creating CMessage: "+len);
			
			if (Settings.findSettings() != null) receiver=Settings.findSettings().getName();
			
			return new CMessage(sender,receiver,type,payload,new Date(timestamp));
			
		}
		return null;
	}
	


}
