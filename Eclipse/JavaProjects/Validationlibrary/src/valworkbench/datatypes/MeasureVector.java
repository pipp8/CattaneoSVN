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
 * The MeasureVector class provides an object containing method 
 * and information concerning measure values.
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0
 */
public class MeasureVector {
	private int nOfEntries;
	private int nOfCluster[];
	private double measureValue[];
	/**
	 * Default constructor method
	 *
	 */
	public MeasureVector()
	{
		//Default constructor method
	}
	/**
	 * Class constructor specifying number of entries 
	 * in measure vector
	 * 
	 * @param nOfEntries the number of entries in measure vector
	 */
	public MeasureVector(int nOfEntries)   
	{
		this.setNOfEntries(nOfEntries);
		this.nOfCluster = new int[nOfEntries];
		this.measureValue = new double[nOfEntries];
	}
	/**
	 * Class constructor specifying the number of entries, a vector containing number of clusters
	 * and a vector containing measure values
	 * 
	 * @param nOfEntries the number of entries in measure vector 
	 * @param nOfCluster a vector of integer containing number of cluster
	 * @param measureValue a vector of double containing measure values
	 */
	public MeasureVector(int nOfEntries, int[] nOfCluster, double[] measureValue)
	{
		this.setNOfEntries(nOfEntries);
		this.nOfCluster = new int[nOfEntries];
		this.measureValue = new double[nOfEntries];
		this.setNOfCluster(nOfCluster);
		this.setMeasureValue(measureValue);
	}
	/**
	 * Number of entries in measure vector mutator method
	 * 
	 * @param nOfEntries an integer representing number of entries 
	 */
	public void setNOfEntries(int nOfEntries)
	{
		this.nOfEntries = nOfEntries;
	}
	/**
	 * Number of entries in measure vector accessor method
	 * 
	 * @return an integer representing measure vector
	 */
	public int getNOfEntries()
	{
		return this.nOfEntries;
	}
	/**
	 * Number of clusters mutator method
	 * 
	 * @param indexEntry an integer representing entry index in measure vector
	 * @param nOfCluster an integer representing the number of clusters
	 */
	public void setNOfCluster(int indexEntry, int nOfCluster)
	{
		this.nOfCluster[indexEntry] = nOfCluster;
	} 
	/**
	 * Number of cluster accessor method
	 * 
	 * @param indexEntry an integer representng entry index in measure vector
	 * @return an integer representing number of clusters for the specified entry
	 */
	public int getNOfCluster(int indexEntry)
	{
		return this.nOfCluster[indexEntry];
	}
	/**
	 * Measure value mutator method 
	 * 
	 * @param indexEntry an integer representing entry index in measure vector
	 * @param measureValue a double representing measure value for the specified entry
	 */
	public void setMeasureValue(int indexEntry, double measureValue)
	{
		this.measureValue[indexEntry] = measureValue;
	}
	/**
	 * Measure value accessor method
	 * 
	 * @param indexEntry an integer representing entry index in measure vector
	 * @return a double representing measure value for the specified entry
	 */
	public double getMeasureValue(int indexEntry)
	{
		return this.measureValue[indexEntry];
	}
	/**
	 * Number of clusters vector accessor method
	 * 
	 * @return a vector of integer containing number of clusters
	 */
	public int[] getNOfCluster()
	{
		int vector[] = new int[this.getSizeMVector()];
		for (int i = 0; i < this.getSizeMVector(); i++)
		{
			vector[i] = this.getNOfCluster(i);
		}
		return vector;
	}
	/**
	 * Number of clusters vector mutator method
	 * 
	 * @param nOfCluster a vector of integer containing number of clusters
	 */
	public void setNOfCluster(int nOfCluster[])
	{
		for (int i = 0; i < this.getSizeMVector(); i++)
		{
			this.setNOfCluster(i, nOfCluster[i]);
		}
	}
	/**
	 * Measure value vector accessor method
	 * 
	 * @return a vector of double containing measure values
	 */
	public double[] getMeasureValue()
	{
		double vector[] = new double[this.getSizeMVector()];
		for (int i = 0; i < this.getSizeMVector(); i++)
		{
			vector[i] = this.getMeasureValue(i);
		}
		return vector;
	}
	/**
	 * Measure value vector mutator method
	 * 
	 * @param measureValue a vector of double containing measure values
	 */
	public void setMeasureValue(double measureValue[])
	{
		for (int i = 0; i < this.getSizeMVector(); i++)
		{
			this.setMeasureValue(i, measureValue[i]);
		}
	}
	/**
	 * Measure vector size accesor method
	 * 
	 * @return an integer representing measure vector size
	 */
	public int getSizeMVector()
	{
		return this.measureValue.length;
	}
	/**
	 * Maximum measure value accessor method
	 * 
	 * @return the maximum measure value in the measure vector
	 */
	public double getMaxMeasureValue()
	{
		double max = this.getMeasureValue(0);
		double temp;
		for(int i = 1; i < this.getNOfEntries(); i++)
		{
			temp = this.getMeasureValue(i);
			if(temp > max)
				max = temp;
		}
		return max;
	}
	/**
	 * 
	 * @return
	 */
	public int getMaxMeasureValueNOfCluster()
	{
		int relativeMax = this.getNOfCluster(0);
		double max = this.getMeasureValue(0);
		int nOfClustTemp;
		double temp;
		
		for(int i = 1; i < this.getNOfEntries(); i++)
		{
			nOfClustTemp = this.getNOfCluster(i);
			temp = this.getMeasureValue(i);
			if(temp > max)
			{
				max = temp;
				relativeMax = nOfClustTemp;
			}
		}
		return relativeMax;
	}
	/**
	 * Minimum measure value accessor method
	 * 
	 * @return the minimum measure value in the measure vector
	 */
	public double getMinMeasureValue()
	{
		double min = this.getMeasureValue(0);
		double temp;
		for(int i = 1; i < this.getNOfEntries(); i++)
		{
			temp = this.getMeasureValue(i);
			if(temp < min)
				min = temp;
		}
		return min;
	}
	/**
	 * 
	 * @return
	 */
	public int getMinMeasureValueNOfCluster()
	{
		int relativeMin = this.getNOfCluster(0);
		double min = this.getMeasureValue(0);
		int nOfClustTemp;
		double temp;
		
		for(int i = 1; i < this.getNOfEntries(); i++)
		{
			nOfClustTemp = this.getNOfCluster(i);
			temp = this.getMeasureValue(i);
			if(temp < min)
			{
				min = temp;
				relativeMin = nOfClustTemp;
			}
		}
		return relativeMin;
	}
	/**
	 * 
	 * Loads measure vector from a text file
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
		File inputMeasFile = new File(fName);
		
		FileReader fMR = new FileReader(inputMeasFile);
		BufferedReader bMR = new BufferedReader(fMR);
			
		temp = bMR.readLine();
		StringTokenizer stNIT = new StringTokenizer(temp);
		//Jump string 'No of Iterations =' in first line
		for (int i = 0; i <= 3; i++)
		{
			stNIT.nextToken();
		}
		this.setNOfEntries(Integer.parseInt(stNIT.nextToken()));
		this.nOfCluster = new int[this.getNOfEntries()];
		this.measureValue = new double[this.getNOfEntries()];
		
		for (int i = 0; i < this.getNOfEntries(); i++)
		{
			temp = bMR.readLine();
			StringTokenizer stMeas = new StringTokenizer(temp);
			stMeas.nextToken();
			this.nOfCluster[i] = Integer.parseInt(stMeas.nextToken());
			stMeas.nextToken();
			this.measureValue[i] =  Double.parseDouble(stMeas.nextToken());
		}
		bMR.close();
		fMR.close();
	}
	
	/**
	 * 
	 * Loads Measure Vector from a Spreadsheet excel file 
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

		//Reads Number of Iteration
		HSSFRow row;
		row = sheet.getRow(0);
		HSSFCell cellr = row.getCell((short)2);
		this.setNOfEntries((int) cellr.getNumericCellValue());
		
		//Instanciates clusters size vector
    	this.nOfCluster = new int[this.getNOfEntries()];
		this.measureValue = new double[this.getNOfEntries()];
    	
    	//Reads clusters 
    	for (int i=0; i<this.getNOfEntries(); i++)
		{
			//Read Current Row
    		row = sheet.getRow(i+1);
			
    		//Read Number of cluster in Iteration
			HSSFCell cellCluster = row.getCell((short)(1));
			this.setNOfCluster(i, (int)cellCluster.getNumericCellValue());
			
			//Read Measure value for cluster i
			HSSFCell cellCValue = row.getCell((short)(3));
			this.setMeasureValue(i, cellCValue.getNumericCellValue());
		}
	}
		
	/**
	 * Stores measure vector into a text file 
	 * 
	 * @param fName the name of the file to be stored
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */	
	public void storeToFile(String fName) throws FileNotFoundException, IOException, Exception
	{
		File outMeasFile = new File(fName);

		FileWriter fMW = new FileWriter(outMeasFile);
		BufferedWriter bMeasW = new BufferedWriter(fMW);
		bMeasW.write("No of Iterations = "+this.getNOfEntries());
		bMeasW.newLine();
		for(int i = 0; i < this.getSizeMVector(); i++)
		{  
			bMeasW.write("Clusters "+this.getNOfCluster(i)+" :\t"+this.getMeasureValue(i));
			bMeasW.newLine();	 
		}
		bMeasW.close();	 
		fMW.close();			
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
		cellr.setCellValue("Number of Iterations");
		cellr = row.createCell((short)1);
		cellr.setCellValue("=");
		cellr = row.createCell((short)2);
		cellr.setCellValue(this.getNOfEntries());
		
		//Write successive row
		for(int i = 0; i<this.getNOfEntries(); i++)
		{
			row = sheet.createRow((short)(i+1));
			HSSFCell cellMeasure = row.createCell((short)0);
			cellMeasure.setCellValue("Clusters");
			cellMeasure = row.createCell((short)1);
			//Write Number of cluster
			cellMeasure.setCellValue(this.getNOfCluster(i));
			cellMeasure = row.createCell((short)2);
			cellMeasure.setCellValue(":");
			cellMeasure = row.createCell((short)3);
			//Write Measure Value
			cellMeasure.setCellValue(this.getMeasureValue(i));
		}
		
		out.flush();
		wb.write(out);
		out.close();
	}
	
}
