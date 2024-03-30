/* ** *** ***** ******* *********** *************
 * DIA - Dipartimento di Informatica ed Applicazioni "Renato M. Capocelli" 				
 * http://www.dia.unisa.it			
 * 
 ************* *********** ******* ***** *** ** */
 
package jwhisper.cgss.modules.recordstore;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


@SuppressWarnings({ "unchecked", "serial" })
public class RecordStore implements Serializable
{
	
	
  /**
   * @associates RecordStore 
   */
  
private static Hashtable recordStores = new Hashtable();
  
  private String name;
  private boolean open = false;
  private int version = 0;
  private long lastModified = 0;
  private int nextRecordID = 1;

  /**
   * @associates RecordListener 
   */
  private Vector recordListeners = new Vector();
  private Hashtable records = new Hashtable();
  
	
  private RecordStore(String name)
  {
    this.name = name;
  }
  
  
  public static void deleteRecordStore(String recordStoreName)
      throws RecordStoreException, RecordStoreNotFoundException
	{
    RecordStore recordStore = (RecordStore) recordStores.get(recordStoreName);
    if (recordStore == null) {
      throw new RecordStoreNotFoundException();
    }
    if (recordStore.open) {
      throw new RecordStoreException();
    }
    
    recordStores.remove(recordStoreName);
	}
	
  

	public static RecordStore openRecordStore(String recordStoreName, boolean createIfNecessary)
      throws RecordStoreException, RecordStoreFullException, RecordStoreNotFoundException
	{
		
    RecordStore recordStore = (RecordStore) recordStores.get(recordStoreName);
    if (recordStore == null) {
    	if (!createIfNecessary) {
    		throw new RecordStoreNotFoundException();
    	}
		// non e' stato ancora opened nella sessione corrente
		File store = null;
		ObjectInputStream ois = null;
		FileInputStream fis = null;
		try {
			store = new File(recordStoreName + ".ser"); // TBF quale directory, usiamo un URI ???
			  
			if (store.exists()) {
				// questo store e' stato salvato in precedenza
				fis = new FileInputStream( store);
				ois = new ObjectInputStream(fis);
			}
		}
		catch ( IOException ioException ) {
			System.err.println( "Error opening file." );
		} 
		  
		if (ois != null) {
			try {
				// legge lo store serializzato
				recordStore = (RecordStore) ois.readObject();
				ois.close();
				fis.close();
			}
			catch(EOFException e) {
				System.err.println("Corrupted Stream Riinitializing");
				recordStore = new RecordStore(recordStoreName);
			}
			catch ( ClassNotFoundException ex) {
				System.err.println( "Unable to create object." );
			}
			catch ( IOException ioException ) {
				System.err.println( "Error during read from file." );
			}
		}
		else {
			// non esiste non è mai stato serializzato prima
			try {
				recordStore = new RecordStore(recordStoreName);
				FileOutputStream fos = new FileOutputStream(store);
				ObjectOutputStream oos = new ObjectOutputStream( fos);
				
				oos.writeObject( recordStore);
				
				oos.close();
			}
		    catch ( IOException ioException ) {
		    	System.err.println( "Error saving the object" );
		    }
		}
		recordStores.put(recordStoreName, recordStore); // aggiunge il nuovo store all'elenco degli store aperti
		}
    recordStore.open = true;
    
		return recordStore;
	}
	
	
	public void closeRecordStore()
      throws RecordStoreNotOpenException, RecordStoreException
	{
    if (!open) {
      throw new RecordStoreNotOpenException();
    }
	else {
		File store = null;
		ObjectOutputStream  oos = null;
		FileOutputStream fos = null;
		try {
			store = new File(name + ".ser"); // TBF quale directory, usiamo un URI ???
			fos = new FileOutputStream(store);
			oos = new ObjectOutputStream( fos);
			
			oos.writeObject( this);
			
			oos.close();
		}
	    catch ( IOException ioException ) {
	    	System.err.println( "Error saving the object" );
	    }
	}

    recordListeners.removeAllElements();
    open = false;
	}
	
	
	public static String[] listRecordStores()
	{
    String[] result = new String[recordStores.size()];
    
    int i = 0;
    for (Enumeration e = recordStores.keys(); e.hasMoreElements(); ) {
      result[i] = (String) e.nextElement();
      i++;
    }
    
		return result;
	}
	
	
	public String getName()
	{
		return name;
	}
	
	
	public int getVersion()
      throws RecordStoreNotOpenException
	{
    if (!open) {
      throw new RecordStoreNotOpenException();
    }
    
    synchronized (this) {
      return version;
    }
	}


	public int getNumRecords()
      throws RecordStoreNotOpenException
	{
    if (!open) {
      throw new RecordStoreNotOpenException();
    }
    
    synchronized (this) {
      return records.size();
    }
	}


  public int getSizeAvailable()
	{
		return (int) Runtime.getRuntime().freeMemory();
	}

  
	public long getLastModified()
      throws RecordStoreNotOpenException
	{
    if (!open) {
      throw new RecordStoreNotOpenException();
    }

    synchronized (this) {
      return lastModified;
    }
	}


	public void addRecordListener(RecordListener listener)
	{
    if (!recordListeners.contains(listener)) {
      recordListeners.addElement(listener);
    }
	}
	
	
	public void removeRecordListener(RecordListener listener)
	{
    recordListeners.removeElement(listener);
	}
	
	
	public int getNextRecordID()
      throws RecordStoreNotOpenException, RecordStoreException
	{
    if (!open) {
      throw new RecordStoreNotOpenException();
    }

    synchronized (this) {
      return nextRecordID;
    }
	}
	
	
	public int addRecord(byte[] data, int offset, int numBytes)
      throws RecordStoreNotOpenException, RecordStoreException, RecordStoreFullException
	{
    if (!open) {
      throw new RecordStoreNotOpenException();
    }    
    if (data == null && numBytes > 0) {
      throw new NullPointerException();
    }
    
    byte[] recordData = new byte[numBytes];
    if (data != null) {
      System.arraycopy(data, offset, recordData, 0, numBytes);
    }
    
    int curRecordID;
    synchronized (this) {
      records.put(new Integer(nextRecordID), recordData);
      version++;
      curRecordID = nextRecordID;
      nextRecordID++;
      lastModified = System.currentTimeMillis();
    }

    fireAddedRecordListener(curRecordID);
    
    return curRecordID;  
  }
	
	
	public void deleteRecord(int recordId)
      throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException
	{
    if (!open) {
      throw new RecordStoreNotOpenException();
    }
    
    synchronized (this) {
      if (records.remove(new Integer(recordId)) == null) {
        throw new InvalidRecordIDException();
      }
      version++;
      lastModified = System.currentTimeMillis();
    }
    
    fireDeletedRecordListener(recordId);
	}
	

  public int getRecordSize(int recordId)
      throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException
	{
    if (!open) {
      throw new RecordStoreNotOpenException();
    }

    synchronized (this) {
      byte[] data = (byte[]) records.get(new Integer(recordId));
      if (data == null) {
        throw new InvalidRecordIDException();
      }
      
      return data.length;
    }
	}
	

  public int getRecord(int recordId, byte[] buffer, int offset)
      throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException
	{
    int recordSize;
    synchronized (this) {
      recordSize = getRecordSize(recordId);
      System.arraycopy(records.get(new Integer(recordId)), 0, buffer, offset, recordSize);
    }
    
		return recordSize;
	}
	
	
	public byte[] getRecord(int recordId)
      throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException
	{
    byte[] data;
    
    synchronized (this) {
      data = new byte[getRecordSize(recordId)];
      getRecord(recordId, data, 0);
    }
    
		return data;
	}
	
	
  public void setRecord(int recordId, byte[] newData, int offset, int numBytes)
      throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException, RecordStoreFullException
	{
    if (!open) {
      throw new RecordStoreNotOpenException();
    }

    byte[] recordData = new byte[numBytes];
    System.arraycopy(newData, offset, recordData, 0, numBytes);

    synchronized (this) {
      Integer id = new Integer(recordId);      
      if (records.remove(id) == null) {
        throw new InvalidRecordIDException();
      }      
      records.put(id, recordData);      
      version++;
      lastModified = System.currentTimeMillis();
    }
    
    fireChangedRecordListener(recordId);
	}

  
	
	public RecordEnumeration enumerateRecords(RecordFilter filter, RecordComparator comparator, boolean keepUpdated)
      throws RecordStoreNotOpenException
	{
    if (!open) {
      throw new RecordStoreNotOpenException();
    }

    return new RecordEnumerationImpl(filter, comparator, keepUpdated);
	}
  
  
  private void fireAddedRecordListener(int recordId)
  {
    for (Enumeration e = recordListeners.elements(); e.hasMoreElements(); ) {
      ((RecordListener) e.nextElement()).recordAdded(this, recordId);
    }
  }

  
  private void fireChangedRecordListener(int recordId)
  {
    for (Enumeration e = recordListeners.elements(); e.hasMoreElements(); ) {
      ((RecordListener) e.nextElement()).recordChanged(this, recordId);
    }
  }

  
  private void fireDeletedRecordListener(int recordId)
  {
    for (Enumeration e = recordListeners.elements(); e.hasMoreElements(); ) {
      ((RecordListener) e.nextElement()).recordDeleted(this, recordId);
    }
  }
  
  
  class RecordEnumerationImpl implements RecordEnumeration
  {
   
    RecordFilter filter;
    RecordComparator comparator;
    boolean keepUpdated;
    
    /**
     * @associates EnumerationRecord 
     */
    Vector enumerationRecords = new Vector();
    int currentRecord;
    
    
    RecordListener recordListener = new RecordListener()
    {
      
      public void recordAdded(RecordStore recordStore, int recordId)
      {
        rebuild();
      }

      
      public void recordChanged(RecordStore recordStore, int recordId)
      {
        rebuild();
      }
  
      
      public void recordDeleted(RecordStore recordStore, int recordId)
      {
        rebuild();
      }
    
    };
    
    
    RecordEnumerationImpl(RecordFilter filter, RecordComparator comparator, boolean keepUpdated)
    {
      this.filter = filter;
      this.comparator = comparator;
      this.keepUpdated = keepUpdated;

      rebuild();
      
      if (keepUpdated) {
        addRecordListener(recordListener);
      }
    }    
    
  
    public int numRecords()
    {
      return enumerationRecords.size();
    }
  
    
    public byte[] nextRecord()
        throws InvalidRecordIDException, RecordStoreNotOpenException, RecordStoreException
    {
      if (!open) {
        throw new RecordStoreNotOpenException();
      }      

      if (currentRecord >= numRecords()) {
        throw new InvalidRecordIDException();
      }

      byte[] result = ((EnumerationRecord) enumerationRecords.elementAt(currentRecord)).value;
      currentRecord++;      
      
      return result;
    }
  
    
    public int nextRecordId()
        throws InvalidRecordIDException
    {
      if (currentRecord >= numRecords()) {
        throw new InvalidRecordIDException();
      }

      int result = ((EnumerationRecord) enumerationRecords.elementAt(currentRecord)).recordId;
      currentRecord++;
      
      return result;
    }
  
    

    public byte[] previousRecord()
        throws InvalidRecordIDException, RecordStoreNotOpenException, RecordStoreException
    {
      if (!open) {
        throw new RecordStoreNotOpenException();
      }      
      if (currentRecord < 0) {
        throw new InvalidRecordIDException();
      }

      byte[] result = ((EnumerationRecord) enumerationRecords.elementAt(currentRecord)).value;
      currentRecord--;
      
      return result;
    }
  
    
    public int previousRecordId()
        throws InvalidRecordIDException
    {
      if (currentRecord < 0) {
        throw new InvalidRecordIDException();
      }

      int result = ((EnumerationRecord) enumerationRecords.elementAt(currentRecord)).recordId;
      currentRecord--;

      return result;
    }
  
    
    public boolean hasNextElement()
    {
      if (currentRecord == numRecords()) {
        return false;
      } else {
        return true;
      }
    }
  
    
    public boolean hasPreviousElement()
    {
      if (currentRecord == 0) {
        return false;
      } else {
        return true;
      }
    }
  
    
    public void reset()
    {
      currentRecord = 0;
    }
  
    
    public void rebuild()
    {
      enumerationRecords.removeAllElements();
      
      int position;
      for (Enumeration e = records.keys(); e.hasMoreElements(); ) {
        Object key = e.nextElement();
        if (filter != null && !filter.matches((byte[]) records.get(key))) {
          continue;
        }
        byte[] tmp_data = (byte[]) records.get(key);
        // here should be better sorting
        if (comparator != null) {
          for (position = 0; position < enumerationRecords.size(); position++) {
            if (comparator.compare(tmp_data, (byte[]) enumerationRecords.elementAt(position)) 
                == RecordComparator.FOLLOWS) {
              break;
            }
          }
        } else {
          position = enumerationRecords.size();
        }
        enumerationRecords.insertElementAt(
            new EnumerationRecord(((Integer) key).intValue(), tmp_data), position);
      }
      currentRecord = 0;
    }
  
    
    public void keepUpdated(boolean keepUpdated)
    {
      if (keepUpdated) {
        if (!this.keepUpdated) {
          rebuild();
          addRecordListener(recordListener);
        }
      } else {
        removeRecordListener(recordListener);
      }
      
      this.keepUpdated = keepUpdated;
    }
  
    
    public boolean isKeptUpdated()
    {
      return keepUpdated;
    }
  
    
    public void destroy()
    {
    }
    
    
    class EnumerationRecord
    {
      
      int recordId;
      byte[] value;
      
      
      EnumerationRecord(int recordId, byte[] value)
      {
        this.recordId = recordId;
        this.value = value;
      }
      
    }
    
  }
  
}

