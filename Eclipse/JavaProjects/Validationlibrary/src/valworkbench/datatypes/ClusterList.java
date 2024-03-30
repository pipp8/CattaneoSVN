package valworkbench.datatypes;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import valworkbench.exceptions.EmptyListException;
/**
 * The ClusterList class provides an object representing a clustering 
 * Matrix through a linked list. A ClusterList object contains the following 
 * fields: 'nOfCluster' (an integer representing the number of clusters); 'count' (an integer 
 * vector containing, for each cluster, the relative size), 'clusterList' (a linked 
 * list vector containing, for each ListNode, indices value of items belonging to the cluster 
 * corresponding to the index row in clusterList).
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0
 */
public class ClusterList {
	private int nOfCluster;
    private int count[];
    private ListNode clusterList[];
    private ListNode tail[];  
    /**
	 * Default Constructor Method
	 * 
	 */
	public ClusterList()
	{
		//Default Constructor Method
	}
	/**
	 * Class constructor specifying the clusters number 
	 * 
	 * @param nOfCluster an integer representing the number of clusters
	 */
	public ClusterList(int nOfCluster) {
		this.setNOfCluster(nOfCluster);
		this.clusterList = new ListNode[nOfCluster];
		this.tail = new ListNode[nOfCluster];
		this.count = new int[nOfCluster];
		//List emptiness
		for(int i = 0; i<nOfCluster; i++)
		{
			this.clusterList[i] = null;
			this.tail[i] = null;
			this.count[i] = 0;
		}
		
	}
	/**
	 * Class constructor specifying a Cluster_Matrix object 
	 * 
	 * @param clusterMatrix the input Cluster_Matrix object
	 */
	public ClusterList(ClusterMatrix clusterMatrix) {
		this.setNOfCluster(clusterMatrix.getNOfCluster());
		this.clusterList = new ListNode[clusterMatrix.getNOfCluster()];
		this.tail = new ListNode[clusterMatrix.getNOfCluster()];
		this.count = new int[clusterMatrix.getNOfCluster()];
		//List emptiness
		for(int i = 0; i < clusterMatrix.getNOfCluster(); i++)
		{
			this.clusterList[i] = null;
			this.tail[i] = null;
			this.count[i] = 0;
		}
		this.clusterListInit(clusterMatrix);
	}
	/**
	 * Class constructor specifying a Data_Matrix object
	 * 
	 * @param dataMatrix the input Data_Matrix object
	 */
	public ClusterList(DataMatrix dataMatrix) {
		this.setNOfCluster(dataMatrix.getNOfItem());
		this.clusterList = new ListNode[dataMatrix.getNOfItem()];
		this.tail = new ListNode[dataMatrix.getNOfItem()];
		this.count = new int[dataMatrix.getNOfItem()];
		//List Emptiness
		for(int i = 0; i < dataMatrix.getNOfItem(); i++)
		{
			this.clusterList[i] = null;
			this.tail[i] = null;
			this.count[i] = 0;
		}
		this.clusterListInit(dataMatrix);
		
	}
	/**
	 * Initializes Cluster_List with a Cluster_Matrix object
	 * 
	 * @param clusterMatrix a Cluster_Matrix object
	 */
	public void clusterListInit(ClusterMatrix clusterMatrix)
	{
		ListNode pointer; 
		
		for(int i = 0; i < clusterMatrix.getNOfCluster(); i++)
		{
			this.clusterList[i] = new ListNode(clusterMatrix.getCMatrixValue(i, 0) ,null);
			pointer = this.clusterList[i];
			
			this.count[i] = clusterMatrix.getClusterSize(i);
			
			for(int j = 1; j < clusterMatrix.getClusterSize(i); j++)
			{
				pointer.next = new ListNode(clusterMatrix.getCMatrixValue(i, j), null);
				pointer = pointer.next;
			}
			this.tail[i] = pointer;
		}
	} 
	/**
	 * Initializes a Cluster_List with a Data_Matrix object
	 * 
	 * @param dataMatrix a Data_Matrix object
	 */
	public void clusterListInit(DataMatrix dataMatrix)
	{
		for(int i = 0; i <dataMatrix.getNOfItem(); i++)
		{
			//Add a new Element
			this.clusterList[i] = new ListNode(i, null);
			//Update tail
			this.tail[i] = this.clusterList[i];
			//Update counter
			this.count[i] = 1;
		}		
	}
	/**
	 * Append the indexj list at the end of the indexi list 
	 * 
	 * @param indexi a linked list 
	 * @param indexj a linked list 
	 */
	public void listUnion(int indexi, int indexj)
	{ 
		try
		{
			if(this.clusterList[indexj] == null)
			{
				throw new EmptyListException();
			}
			else
			{
				this.tail[indexi].next = this.clusterList[indexj]; 
				this.tail[indexi] = this.tail[indexj];
				this.clusterList[indexj] = null;
				this.tail[indexj] = null;
				this.count[indexi] += this.count[indexj];
				this.count[indexj] = 0;
			}
		}
		catch(EmptyListException e)
		{
			System.out.println(e.toString());
		}
	}	
	/**
	 * Converts a Cluster_List object to Cluster_Matrix object
	 * 
	 * @return a Cluster_Matrix object  
	 */
	public ClusterMatrix clusterListToCMatrix()
	{
		ClusterMatrix clusterMatrix = new ClusterMatrix(this.getNOfCluster());
		
		for(int i = 0; i < this.getNOfCluster(); i++)
		{
			clusterMatrix.setClusterSize(i, this.getCount(i));
			clusterMatrix.instanceCMatrixRow(i, this.getCount(i));
			
			ListNode pointer = clusterList[i];
			for(int j = 0; j < this.getCount(i); j++)
			{
				clusterMatrix.setCMatrixValue(i, j, Integer.parseInt(pointer.element.toString()));
				pointer = pointer.next;
			}
		}
				
		return clusterMatrix;
	}
	/**
	 * Number of clusters accessor method
	 * 
	 * @return an integer representing the number of clusters in Cluster_List
	 */
	public int getNOfCluster() {
		return nOfCluster;
	}
    /**
     * Number of clusters mutator method
     * 
     * @param nOfCluster an integer representing the number of clusters
     */
	public void setNOfCluster(int nOfCluster) {
		this.nOfCluster = nOfCluster;
	}
	/**
	 * Number of list elements accessor method 
	 * 
	 * @param cluster an integer representing cluster index
	 * @return an integer representing the number of elements in Cluster_List
	 */
	public int getCount(int cluster) {
		return count[cluster];
	}
	/**
	 * Number of list elements mutator method
	 * 
	 * @param cluster an integer representing cluster index
	 * @param value an integer representing the number of items in the specified cluster
	 */
	public void setCount(int cluster, int value) {
		this.count[cluster] = value;
	}
	/**
	 * Cluster list Accessor method
	 * 
	 * @param cluster an integer representing cluster index
	 * @return a reference to a Cluster_List object
	 */
	public ListNode getClusterList(int cluster) {
		return this.clusterList[cluster];
	}
	/**
	 * Cluster list mutator method
	 * 
	 * @param cluster an integer representing cluster index
	 * @param pointer a pointer to the head of cluster list
	 */
	public void setClusterList(int cluster, ListNode pointer){
		this.clusterList[cluster] = pointer;
	}
	/**
	 * Tail in cluster list accessor method
	 * 
	 * @param cluster an integer representing cluster index
	 * @return a reference to cluster list tail for the specified cluster index
	 */
	public ListNode getTail(int cluster) {
		return this.tail[cluster];	
	}
	/**
	 * Tail in cluster list Mutator method
	 * 
	 * @param cluster an integer representing cluster index 
	 * @param pointer a pointer to the tail of cluster list
	 */
	public void setTail(int cluster, ListNode pointer) {
		this.tail[cluster] = pointer;
	}
	/**
	 * Loads Cluster List from a text file
	 * 
	 * @param fName the name of the file to be loaded
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	public void loadFromFile(String fName) throws FileNotFoundException, IOException, NumberFormatException, Exception
	{
		String temp;
		File inputCList = new File(fName);
		
		FileReader inputStream = new FileReader(inputCList);
		BufferedReader bInputR = new BufferedReader(inputStream);
			  
		//Reads the first line of file
		temp = bInputR.readLine();
		StringTokenizer stFirst = new StringTokenizer(temp);
			
		for (int i = 0; i < 4; i++)stFirst.nextToken();
		
		//Loads Number of clusters  
		int nOfCluster = Integer.parseInt(stFirst.nextToken());
		this.setNOfCluster(nOfCluster); 
		
		this.clusterList = new ListNode[nOfCluster];
		this.tail = new ListNode[nOfCluster];
		this.count = new int[nOfCluster];
		
		//Loads cluster List
		//Reads Cluster Size
		for (int i = 0; i < this.getNOfCluster(); i++)
        {
			temp = bInputR.readLine();
			StringTokenizer stCList = new StringTokenizer(temp);
			this.setCount(i, Integer.parseInt(stCList.nextToken()));			
			
			//Creates Cluster List
			this.clusterList[i] = new ListNode(Integer.parseInt(stCList.nextToken()), null);
						
			ListNode pointer = this.clusterList[i];
			for(int j = 1; j < this.getCount(i); j++)
			{
				pointer.next = new ListNode(Integer.parseInt(stCList.nextToken()), null);
				pointer = pointer.next;
			}
			this.tail[i] = pointer;				 
		}
		bInputR.close();
		inputStream.close();
	}
	/**
	 * Loads Cluster List from a text file
	 * 
	 * @param fName the name of the file to be loaded
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	public void loadFromExcelFile(String fName) throws FileNotFoundException, IOException, NumberFormatException, Exception
	{
		POIFSFileSystem fs;
		
		fs = new POIFSFileSystem(new FileInputStream(fName));

		HSSFWorkbook wb = new HSSFWorkbook(fs);
		
		HSSFSheet sheet = wb.getSheetAt((short)0); 

		//reads row and column number
		
		//Reads Number of cluster
		HSSFRow row;
		row = sheet.getRow(0);
		HSSFCell cellr = row.getCell((short)2);
		this.setNOfCluster((int) cellr.getNumericCellValue());
		
		//Instanciate Cluster list
		this.clusterList = new ListNode[this.getNOfCluster()];
		this.tail = new ListNode[this.getNOfCluster()];
    	this.count= new int[this.getNOfCluster()];
    	    			
		//Reads clusters 
    	for (int i=0; i<this.getNOfCluster(); i++)
		{
			//Read Current Row
    		row = sheet.getRow(i+1);
			
    		//Read Cluster Size
			HSSFCell cellCluster = row.getCell((short)(0));
			this.setCount(i, (int)cellCluster.getNumericCellValue());
						
			//Creates Cluster List
			cellCluster = row.getCell((short)(1));
			this.clusterList[i] = new ListNode(cellCluster.getNumericCellValue(), null);
						
			ListNode pointer = this.clusterList[i];
			for(int j = 1; j < this.getCount(i); j++)
			{
				HSSFCell cellCIndex = row.getCell((short)(j+1));
				pointer.next = new ListNode((int)cellCIndex.getNumericCellValue(), null);
				pointer = pointer.next;
			}
			this.tail[i] = pointer;
			
		}
	}
	/**
	 * Stores Cluster List into a text file 
	 * 
	 * @param fName the name of the file to be stored
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */	
	public void storeToFile(String fName) throws FileNotFoundException, IOException, Exception
	{
		File outputCList = new File(fName);
		
		FileWriter fCLW = new FileWriter(outputCList);
		BufferedWriter bCLW = new BufferedWriter(fCLW);
		bCLW.write("No of Clusters = "+this.getNOfCluster());
		bCLW.newLine();
		
		for (int i = 0; i < this.getNOfCluster(); i++)
		{
			bCLW.write(this.getCount(i)+"\t");
			
			ListNode pointer = this.clusterList[i];
			
			int j = 0;
			while(pointer != null)
			{
				if (j == 0)
				{
					bCLW.write(pointer.element.toString());
				}
				else
				{
					bCLW.write(" "+pointer.element.toString());
				}
				j+=1;
				pointer = pointer.next;
			}
			bCLW.newLine();
		}
		bCLW.close();
		fCLW.close();
	}
	
	/**
	 * Stores Cluster List into a Sheet Excel file 
	 * 
	 * @param fName the name of the file to be stored
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */	
	public void storeToExcelFile(String fName) throws FileNotFoundException, IOException, Exception
	{
		FileOutputStream out = new FileOutputStream(fName);
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet();
		
		//Write First row
		HSSFRow row;
		row = sheet.createRow((short)0);
		HSSFCell cellr = row.createCell((short)0);
		cellr.setCellValue("Number of Cluster");
		cellr = row.createCell((short)1);
		cellr.setCellValue("=");
		cellr = row.createCell((short)2);
		cellr.setCellValue(this.getNOfCluster());
		
		//Write successive row
		for(int i = 0; i<this.getNOfCluster(); i++)
		{
			//Write Cluster size
			row = sheet.createRow((short)(i+1));
			HSSFCell cellCluster = row.createCell((short)0);
			cellCluster.setCellValue(this.getCount(i));
			
			ListNode pointer = this.clusterList[i];
			
			int j = 1;
			while(pointer != null)
			{					
				cellCluster = row.createCell((short)(j));
				cellCluster.setCellValue(Double.parseDouble(pointer.element.toString()));
				pointer = pointer.next;
				j+=1;
			}

		}
		out.flush();
		wb.write(out);
		out.close();
	}

}
