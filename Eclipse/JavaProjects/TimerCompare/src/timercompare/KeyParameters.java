/*
 * $Revision: 1.1 $
 * $Date: 2008-11-14 14:38:23 $
 */

package	timercompare;

import java.math.BigInteger;
import java.util.logging.Level;

import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.util.encoders.Hex;


public enum KeyParameters {
	
	/*
	 * definizione dei parametri delle curve raccomandate da SEC2 per
	 * EC over Fp
	 * Following SEC 1 [12], elliptic curve domain parameters over F p are a sextuple:
	 *          T = (p;a;b;G;n;h)
	 */
	
	/*
	 * secp112r2 2.2.2 56 112 512
	 * secp128r2 2.3.2 64 128 704
	 * secp160r2 2.4.3 80 160 1024
	 * secp192r1 2.5.2 96 192 1536
	 * secp224r1 2.6.2 112 224 2048
	 * secp256r1 2.7.2 128 256 3072
	 * secp384r1 2.8.1 192 384 7680
	 * secp521r1 2.9.1 256 521 15360
	 */
	
	secp112r2(
			"DB7C2ABF62E35E668076BEAD208B",	// P
			"6127C24C05F38A0AAAF65C0EF02C", // A
			"51DEF1815DB5ED74FCC34C85D709",	// B
			"034BA30AB5E892B4E1649DD0928643", // G
			"36DF0AAFD8B8D7597CA10520D04B", // N
			"4", // H
			"002757A1114D696E6768756151755316C05E0BD4", 	// Seed
			512,							// lunghezza chiave RSA equivalente
			63),							// lunghezza massima del blocco da cifrare x RSA
			
	secp128r2(
			"FFFFFFFDFFFFFFFFFFFFFFFFFFFFFFFF",				// p
			"D6031998D1B3BBFEBF59CC9BBFF9AEE1",				// a
			"5EEEFCA380D02919DC2C6558BB6D8A5D",				// b
			"027B6AA5D85E572983E6FB32A7CDEBC140",			// G
			"3FFFFFFF7FFFFFFFBE0024720613B5A3",				// n
			"4",											// h
			"004D696E67687561517512D8F03431FCE63B88F4",		// Seed
			704,							// lunghezza chiave RSA equivalente
			87),							// lunghezza massima del blocco x RSA
			
	secp160r2(
			"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFAC73",		// p
			"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFAC70",		// a
			"B4E134D3FB59EB8BAB57274904664D5AF50388BA",		// b
			"0252DCB034293A117E1F4FF11B30F7199D3144CE6D",	// G compressed format
			"0100000000000000000000351EE786A818F3A1A16B",	// n
			"1",											// h
			"B99B99B099B323E02709A4D696E6768756151751",		// Seed
			1024,							// lunghezza chiave RSA equivalente
			127),							// lunghezza massima del blocco da cifrare x RSA
	
	secp192r1(
/* q */	// "6277101735386680763835789423207666416083908700390324961279", // BouncyCastle e cryptoSMS usano questo valore
/* p */		"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFFFFFFFFFFFF",				 // mentre SEC suggerisce questo ... ma e' lo stesso valore solo in Hex chio chio
/* a */		"fffffffffffffffffffffffffffffffefffffffffffffffc", 
/* b */		"64210519e59c80e70fa7e9ab72243049feb8deecc146b9b1", 
/* G */		"03188da80eb03090f67cbf20eb43a18800f4ff0afd82ff1012",		// N.B. compressed format 
/* n */		"ffffffffffffffffffffffff99def836146bc9b1b4d22831",
/* h */		"1",
/* Seed */	"3045AE6FC8422f64ED579528D38120EAE12196D5",
			1536,							// lunghezza chiave RSA equivalente
			191),							// lunghezza massima del blocco da cifrare x RSA

	secp224r1(
/* p */		"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF000000000000000000000001",		// p
/* a */		"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFE",		// a
/* b */		"B4050A850C04B3ABF54132565044B0B7D7BFD8BA270B39432355FFB4",		// b
/* G */		"02B70E0CBD6BB4BF7F321390B94A03C1D356C21122343280D6115C1D21",	// G
/* n */		"FFFFFFFFFFFFFFFFFFFFFFFFFFFF16A2E0B8F03E13DD29455C5C2A3D",		// n
/* h */		"1",															// h
/* Seed */	"BD71344799D5C7FCDC45B59FA3B9AB8F6A948BC5",						// Seed
			2048,							// lunghezza chiave RSA equivalente
			255),							// lunghezza massima del blocco da cifrare x RSA

	secp256r1(
/* p */		"FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF",	// p
/* a */		"FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFC",	// a
/* b */		"5AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B",	// b
/* G */		"036B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C296", // G	
/* n */		"FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551",	// n
/* h */		"1",																// h
/* Seed */	"C49D360886E704936A6678E1139D26B7819F7E90",							// Seed
			3072,							// lunghezza chiave RSA equivalente
			383),							// lunghezza massima del blocco da cifrare x RSA

	secp384r1(
/* p */		"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFFFF0000000000000000FFFFFFFF",
/* a */		"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFFFF0000000000000000FFFFFFFC",
/* b */		"B3312FA7E23EE7E4988E056BE3F82D19181D9C6EFE8141120314088F5013875AC656398D8A2ED19D2A85C8EDD3EC2AEF",
/* G */		"03AA87CA22BE8B05378EB1C71EF320AD746E1D3B628BA79B9859F741E082542A385502F25DBF55296C3A545E3872760AB7",
/* n */		"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFC7634D81F4372DDF581A0DB248B0A77AECEC196ACCC52973",
/* h */		"1",
/* Seed */	"A335926AA319A27A1D00896A6773A4827ACDAC73",
			7680,							// lunghezza chiave RSA equivalente
			959),							// lunghezza massima del blocco da cifrare x RSA

	secp521r1(
/* p */		"01FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF",
/* a */		"01FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFC",
/* b */		"0051953EB9618E1C9A1F929A21A0B68540EEA2DA725B99B315F3B8B489918EF109E156193951EC7E937B1652C0BD3BB1BF073573DF883D2C34F1EF451FD46B503F00",
/* G */		"0200C6858E06B70404E9CD9E3ECB662395B4429C648139053FB521F828AF606B4D3DBAA14B5E77EFE75928FE1DC127A2FFA8DE3348B3C1856A429BF97E7E31C2E5BD66",
/* n */		"01FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFA51868783BF2F966B7FCC0148F709A5D03BB5C9B8899C47AEBB6FB71E91386409",
/* h */		"1", // h
/* Seed */	"D09E8800291CB85396CC6717393284AAA0DA64BA",	
			15359,							// lunghezza chiave RSA equivalente
			0);								// lunghezza massima del blocco da cifrare x RSA
			// fine enum

	private BigInteger p; //$%$£&£&%£&$£&%£ P e Q sono la stessa cosa ?!?!?!?!?
	private BigInteger a;
	private BigInteger b;
	private byte[] G;
	private BigInteger n;
	private BigInteger h;
	private byte[] Seed; // Seed
	private int RSAKeyLen;
	private int maxBlockLen;


	
	private KeyParameters(String p, String a,String b,String G,String n,String h,String S,
			int len, int blocLen) {
		this.p = new BigInteger(p, 16);
		this.a = new BigInteger(a, 16);
		this.b = new BigInteger(b, 16);
		this.G = Hex.decode(G);
		this.n = new BigInteger(n, 16);
		this.h = new BigInteger(h);
		this.Seed = Hex.decode(S);
		this.RSAKeyLen = len;
		this.maxBlockLen = blocLen;
	}
	
	public BigInteger getP() {
		return p;
	}

	public BigInteger getA() {
		return a;
	}

	public BigInteger getB() {
		return b;
	}

	public byte[] getG() {
		return G;
	}

	public BigInteger getN() {
		return n;
	}

	public BigInteger getH() {
		return h;
	}

	public byte[] getSeed() {
		return Seed;
	}

	public int getRSAKeyLen() {
		return RSAKeyLen;
	}

	public int getMaxBlockLen() {
		return maxBlockLen;
	}

}
