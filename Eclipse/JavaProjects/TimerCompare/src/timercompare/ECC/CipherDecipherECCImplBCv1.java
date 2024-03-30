package timercompare.ECC;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.IESEngine;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.generators.KDF2BytesGenerator;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.IESParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.util.encoders.Hex;

import timercompare.CipherDecipherEngine;
import timercompare.utility.TCUtility;
import java.io.*;

// public class CipherDecipherECCImplBCimplements CipherDecipherEngine {
//
//	private byte[] dataOriginale;
//	private byte[] dataCifrato;
//	private final byte[] d = new byte[]{1,2,3,4,5,6,7,8};
//	private final byte[] e = new byte[]{8,7,6,5,4,3,2,1};
//	private int keySize;
//	private IESEngine enginC;
//	private ECCurve.Fp curve112;
//	private ECDomainParameters prime112v1;
//	private ECKeyPairGenerator ecPGen;
//	private ECKeyGenerationParameters ecGenParam;
//	private AsymmetricCipherKeyPair keyPair;
//	private ECPublicKeyParameters ecPubKeyParams;
//	private ECPrivateKeyParameters ecPriKeyParams;
//	private IESParameters p;
//
//	
//	public CipherDecipherECCImplBC(byte[] dataInput){
//		//keySize can be  160, 224, 256, 384, 512
//		dataOriginale = dataInput;
//		keySize = 160;
////		initEngine();
//	}
//	
//	public CipherDecipherECCImplBC(byte[] dataInput, int pKeySize){
//		//keySize can be  160, 224, 256, 384, 512
//		dataOriginale = dataInput;
//		keySize = pKeySize;
////		initEngine();
//	}
//	
//	public CipherDecipherECCImplBC(File fileInput){
//		try {
//			dataOriginale = TCUtility.getBytesFromFile(fileInput);
//		} catch (IOException e) {
//			dataOriginale = new byte[0];
//			System.err.println("CipherDecipherECCImplBC: file not exists");
//		}
//		keySize = 1024;
//		//initEngine();
//	}
//	
//	public CipherDecipherECCImplBC(File fileInput, int pKeySize){
//		try {
//			dataOriginale = TCUtility.getBytesFromFile(fileInput);
//		} catch (IOException e) {
//			dataOriginale = new byte[0];
//			System.err.println("CipherDecipherECCImplBC: file not exists");
//		}
//		keySize = pKeySize;
////		initEngine();
//	}
//	
//	public void initEngine(){
//		// System.out.println(d+" "+e+" "+d+" "+e);
//		// Creazione motore di cifratura
//		enginC = new IESEngine(new ECDHBasicAgreement(),
//				new KDF2BytesGenerator(new SHA1Digest()), 
//				new HMac(new SHA1Digest()));
//
//		// Creazione curva e parametri di curvatura (da fare FINAL)
//		curve112 = new ECCurve.Fp(
//				new BigInteger("4451685225093714772084598273548427"),	
//				new BigInteger("DB7C2ABF62E35E668076BEAD2088",16),	
//				new BigInteger("659EF8BA043916EEDE8911702B22",16));
//
//		// Creazione parametri di dominio (da fare FINAL)
//		prime112v1 = new ECDomainParameters(curve112,
//				curve112.decodePoint(Hex.decode("0209487239995A5EE76B55F9C2F098")),
//				new BigInteger("DB7C2ABF62E35E7628DFAC6561C5",16),BigInteger.valueOf(1),
//				Hex.decode("00F50B028E4D696E676875615175290472783FB1"));
//		
//		// Generazione chiavi
//		//System.out.println("generatore di chiavi ECC");
//		ecPGen = new ECKeyPairGenerator();
//		ecGenParam = new ECKeyGenerationParameters(prime112v1, new SecureRandom());
//		
//		// inizializzazione del generatore di chiavi
//		ecPGen.init(ecGenParam);
//		
//		// contenitore chiavi
//		keyPair = ecPGen.generateKeyPair();
//		
//		// prendiamo le chiavi
//		ecPubKeyParams = (ECPublicKeyParameters)keyPair.getPublic();
//		ecPriKeyParams = (ECPrivateKeyParameters)keyPair.getPrivate();
//		p = new IESParameters(d,e,keySize);
//	}
//	
//	public byte[] encrypt() {
//		// cifratura
////		System.out.println("cifratura di: "+dataOriginale);
//		enginC.init(true, ecPriKeyParams,ecPubKeyParams,p);
//		try {
//			dataCifrato = enginC.processBlock(dataOriginale, 0, dataOriginale.length);
//		} catch (InvalidCipherTextException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
////		System.out.println("cifrato: "+dataCifrato);
//		return dataCifrato;
//	}
//	
//	public byte[] decrypt() {
//		// decifratura
////		System.out.println("decifratura");
//		enginC.init(false, ecPriKeyParams,ecPubKeyParams,p);
//		try {
//			dataOriginale = enginC.processBlock(dataCifrato, 0, dataCifrato.length);
//		} catch (InvalidCipherTextException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
////		System.out.println("decifrato: "+dataOriginale);
//		return dataOriginale;
//		
//	}
//		
//}
