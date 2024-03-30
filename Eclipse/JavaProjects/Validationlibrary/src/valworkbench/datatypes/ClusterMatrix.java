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
/**
 * The ClusterMatrix class provides an object representing a clustering 
 * Matrix. A ClusterMatrix type object contains a Matrix with the following 
 * fields: 'nOfCluster' (an integer representing the number of clusters), 'clusterSize' 
 * (an integer vector containing, for each cell, the number of items for each cluster), 
 * 'clusterMatrix' (a matrix containing, for each row, indices value
 * of items, belonging to the cluster corresponding to the index row of the matrix).
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0
 */
public class ClusterMatrix {  
	private int nOfCluster;  //Testare con file di esempio
	private int clusterSize[];
	private int clusterMatrix[][];
	/**
	* Default class constructor 
	*
	*/
	public ClusterMatrix()
	{
		//Default class constructor
	}
	/**
	* Class constructor specifying the number of clusters 
	* 
	* @param nOfCluster an integer representing the number of clusters
	*/
	public ClusterMatrix(int nOfCluster)
	{
		this.setNOfCluster(nOfCluster);
		this.clusterSize = new int[nOfCluster];
		this.clusterMatrix = new int[nOfCluster][];
	}
    /**
	* Cluster indices vector accessor method
	*
	* @param clusterIndex an integer representing the index of the required cluster 
	* @return a reference to an integer vector containing indices of cluster elements 
	*/
	public int[] getCMatrixRow(int clusterIndex)
	{
		int vector[] = new int[this.clusterSize[clusterIndex]];
		for (int j = 0; j < this.clusterSize[clusterIndex]; j++)
		{
			vector[j] = this.clusterMatrix[clusterIndex][j];
		}
		return vector;   
	}
	/**
	 * Cluster indices vector mutator method
	 * 
	 * @param clusterIndex an integer representing the index of cluster 
	 * to be modified
	 * @param itemIndices an integer vector representing item indices
	 */
	public void setCMatrixRow(int clusterIndex, int itemIndices[])
	{
		for (int i = 0; i < this.getClusterSize(clusterIndex); i++)
		{
			this.clusterMatrix[clusterIndex][i] = itemIndices[i];
		}
	}
	/**
	 * Instanciates cluster matrix row 
	 * 
	 * @param clusterIndex an integer representing the cluster index 
	 * @param clusterSize an integer representing  the number of cluster elements
	 */
	public void instanceCMatrixRow(int clusterIndex, int clusterSize)
	{
		this.clusterMatrix[clusterIndex] = new int[clusterSize];
	}
   /**
	* Instanciates cluster matrix rows 
	* 
	* @param clusterSize an integer vector representing the size of each cluster 
	*/
	public void instanceCMatrixRow(int clusterSize[])
	{
		for(int i = 0; i < this.getNOfCluster(); i++)
		{
			this.clusterMatrix[i] = new int[clusterSize[i]];
		}
	}
	/**
	 * Cluster element index accessor method
	 * 
	 * @param cluster an integer representing index of cluster 
	 * @param itemColumn an integer representing the index column for an item 
	 * @return an integer representing the item of the specified cluster and column
	 */
	public int getCMatrixValue(int cluster, int itemColumn)
	{
		return this.clusterMatrix[cluster][itemColumn];
	}
	/**
	* Cluster element index Mutator method
	* 
	* @param cluster an integer representing index of cluster
	* @param itemColumn an integer representing the column index for an item
	* @param itemIndex an integer representing the item for the specified cluster and column
	*/
	public void setCMatrixValue(int cluster, int itemColumn, int itemIndex)
	{
		this.clusterMatrix[cluster][itemColumn] = itemIndex;
	}
	/**
	 * Cluster size accessor method
	 * 
	 * @param clusterIndex an integer representing index of cluster 
	 * @return an integer representing the size of the specified cluster
	 */
	public int getClusterSize(int clusterIndex)
	{
		return this.clusterSize[clusterIndex];
	}
   /**
	* Cluster size vector Accessor method
	* 
	* @return an integer vector representing clusters size
	*/      
	public int[] getClusterSize()
	{
		return clusterSize;
	}
   /**
	* Cluster size mutator method 
	* 
	* @param clusterIndex an integer representing index of a cluster 
	* @param clusterSize an integer representing size of cluster
	*/
	public void setClusterSize(int clusterIndex, int clusterSize) 
	{
		this.clusterSize[clusterIndex] = clusterSize;
	}
   /**
    * Cluster size vector mutator method
    * 
    * @param clusterSize an integer vector representing clusters size
    */
	public void setClusterSize(int clusterSize[])
	{
		for (int i = 0; i < this.getNOfCluster(); i++)
		{
			this.clusterSize[i] = clusterSize[i];
		}
	}
	/**
	* Number of clusters accessor method 
	* 
	* @return an integer representing number of clusters 
	*/
	public int getNOfCluster() 
	{
		return nOfCluster;
	}
	/**
	* Number of clusters mutator method 
	*  
	* @param nOfCluster an integer representing number of clusters
	*/
	public void setNOfCluster(int nOfCluster)
	{
		this.nOfCluster = nOfCluster;
	}
	
	/**
	 * Compact the cluster matrix deleting empty clusters in cluster solution
	 * 
	 * @return a Cluster_Matrix object
	 */
	public ClusterMatrix compactClusterMatrix()
	{
		int nonEmptyClusters=0;
		
		for(int i = 0; i<this.getNOfCluster(); i++)
		{			
			if(this.getClusterSize(i)!=0)nonEmptyClusters+=1;
		}
		
		ClusterMatrix cMatrix = new ClusterMatrix(nonEmptyClusters);
			
		for(int j = 0; j < this.getNOfCluster(); j++)
		{
			if(this.getClusterSize(j)!=0)
			{
				int k=0;
				while((cMatrix.getClusterSize(k)!=0) &	(k < nonEmptyClusters))
				{
					k++;
				}
				cMatrix.setClusterSize(k, this.getClusterSize(j));
				cMatrix.instanceCMatrixRow(k, cMatrix.getClusterSize(k));
				cMatrix.setCMatrixRow(k, this.getCMatrixRow(j));
			}
		}
		
	return cMatrix;	
	}
		
	/**
	 * Loads cluster matrix from a text file 
	 * 
	 * @param fName the name of the file to be loaded
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	public void loadFromFile(String fName) throws FileNotFoundException, IOException, NumberFormatException, Exception
	{
		int nOfCluster=0;
		String temp;
		File inputCMatrix = new File(fName);

		FileReader fCMR = new FileReader(inputCMatrix);
		BufferedReader bCMR = new BufferedReader(fCMR);
		temp = bCMR.readLine();
    	StringTokenizer stFirst = new StringTokenizer(temp);
    	
    	//Jumps string 'No of Cluster = ' in first line 
    	for (int i = 0; i < 4; i++)
        {
           stFirst.nextToken();
           
        }
    	    		
    	//Loads Number of clusters  
    	nOfCluster = Integer.parseInt(stFirst.nextToken()); 
    	
    	//Sets Number Of Clusters
    	this.setNOfCluster(nOfCluster);
    	
    	//Instanciates clusters size vector
    	this.clusterSize = new int[nOfCluster];
    	
    	//Instanciates cluster Matrix
    	this.clusterMatrix = new int[nOfCluster][];
    	
    	//Loads Cluster Matrix
    	for (int i = 0; i < this.getNOfCluster(); i++)
        {
    		temp = bCMR.readLine();
    		StringTokenizer stCMatrix = new StringTokenizer(temp);
    		this.setClusterSize(i, Integer.parseInt(stCMatrix.nextToken()));
    		this.instanceCMatrixRow(i, this.getClusterSize(i));
    		
    		for(int j = 0; stCMatrix.hasMoreTokens(); j++)
    		{
    			this.setCMatrixValue(i, j, Integer.parseInt(stCMatrix.nextToken()));
    		}
    	}
    	//End  loading cluster Matrix
    	bCMR.close();
    	fCMR.close();
    }
	
	/**
	 * 
	 * Loads Cluster matrix from a Spreadsheet excel file 
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
		
		//Instanciates clusters size vector
    	this.clusterSize = new int[this.getNOfCluster()];
    	
    	//Instanciates cluster Matrix
    	this.clusterMatrix = new int[this.getNOfCluster()][];
				
		//Reads clusters 
    	for (int i=0; i<this.getNOfCluster(); i++)
		{
			//Read Current Row
    		row = sheet.getRow(i+1);
			
    		//Read Cluster Size
			HSSFCell cellCluster = row.getCell((short)(0));
			this.setClusterSize(i, (int)cellCluster.getNumericCellValue());
			
			//Instanciates cluster column in cluster matrix
			this.instanceCMatrixRow(i, this.getClusterSize(i));
			
			//Read Cluster Index
			for(int j=0; j<this.getClusterSize(i); j++)
			{
				HSSFCell cellCIndex = row.getCell((short)(j+1));
				this.setCMatrixValue(i, j, (int)cellCIndex.getNumericCellValue());
			}
		}
	}
	
	/**
	 * Stores cluster matrix into a text file 
	 * 
	 * @param fName the name of the file to be stored
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */	
	public void storeToFile(String fName) throws FileNotFoundException, IOException, Exception
	{
		File outputCMatrix = new File(fName);
		
		FileWriter fCMW = new FileWriter(outputCMatrix);
		BufferedWriter bCMW = new BufferedWriter(fCMW);
		bCMW.write("No of Clusters = "+this.getNOfCluster());
		bCMW.newLine();
		for (int i = 0; i < this.getNOfCluster(); i++)
		{
			bCMW.write(this.getClusterSize(i)+"\t");
			for (int j = 0; j < this.getClusterSize(i); j++)
			{
				int intvalue = this.getCMatrixValue(i, j);
				String value = Integer.toString(intvalue);
				if (j == 0)
				{
					bCMW.write(value);
				}
				else
				{
					bCMW.write(" "+value);
				}
			}
			bCMW.newLine();
		}
		bCMW.close();
		fCMW.close();
	}
	
	
	/**
	 * Stores Cluster matrix into a spreadsheet excel file
	 * 
	 * @param fName the name of the excel file to be stored
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
			cellCluster.setCellValue(this.getClusterSize(i));
			
			//Write Cluster Index
			for(int j = 0; j<this.getClusterSize(i); j++)
			{
				cellCluster = row.createCell((short)(j+1));
				cellCluster.setCellValue(this.getCMatrixValue(i, j));
			}
		}
		
		out.flush();
		wb.write(out);
		out.close();
		
	}
	
}
