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
package org.cryptosms.test;

import java.util.Date;

import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.midlet.MIDletStateChangeException;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.IESEngine;
import org.bouncycastle.crypto.generators.KDF2BytesGenerator;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.IESParameters;
import org.bouncycastle.math.BigInteger;
import org.bouncycastle.security.SecureRandom;
import org.cryptosms.helpers.CryptoHelper;
import org.cryptosms.helpers.Helpers;

public class TestCryptoStuff extends TestFrame {

	SecureRandom PRNG;

	private Display display;

	protected void startApp() throws MIDletStateChangeException {
		display = Display.getDisplay(this);
		form = new Form("Test");
		form.addCommand(CMD_EXIT);
		form.setCommandListener(this);
		displayString("start: " + Helpers.dateToStringFull(new Date()));

		// ECIESTest.main(null);
		try {

			ECDomainParameters ecparms = CryptoHelper.prime192v1;

			System.out.println("after domain: " + Helpers.dateToStringFull(new Date()));
			displayString("after domain: " + Helpers.dateToStringFull(new Date()));

			PRNG = SecureRandom.getInstance("SHA256PRNG");
			BigInteger privNum = new BigInteger(PRNG.generateSeed(16));

			displayString("after random: " + Helpers.dateToStringFull(new Date()));

			ECPrivateKeyParameters priv = new ECPrivateKeyParameters(privNum, ecparms);
			displayString("after priv key: " + Helpers.dateToStringFull(new Date()));

			ECPublicKeyParameters pub = CryptoHelper.generatePublicKey(priv);

			displayString("after pub key: " + Helpers.dateToStringFull(new Date()));
			System.out.println("Public Key length: "+pub.getQ().getEncoded().length);

			AsymmetricCipherKeyPair p1 = new AsymmetricCipherKeyPair(pub, priv);
			AsymmetricCipherKeyPair p2 = new AsymmetricCipherKeyPair(pub, priv);
			IESEngine engine1 = new IESEngine(new ECDHBasicAgreement(), new KDF2BytesGenerator(new SHA1Digest()),
					new HMac(new SHA1Digest()));
			IESEngine engine2 = new IESEngine(new ECDHBasicAgreement(), new KDF2BytesGenerator(new SHA1Digest()),
					new HMac(new SHA1Digest()));
			byte[] d = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
			byte[] e = new byte[] { 8, 7, 6, 5, 4, 3, 2, 1 };
			IESParameters p = new IESParameters(d, e, 64);

			engine1.init(true, p1.getPrivate(), p2.getPublic(), p);
			engine2.init(false, p2.getPrivate(), p1.getPublic(), p);

			displayString("after enginesetup: " + Helpers.dateToStringFull(new Date()));

			byte[] msg = "This is a test message, so long, noone would ever type it in by hand.".getBytes();
			byte[] crypted = engine1.processBlock(msg, 0, msg.length);
			displayString("encrypted: "+crypted.toString());

			displayString("after encryption: " + Helpers.dateToStringFull(new Date()));
			displayString("len: " + crypted.length);

			byte[] clear = engine2.processBlock(crypted, 0, crypted.length);

			displayString("after decryption: " + Helpers.dateToStringFull(new Date()));
			displayString("text: " + new String(clear));

		} catch (Exception exp) {
			System.out.println("Exception: " + exp.toString());
		}
		form.append("finish: " + Helpers.dateToStringFull(new Date()));
		if (display != null)
			display.setCurrent(form);

	}

}
