/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */

package crymes.util.helpers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import jwhisper.cgss.modules.recordstore.InvalidRecordIDException;
import jwhisper.cgss.modules.recordstore.RecordComparator;
import jwhisper.cgss.modules.recordstore.RecordEnumeration;
import jwhisper.cgss.modules.recordstore.RecordFilter;
import jwhisper.cgss.modules.recordstore.RecordStore;
import jwhisper.cgss.modules.recordstore.RecordStoreException;
import jwhisper.cgss.modules.recordstore.RecordStoreFullException;
import jwhisper.cgss.modules.recordstore.RecordStoreNotFoundException;
import jwhisper.cgss.modules.recordstore.RecordStoreNotOpenException;

import jwhisper.utils.Helpers;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.security.SecureRandom;



/**
 * Crypted Records for the world!
 * one password for all Recordstores
 * - the crypted is 
 * 
 * @author crypto
 * 
 */
@SuppressWarnings("unused")
public class CryptedRecordStore {

	private static boolean initialized = false;

	private static RecordStore magic_cookie_store;
	
	
	private static final String MAGIC_COOKIE_STORE_NAME="magic_cookie_store2";
	private static final String CRYPTED_KEYFILE_NAME="keyfile";
	
	private static final byte[] MAGIC_COOKIE = "MagicCookie".getBytes();

	private static AESFastEngine encryptEngine;
	private static AESFastEngine decryptEngine;
	
	//private static Digest digest;

	private static KeyParameter param;
	//private static String recordStoreGroupName ;

	private RecordStore store = null;
	private RSCache cache=null;

	private CryptedRecordStore(String name, boolean createIfNecessary) throws Exception {
		store = RecordStore.openRecordStore(name, createIfNecessary);
		
		if(true){
			cache=new RSCache(name);
			int i;
			byte[] ba;
			RecordEnumeration e=store.enumerateRecords(null, null, false);
			while(e.hasNextElement()){
				i=e.nextRecordId();
				ba=decrypt(store.getRecord(i));
				cache.add( new Record(i, ba));
			}
		}
	}

	private static void initialize(byte[] pass) {

		encryptEngine = new AESFastEngine();
		decryptEngine = new AESFastEngine();
		Digest digest = new SHA256Digest();
		
		byte[]	hash=new byte[digest.getDigestSize()];
		
		digest.reset();
		digest.update(pass, 0, pass.length);
		digest.doFinal(hash, 0);
		
		param = new KeyParameter(hash);

		if (checkPassword()) {
			initialized = true;
		} else {
			initialized = false;
		}

	}

	private static boolean checkPassword() {
		try {
			magic_cookie_store = RecordStore.openRecordStore(MAGIC_COOKIE_STORE_NAME, true);
			byte[] content = encrypt(MAGIC_COOKIE) ;
			

			if (magic_cookie_store.getNumRecords() > 0) {
				byte[] inStore = magic_cookie_store.getRecord(1);

				if (Helpers.compareByteArray(content, inStore)) {
					return true;
				} else {
					return false;
				}
			} else { // its new
				magic_cookie_store.addRecord(content, 0, content.length);
				return true;
			}
		} catch (Exception e) {
			return false;
		}
	}

public static void init(String password) throws Exception{
	initialize(password.getBytes());
	if(!initialized){
		throw new Exception("bad passphrase");
		
	}
}
	
	public static CryptedRecordStore open(String name, boolean createIfNecessary ) throws Exception {
//		if (!initialized)
//			initialize(passwort.getBytes());
		if (initialized) {
			return new CryptedRecordStore(name, createIfNecessary);
		} else {
			throw new Exception("CryptedRecordStore used uninitialized");
		}
	}

	public static void deleteCryptedRecordStore(String name) throws Exception {
		
		RecordStore.deleteRecordStore(name);
	}

	public void closeCryptedRecordStore() throws Exception {
		if (store != null) store.closeRecordStore();
		initialized=false;
		store = null;
		if (cache != null) cache.destroy();
		cache=null;
		// is there anything else todo to release the current Object
		// this=null ..
	}

	public void addRecord(byte[] data) throws Exception {
		if (store != null) {
			byte[] crypted = encrypt(data);
			int i=store.getNextRecordID();
			//store.setRecord(i,crypted, 0, crypted.length);
			store.addRecord(crypted, 0, crypted.length);
			cache.add(new Record(i, data));
		} else {
			throw new RecordStoreNotOpenException("trying to addRecord to closed CryptedRecordStore");
		}
	}
//	public void addRecord(byte[] data,int start,int length) throws Exception {
//		if (store != null) {
//			byte[] crypted = encrypt(data);
//
//			store.addRecord(crypted, 0, crypted.length);
//		} else {
//			throw new RecordStoreNotOpenException("trying to addRecord to closed CryptedRecordStore");
//		}
//	}
	//TODO handle open recordstore
	public static boolean isEmpty(String name) throws Exception{
		
		RecordStore rs=RecordStore.openRecordStore(name, true);
		boolean isEmpty=!(rs.getNumRecords()>0);
		rs.closeRecordStore();
		return isEmpty;
	}
	public int getNextRecordId() throws RecordStoreNotOpenException, RecordStoreException{
		return store.getNextRecordID();
	}
	public int getNumRecords() throws Exception {
		return store.getNumRecords();
	}
	
	public CryptedRecordEnumeration enumerateCryptedRecords( RecordFilter rf, RecordComparator rc, boolean keepUpdated) throws Exception {
		return new CryptedRecordEnumeration(this,   null,
				   null,false);
		
	}
	public int[] getRecordIds() throws RecordStoreNotOpenException, InvalidRecordIDException{
		RecordEnumeration e =store.enumerateRecords(null, null, false);
		int[] recs= new int[store.getNumRecords()];
		int i=0;
		while (e.hasNextElement()){
			recs[i]=e.nextRecordId();
			i++;
		}
		return recs;
	}

	public byte[] getRecord(int number) throws Exception {
		if (store != null) {
			return cache.getData(number);
//			byte[] content = store.getRecord(number);
//			return decrypt(content);
		} else {
			throw new RecordStoreNotOpenException("trying to getRecord of closed CryptedRecordStore");
		}
	}

	private synchronized static byte[] encrypt(byte[] text) throws IOException {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream dataStream = new DataOutputStream(byteStream);

		encryptEngine.init(true, param);
		int blocksize = encryptEngine.getBlockSize();
		dataStream.writeShort(text.length);
		dataStream.write(text);
		dataStream.close();

		byte[] in = new byte[((byteStream.size() + blocksize - 1) / blocksize) * blocksize];
		System.arraycopy(byteStream.toByteArray(), 0, in, 0, byteStream.size());
		byte[] out = new byte[in.length];

		for (int i = 0; i < in.length; i += blocksize) {
			encryptEngine.processBlock(in, i, out, i);
		}

		return out;
	}

	private synchronized static byte[] decrypt(byte[] crypted) throws IOException {
		byte[] clear = new byte[crypted.length];

		decryptEngine.init(false, param);
		int blocksize = decryptEngine.getBlockSize();

		for (int i = 0; i < crypted.length; i += blocksize) {
			decryptEngine.processBlock(crypted, i, clear, i);
		}

		ByteArrayInputStream inStream = new ByteArrayInputStream(clear);
		DataInputStream dataStream = new DataInputStream(inStream);

		int size = dataStream.readShort();
		byte[] out = new byte[size];
		dataStream.read(out);
		dataStream.close();

		return out;
	}

	/**
	 * @return
	 * @throws RecordStoreNotOpenException 
	 */
	public String getName() throws RecordStoreNotOpenException {
		return store.getName();
	}
	public void setRecord(int id,byte [] ba) throws Exception,IOException, RecordStoreNotOpenException, RecordStoreException{
		if (store != null) {
			byte[] crypted = encrypt(ba);
			
			store.setRecord(id,crypted, 0, crypted.length);
			cache.setData(id, ba);
			
		} else {
			throw new RecordStoreNotOpenException("trying to addRecord to closed CryptedRecordStore");
		}
	}
	public void deleteRecord(int id) throws Exception{
		store.deleteRecord(id);
		cache.deleteRecord(id);
	}
	/**
	 * @return
	 */
	
	// E' stupido sul server...
	public static boolean hasPass() {
		boolean value = false;
		
		File store = new File(MAGIC_COOKIE_STORE_NAME + ".ser"); // TBF quale directory, usiamo un URI ???
		  
		if (store.exists()) {
			value = true;
		}
		return value;
//		String [] stores=RecordStore.listRecordStores();
//		
//		if (stores == null) return false; // no stores at all - initial usage?
//		
//		boolean hasPass=false;
//		for (int j=0;j<stores.length;j++)
//		{
//			if(stores[j].equals(MAGIC_COOKIE_STORE_NAME)){
//				hasPass=true;
//				break;
//			}
//		}
//		hasPass = true;
//		return hasPass;
	}

	/**
	 * @throws RecordStoreException 
	 * @throws RecordStoreNotFoundException 
	 * 
	 */
	public static void deleteGroup() throws RecordStoreNotFoundException, RecordStoreException {
		if (magic_cookie_store==null){
			magic_cookie_store = RecordStore.openRecordStore(MAGIC_COOKIE_STORE_NAME, true);
		}
		magic_cookie_store.closeRecordStore();
		RecordStore.deleteRecordStore(MAGIC_COOKIE_STORE_NAME);
		
	}

	/**
	 * @param object
	 * @param object2
	 * @param b
	 * @return
	 * @throws Exception 
	 */
	public CacheEnumeration enumerateCache(RecordFilter rf, RecordComparator rc) throws Exception {
		
		return cache.getEnumeration(rf,rc);
	}
	
	
	private byte[] getCryptedKey() {
		return null;
	}
	
	private void putCryptedKey(byte[] key) {
		
	}
	
	private static void createKey(String password) {
		Digest digest = new SHA256Digest();
		SecureRandom PRNG = SecureRandom.getInstance("SHA256PRNG");
		
		// feed some randomness into the PRNG
		PRNG.setSeed(Runtime.getRuntime().freeMemory());
		PRNG.setSeed(System.currentTimeMillis());
		// TODO more and better randomness
		
		byte[]	realkey=new byte[digest.getDigestSize()];
		
		// generate a random key
		PRNG.nextBytes(realkey);
		
		// first hash the password
		byte[] hashphrase=new byte[digest.getDigestSize()];
		digest.reset();
		digest.update(password.getBytes(),0,password.length());
		digest.doFinal(hashphrase, 0);
		
		// encrypt the key with the hashed password
		AESFastEngine engine=new AESFastEngine();
	}
}
