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
 * The SimilarityMatrix class provides an object for representing a
 * similarity matrix. 
 * 
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0
 */
public class SimilarityMatrix {
	private int numberOfRow;
	private String nameElement[];
	private double similarityMatrix[][];
	/**
	 * Default class constructor
	 *
	 */
	public SimilarityMatrix()
	{
		//Default class constructor
	}
	/**
	 * Class constructor specifying number of rows in similarity matrix
	 * 
	 * @param numberOfRow an integer representing number of rows 
	 */
	public SimilarityMatrix(int numberOfRow)
	{
		this.setNumberOfRow(numberOfRow);
		this.instanceSMatrix(numberOfRow);
	}
	/**
	 * Class constructor specifying an input data matrix
	 * 
	 * @param dataMatrix a reference to Data_Matrix object
	 */
	public SimilarityMatrix(DataMatrix dataMatrix)
	{
		this.setNumberOfRow(dataMatrix.getNOfItem());
		this.instanceSMatrix(dataMatrix.getNOfItem());
		this.fillSMatrix(dataMatrix);
	}
	/**
	 * Class contructor specifying cluster matrix and data matrix
	 * 
	 * @param clusterMatrix a reference to a Cluster_Matrix object
	 * @param patternMatrix a reference to a Data_Matrix object
	 */	
	public SimilarityMatrix(ClusterMatrix clusterMatrix, DataMatrix patternMatrix)
	{
		this.setNumberOfRow(clusterMatrix.getNOfCluster());
		this.instanceSMatrix(clusterMatrix.getNOfCluster());
		this.fillSMatrix(clusterMatrix, patternMatrix);
	}
	/**
	 * Fills similarity matrix with information data obtained from a Data_Matrix
	 * 
	 * @param dataMatrix a reference to a Data_Matrix object
	 */
	public void fillSMatrix(DataMatrix dataMatrix)
	{
		for(int i = 0; i < dataMatrix.getNOfItem(); i++)
		{
			this.setNameElement(i, dataMatrix.getItemName(i));
			for(int j = 0; j < dataMatrix.getNOfItem(); j++)
			{
				this.setValueSMatrix(i, j, this.euclideanDistance(dataMatrix.getItemValueRow(i), dataMatrix.getItemValueRow(j)));
			}
		}
	}
	/**
	 * Fills a similarity matrix with information data obtained from a Cluster_Matrix
	 * 
	 * @param clusterMatrix a reference to a Cluster_Matrix object
	 * @param dataMatrix a reference to a Data_Matrix object
	 */
	public void fillSMatrix(ClusterMatrix clusterMatrix, DataMatrix dataMatrix)
	{
		double centroid[][] = new double[clusterMatrix.getNOfCluster()][dataMatrix.getNOfFeature()];
	    
		for(int i = 0; i < clusterMatrix.getNOfCluster(); i++)
	    {
	    	for(int j = 0; j < clusterMatrix.getClusterSize(i); j++)
	    	{
	    		for(int k = 0; k < dataMatrix.getNOfFeature(); k++)
	    		{
	    			centroid[i][k] += dataMatrix.getItemValue(clusterMatrix.getCMatrixValue(i, j), k);
	    		}	    		
	    	}
	    	
	    	for (int k = 0; k < dataMatrix.getNOfFeature(); k++)	
	    	{
	    		centroid[i][k] += centroid[i][k] / clusterMatrix.getClusterSize(i);
	    	}
	    }
		
		for(int i = 0; i < clusterMatrix.getNOfCluster(); i++)
		{
			for(int j = 0; j < clusterMatrix.getNOfCluster(); j++)
			{
				this.setValueSMatrix(i, j, this.euclideanDistance(centroid[i], centroid[j]));
			}
		}
		
	}
	/**
	 * Fills a similarity matrix with information obtained from matrix of double
	 *
	 * @param value a matrix of double 
	 * @param numberOfRow an integer representing the number of rows in data matrix 
	 */
	public void fillSMatrix(double[][] value, int numberOfRow)
	{
		for(int i = 0; i < numberOfRow; i++)
		{
			for(int j = 0; j < numberOfRow; j++)
			{
				this.setValueSMatrix(i, j, this.euclideanDistance(value[i], value[j]));
			}
		}
	}
	/**
	 * Computes the euclidean distance between two items 
	 * 
	 * @param itemI a vector of double representing the first item
	 * @param itemJ a vector of double representing the second item
	 * @return a double value representing the euclidean distance between the two specified items
	 */
	public double euclideanDistance(double itemI[], double itemJ[])
	{
		double errorSum=0;
		
		for(int i = 0; i < itemI.length; i++)
		{
			errorSum += (double)Math.pow((double)(itemI[i]-itemJ[i]), 2);
		}
		return Math.sqrt(errorSum);
	}
	/**
	 * Finds maximum similarity value in similarity matrix
	 *
	 * @return a double value representing maximum similarity (minimum distance)
	 */
	public double maxSimilarity()
	{
		double temp, minValue;
		minValue = this.getValueSMatrix(0, 1);		
		for(int i = 1; i < this.getNumberOfRow(); i++)
		{
			for (int j=0; j < i; j++)
			{
				temp = this.getValueSMatrix(i, j);
				if(temp < minValue)minValue = temp;
			}
		}
		return minValue;
	}
	/**
	 * Finds index of row and column of the entries with maximum similarity
	 * 
	 * @return a vector of two integer containing row and column index
	 */
	public int[] maxSimilarityIndex()
	{
		int indexes[] = new int[2];
		double tempValue, minValue; 
		
		minValue = this.getValueSMatrix(0, 1);  
		indexes[0]= 0;							
		indexes[1]= 1;							
		
		for(int i = 0; i < this.getNumberOfRow(); i++)	
		{
			for (int j = i+1; j < this.getNumberOfRow(); j++)  
			{
				tempValue = this.getValueSMatrix(i, j);
				if(tempValue < minValue)
				{
					minValue = tempValue;
					indexes[0] = i;
					indexes[1] = j;
				}
			}
		}
		return indexes;
	}
	
	/**
	 * Finds minimum similarity value in similarity matrix
	 *
	 * @return a double value representing minimum similarity
	 */
	public double minSimilarity()
	{
		double temp, maxValue; 
		
		maxValue = this.getValueSMatrix(1, 0);
		for(int i = 1; i < this.getNumberOfRow(); i++)
		{
			for (int j = 0; j < (i+1); j++)
			{
				temp = this.getValueSMatrix(i, j);
				if(temp > maxValue)maxValue = temp; 
			}
		}
		return maxValue;
	}
	/**
	 * Finds index of row and column of the entries with minimum similarity
	 * 
	 * @return a vector of two integer containing row and column index
	 */
	public int[] minSimilarityIndex()
	{
		int indexes[] = new int[2];
		double tempValue, maxValue; 
		
		maxValue = this.getValueSMatrix(1, 0);
		indexes[0]= 1;
		indexes[1]= 0;
		
		for(int i = 1; i < this.getNumberOfRow(); i++)
		{
			for (int j = 0; j < i; j++)
			{
				tempValue = this.getValueSMatrix(i, j);
				if(tempValue > maxValue)
				{
					maxValue = tempValue;
					indexes[0] = i;
					indexes[1] = j;
				}
			}
		}
		return indexes;
	}
	
	/**
	 * Instanciates similarity matrix 
	 * 
	 * @param numberOfRow an integer representing number of rows in similarity matrix
	 */	
	private void instanceSMatrix(int numberOfRow)
	{
		this.nameElement = new String[numberOfRow];
		this.similarityMatrix = new double[numberOfRow][numberOfRow];
	}
	
	/**
	 * Number of rows accessor method
	 * 
	 * @return an integer representing the number of rows in similarity matrix
	 */
	public int getNumberOfRow() {
		return numberOfRow;
	}
	/**
	 * Number of rows mutator method 
	 * 
	 * @param numberOfRow an integer representing the number of rows in similarity matrix
	 */
	public void setNumberOfRow(int numberOfRow) {
		this.numberOfRow = numberOfRow;
	}
	/**
	 * Element name accessor method
	 * 
	 * @param row an integer representing index in similarity matrix
	 * @return a String representing the name of the specified element
	 */
	public String getNameElement(int row){
		return this.nameElement[row];
	}
	/**
	 * Element name mutator method
	 * 
	 * @param row an integer representing row index 
	 * @param name a String representing the name of the specified element
	 */
	public void setNameElement(int row, String name){
		this.nameElement[row] = name;
	}
	/**
	 * Element name vector accessor method 
	 * 
	 * @return a vector of Strings representing element names
	 */
	public String[] getNameElement() {
		return nameElement;
	}
	/**
	 * Element name vector mutator method
	 * 
	 * @param nameElement a vector of Strings representing element names
	 */
	public void setNameElement(String[] nameElement) {
		this.nameElement = nameElement;
	}
	/**
	 * Similarity matrix entry value mutator method
	 * 
	 * @param row an integer representing index of row in similarity matrix
	 * @param column an integer representing index of column in similarity matrix
	 * @param value a double value representing the distance between the elements
	 */
	public void setValueSMatrix(int row, int column, double value)
	{
		similarityMatrix[row][column] = value;
	}
	/**
	 * Similarity matrix row mutator method
	 * 
	 * @param row an integer representing index of row in similarity matrix
	 * @param rowValues a vector of double values representing the distance between the row element
	 * and each other element.
	 */
	public void setValueSMatrix(int row, double rowValues[]){
		this.similarityMatrix[row]=rowValues;
	}
	/**
	 * Similarity matrix entry value accessor method 
	 * 
	 * @param row an integer representing index of row in similarity matrix
	 * @param column an integer representing index of column in similarity matrix
	 * @return a double value representing the distance value between two elements
	 */
	public double getValueSMatrix(int row, int column){
		return this.similarityMatrix[row][column];
	}
	/**
	 * Similarity matrix vector accessor method
	 * 
	 * @param row an integer representing index of row in similarity matrix
	 * @return a vector of double containing similariity matrix values
	 */
	public double[] getValueSMatrix(int row){
		return this.similarityMatrix[row];
	}
	/**
	 * Loads similarity matrix from a text file
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
		File inputSMatrix = new File(fName);
		
		FileReader inputStream = new FileReader(inputSMatrix);
		BufferedReader bInputR = new BufferedReader(inputStream);
		  
		//Reads the first line of the file and sets the number of entries
		temp = bInputR.readLine();
		StringTokenizer sFirst = new StringTokenizer(temp);
		
		for (int i = 0; i <= 3; i++)
		{
			sFirst.nextToken();
		}
		//End to read First Line
		//Reads number of row
		this.setNumberOfRow(Integer.parseInt((sFirst.nextToken())));
		this.instanceSMatrix(this.getNumberOfRow());
		//Reads and loads SMatrix Values
		for (int i = 0; i < this.getNumberOfRow(); i++)
		{
			temp = bInputR.readLine();
			StringTokenizer stMatrix = new StringTokenizer(temp);
			this.setNameElement(i, stMatrix.nextToken());
			
			for (int j = 0; j < this.getNumberOfRow(); j++)
			{
				this.setValueSMatrix(i, j, Float.parseFloat(stMatrix.nextToken()));
			}
		}
		bInputR.close();
		inputStream.close();
	}
	/**
	 * 
	 * Loads data matrix from a first sheet of excel file 
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
		HSSFRow row;
		row= sheet.getRow(0);
		HSSFCell cellr = row.getCell((short)2);
		this.setNumberOfRow(((int) cellr.getNumericCellValue()));
		
		//Instance Similarity Matrix
		this.instanceSMatrix(this.getNumberOfRow());
				
		//Reads and loads SMatrix Values
		for (int i = 0; i < this.getNumberOfRow(); i++)
		{
			row = sheet.getRow(i+1);
			HSSFCell cellf = row.getCell((short)0);
			this.setNameElement(i, cellf.getStringCellValue());
			
			for (int j = 0; j < this.getNumberOfRow(); j++)
			{
				cellf = row.getCell((short)(j+1));
				this.setValueSMatrix(i, j, cellf.getNumericCellValue());
			}
		}
	}
	/**
	 * Stores similarity matrix into a text file 
	 * 
	 * @param fName the name of the file to be stored
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */	
	public void storeToFile(String fName) throws FileNotFoundException, IOException, Exception
	{
		File outputSMatrix = new File(fName);
		
		FileWriter stInputW = new FileWriter(outputSMatrix);
		BufferedWriter bwInput = new BufferedWriter(stInputW);
		bwInput.write("Number of Row = "+this.getNumberOfRow());
		bwInput.newLine();
		
		for (int i = 0; i < this.getNumberOfRow(); i++)
		{
			bwInput.write(this.getNameElement(i)+" ");
			for (int j = 0; j < this.getNumberOfRow(); j++)
			{
				bwInput.write(this.getValueSMatrix(i, j)+" ");
			}
			bwInput.newLine();
		}
		bwInput.close();
		stInputW.close();
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
		cellr.setCellValue("Number of Row");
		cellr = row.createCell((short)1);
		cellr.setCellValue("=");
		cellr = row.createCell((short)2);
		cellr.setCellValue(this.getNumberOfRow());
		
		//Write successive row
		for(int i = 0; i<this.getNumberOfRow(); i++)
		{
			//Write Cluster size
			row = sheet.createRow((short)(i+1));
			HSSFCell cellCluster = row.createCell((short)0);
			cellCluster.setCellValue(this.getNameElement(i));
			
			//Write Cluster Index
			for(int j = 0; j<this.getNumberOfRow(); j++)
			{
				cellCluster = row.createCell((short)(j+1));
				cellCluster.setCellValue(this.getValueSMatrix(i, j));
			}
		}
		out.flush();
		wb.write(out);
		out.close();
	}
	
}
