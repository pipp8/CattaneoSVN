/*
 * $Revision: 1.4 $
 * $Date: 2009-07-08 16:55:32 $
 */

package timercompare.test;

import timercompare.CipherDecipherEngine;
import timercompare.KeyParameters;
import timercompare.ECC.CipherDecipherECCImplBC;
import timercompare.ECC.CipherDecipherECCImplProvider;
import timercompare.RSA.CipherDecipherRSAImplBC;
import timercompare.RSA.CipherDecipherRSAImplProvider;
import timercompare.utility.*;

import java.io.*;
import java.math.BigInteger;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Hex;

public class TCTest {
	
	private CipherDecipherEngine rsaEngBC;
	private CipherDecipherEngine eccEngBC;
	private Random rng; 
	private final int CYCLES = 100;
	private Logger logger= null;
	private byte[] inputSource = null;
	
	public TCTest() {
		logger = Logger.getLogger("RSAvsEC");
		logger.setLevel(Level.WARNING);

		rng = new Random();

		File f1 = new File("prova.txt");
		try {
			inputSource = TCUtility.getBytesFromFile(f1);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Input file does not exists");
		}
	}
	
	
	private void RSATest(){
		for(int i = 0; i < KeyParameters.values().length; i++) 
			RSATest(i);
	}
	
	private void RSATest(int which){
		
		logger.log(Level.INFO, "Starting RSA Engine");
		rsaEngBC = new CipherDecipherRSAImplBC( which);

		KeyParameters[] kp = KeyParameters.values();
		int inputLen = kp[which].getMaxBlockLen();
		
		// cipher 300 volte
	 	for (int i = 0; i < this.CYCLES; i++){

	 		byte[] plainText = getInputText(inputLen);

	 		logger.log(Level.INFO, "RSA Encryption input buffer (len:" + plainText.length + ")");

	 		
			byte[] ct = rsaEngBC.encrypt( plainText);
	 		logger.log(Level.INFO, "result (len:" + ct.length + ")");

			// decipher
		 	byte[]result = rsaEngBC.decrypt( ct);

		 	boolean uguali = Arrays.areEqual(plainText, result);
			if (!uguali) 
				logger.log(Level.SEVERE, "ATTENZIONE! Il test RSA è fallito.");
			else
				logger.log(Level.INFO, "step " + i + " ok");
		}
		logger.log(Level.INFO, "RSA Test terminated");
	}

	private void ECCTest(){
		for(int i = 0; i < KeyParameters.values().length; i++) 
			ECCTest(i);
	}
	
	private void ECCTest(int which){

		logger.log(Level.INFO, "Starting EC Engine");
		logger.setLevel(Level.INFO);
		
		eccEngBC = new CipherDecipherECCImplBC(which);

		KeyParameters[] kp = KeyParameters.values();
		int inputLen = kp[which].getMaxBlockLen();

	 	for (int i = 0; i < this.CYCLES; i++){
	 		// cypher step

	 		byte[] plainText = getInputText(inputLen);

	 		logger.log(Level.INFO, "EC Encryption input buffer (len:" + plainText.length+")");
	 		
	 		byte[] ct = eccEngBC.encrypt(plainText);
	 		logger.log(Level.INFO, "result (len:" + ct.length + ")");

			// decipher
		 	byte[]result = eccEngBC.decrypt( ct);

		 	boolean uguali = Arrays.areEqual(plainText, result);
			if (!uguali) 
				logger.log(Level.SEVERE, "ATTENZIONE! Il test ECC è fallito.");
			else
				logger.log(Level.INFO, "step " + i + " ok");
		}
		logger.log(Level.INFO, "EC Test terminated");
	}

	
	public byte[] getInputText(int len) {
		
		byte[] plainText = new byte[len];
		if (inputSource == null || inputSource.length < len *2) {
			logger.log(Level.SEVERE, "Plain Input Source non disponible. Exiting");
			System.exit(-1);
		}
		int pos = rng.nextInt( inputSource.length - len);
		for(int j = 0; j < len; j++)
				plainText[j] = inputSource[pos+j];
		
		return plainText;
	}
	
	public static void main(String[] args) throws Exception {
		TCTest t = new TCTest();

//		t.testParameters();
		
		KeyParameters[] kp = KeyParameters.values();
		for(int i = 0; i < kp.length-1; i++) // -1 per evitare RSA 15000
		{
			t.RSATest( i);
			t.ECCTest( i);
		}
	}

	public void testParameters() {
		String ecName = "prime192v1";
		ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(ecName);
		System.out.println("Curva: " + ecName);
		TCUtility.printParameterSpec(ecSpec);

		System.out.println();

		ECCurve ecCurve = new ECCurve.Fp(
				KeyParameters.secp192r1.getP(),
				KeyParameters.secp192r1.getA(),
				KeyParameters.secp192r1.getB());

		ECDomainParameters ecParam= new ECDomainParameters(
				ecCurve,
/* G */			ecCurve.decodePoint( KeyParameters.secp192r1.getG()),
/* n */			KeyParameters.secp192r1.getN(),
/* h */			KeyParameters.secp192r1.getH(),
/* seed */		KeyParameters.secp192r1.getSeed());

		TCUtility.printDomainParameters(ecParam);
		BigInteger p = new BigInteger("6277101735386680763835789423207666416083908700390324961279");
		BigInteger q = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFFFFFFFFFFFF", 16);
		System.out.println( p);
		System.out.println( q);
	}
}
