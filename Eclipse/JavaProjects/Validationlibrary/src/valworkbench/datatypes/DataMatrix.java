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
 * The DataMatrix class provides an object representing Data Matrix.
 * A Data_Matrix object contains a Matrix with the following fields: 'nOfItems'
 * (an integer value representing the number of Items in data matrix),
 * 'nOfFeatures' (an integer value representing the number of features (columns) in
 * data matrix), 'itemName' (a vector containing the items name),
 * 'itemDescription' (a vector containing description of specified items in the 
 * corresponding entry of vector 'itemName'), 'featureName' (a vector containing the 
 * features name), 'itemValue' (a double matrix containing the items double value).
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0
 */ 
public class DataMatrix {
	private int nOfItems, nOfFeatures; 
	private String itemName[];
	private String itemDescription[];
	private String featureName[];      
	private double itemValue[][];	   
	/**
	 * Default class constructor 
	 * 
	 */
	public DataMatrix()   
	{
		//Default constructor method
	}
	
	/**
	 * Class constructor specifying number of Items 
	 * and number of conditions
	 *
	 * @param nOfItems an integer representing number of items in data matrix 
	 * @param nOfFeatures an integer representing number of features (conditions) in data matrix
	 */
	public DataMatrix(int nOfItems, int nOfFeatures)   
	{
		this.nOfItems = nOfItems;
		this.nOfFeatures = nOfFeatures;
		this.itemName = new String[nOfItems];
		this.itemDescription = new String[nOfItems];
		this.featureName = new String[nOfFeatures];
		this.itemValue = new double[nOfItems][nOfFeatures];
	}
	/**
	 * Item name accessor method
	 * 
	 * @param itemIndex an integer representing index of item name required 
	 * @return a String representing the item name with specified index
	 */
	public String getItemName(int itemIndex)     
	{
		return itemName[itemIndex];
	}
	/**
	 * Item Name Mutator method
	 * 
	 * @param itemIndex an integer representing index of item name to be modified
	 * @param itemName a String representing the item name
	 */
	public void setItemName(int itemIndex, String itemName)    
	{
		this.itemName[itemIndex] = itemName;
	}
	/**
	 * Item description accessor method 
	 * 
	 * @param itemIndex an integer representing index of item descritpion required
	 * @return a String representing the item description
	 */
	public String getItemDescription(int itemIndex)
	{
		return itemDescription[itemIndex];
	}
	/**
	 * Item description mutator method
	 * 
	 * @param itemIndex an integer representing index of item description to be modified
	 * @param itemDescription a String representing the item description
	 */
	public void  setItemDescription(int itemIndex, String itemDescription)
	{
		this.itemDescription[itemIndex] = itemDescription;
	}
	/**
	 * Feature name accessor method
	 * 
	 * @param featIndex an integer representing index of feature name 
	 * @return a String representing the feature name
	 */
	public String getFeatureName(int featIndex)   
	{
		return featureName[featIndex];
	}
	/**
	 * feature name mutator method
	 * 
	 * @param featIndex an integer representing index of feature name to be modified
	 * @param featureName a String representing the feature name
	 */
	public void setFeatureName(int featIndex, String featureName)    
	{
		this.featureName[featIndex] = featureName;
	}
	/**
	 * Item Value accessor method
	 * 
	 * @param itemIndex an integer representing item row index
	 * @param featIndex an integer representing item column index
	 * @return a double value corresponding to item value of selected item
	 */
	public double getItemValue(int itemIndex, int featIndex) 
	{
		return this.itemValue[itemIndex][featIndex];
	}
	/**
	 * Item value mutator method
	 * 
	 * @param itemIndex an integer representing item index
	 * @param featIndex an integer representing feature index
	 * @param itemValue a double representing item value 
	 */
	public void setItemValue(int itemIndex, int featIndex, double itemValue)  
	{
		this.itemValue[itemIndex][featIndex] = itemValue;
	}
	/**
	 * Item Name vector mutator method 
	 * 
	 * @param itemName a String vector representing item names 
	 */
	public void setItemName(String itemName[])        
	{
		for (int i = 0; i < this.getNOfItem(); i++)
		{
			this.setItemName(i, itemName[i]);
		}
	}
	/**
	 * Item Name vector accessor method
	 * 
	 * @return a String vector representing item names
	 */
	public String[] getItemName()            
	{
		String vectorItemName[] = new String[this.getNOfItem()];
		for (int i = 0; i < this.getNOfItem(); i++)
		{
			vectorItemName[i] = this.getItemName(i);
		}
		return vectorItemName;
	}
	/**
	 * Item Description vector Mutator method
	 * 
	 * @param itemDescription a String vector representing item descriptions
	 */
	public void setItemDescription(String itemDescription[])
	{
		for (int i = 0; i < this.getNOfItem(); i++)
		{
			this.setItemDescription(i, itemDescription[i]);
		}
	}
	/**
	 * Item Description vector accessor method
	 * 
	 * @return a String vector representing item descriptions
	 */
	public String[] getItemDescription()
	{
		String vectorItemDescription[] = new String[this.getNOfItem()];
		for (int i = 0; i < this.getNOfItem(); i++)
		{
			vectorItemDescription[i] = this.getItemDescription(i);
		}
		return vectorItemDescription;
	}
	/**
	 * Feature Name vector Mutator method 
	 * 
	 * @param featureName a String vector representing feature names
	 */
	public void setFeatureName(String featureName[])     
	{
		for (int j = 0; j < this.getNOfFeature(); j++)
		{
			this.setFeatureName(j, featureName[j]);
		}
	}
	/**
	 * Feature Name vector accessor method
	 * 
	 * @return a vector of String representing feature name 
	 */
	public String[] getFeatureName()     
	{
		String vectorfName[] = new String[this.getNOfFeature()];
		for (int j = 0; j < this.getNOfFeature(); j++)
		{
			vectorfName[j] = this.getFeatureName(j);
		}
		return vectorfName;
	}
	/**
	 * item values row vector Mutator method
	 * 
	 * @param itemIndex an integer representing index of item 
	 * @param itemVectorRow a double vector representing item values
	 */	
	public void setItemValueRow(int itemIndex, double itemVectorRow[])  
	{
		for(int j = 0; j < this.getNOfFeature(); j++ )
		{
			this.itemValue[itemIndex][j] = itemVectorRow[j]; 
		}
	}
	/**
	 * Item values row vector accessor method
	 * 
	 * @param itemIndex an integer representing index of item 
	 * @return a double vector representing item values for specified row index
	 */	
	public double[] getItemValueRow(int itemIndex) 
	{
		double itemVectorRow[] = new double[this.getNOfFeature()];
		for (int j= 0; j < this.getNOfFeature(); j++)
		{
			itemVectorRow[j] = this.itemValue[itemIndex][j]; 
		}
		return itemVectorRow;
	}
	/**
	 * Item values column vector Mutator method 
	 * 
	 * @param featIndex an integer representing index of feature
	 * @param itemVectorColumn a double vector representing feature values for specified column index 
	 */	
	public void setItemValueColumn(int featIndex, double itemVectorColumn[]) 
	{
		for(int i = 0; i < this.getNOfItem(); i++)
		{
			this.itemValue[i][featIndex] = itemVectorColumn[i];
		}
	}
	/**
	 * Item values column vector accessor method
	 * 
	 * @param featIndex an integer representing index of feature 
	 * @return a double vector representing item values for specified column index
	 */	
	public double[] getItemValueColumn(int featIndex)  
	{
		double itemVectorColumn[] = new double[this.getNOfItem()];
		for (int i = 0; i < this.getNOfItem(); i++)
		{
			itemVectorColumn[i] = this.itemValue[i][featIndex]; 
		}
		return itemVectorColumn;
	}
	/**
	 * Item values matrix mutator method 
	 *  
	 * @param itemValue a double matrix representing item values
	 */
	public void setItemValue(double[][] itemValue)
	{
		for (int i = 0; i < this.getNOfItem(); i++)
		{
			for (int j = 0; j < this.getNOfFeature(); j++)
			{
				this.itemValue[i][j] = itemValue[i][j];
			}
		}
	}
	/**
	 * Item values matrix accessor method 
	 * 
	 * @return a double matrix containing item values
	 */
	public double[][] getItemValue()
	{
		double itemValue[][] = new double[this.getNOfItem()][this.getNOfFeature()]; 
		for (int i = 0; i < this.getNOfItem(); i++)
		{
			for (int j = 0; j < this.getNOfFeature(); j++)
			{
				itemValue[i][j] = this.itemValue[i][j];
			}
		}
		return itemValue;
	}
	/**
	 * Copies the data matrix in a new object of the same type 
	 * 
	 * @return a reference to a new Data_Matrix object equal to this  
	 */
	public DataMatrix copyDMatrix()
	{
		DataMatrix dataMatrix = new DataMatrix(this.getNOfItem(), this.getNOfFeature());
		dataMatrix.setItemName(this.getItemName());
		dataMatrix.setItemDescription(this.getItemDescription());
		dataMatrix.setFeatureName(this.getFeatureName());
		dataMatrix.setItemValue(this.getItemValue());
		return dataMatrix;
	}
	/**
	 * Selects m items from the data matrix and returns a new Data matrix
	 * object containing only the m items selected
	 * 
	 * @param m number of items selected from data matrix
	 * @return a Data_Matrix object with m items 
	 */
	public DataMatrix selectFirstMItem(int m)
	{
		DataMatrix dataMatrix = new DataMatrix(m, this.getNOfFeature());

		dataMatrix.setFeatureName(this.getFeatureName());
		for (int i=0;i<m;i++)
		{
			dataMatrix.setItemName(i, this.getItemName(i));
			dataMatrix.setItemDescription(i, this.getItemDescription(i));
			dataMatrix.setItemValueRow(i,this.getItemValueRow(i));
		}
		return dataMatrix;
	}
	
	/**
	 * Selects last m items from the data matrix and returns a new Data matrix
	 * object containing only the m items selected
	 * 
	 * @param m number of items selected from data matrix
	 * @return a Data_Matrix object with m items 
	 */
	public DataMatrix selectLastMItem(int m)
	{
		DataMatrix dataMatrix = new DataMatrix(m, this.getNOfFeature());

		dataMatrix.setFeatureName(this.getFeatureName());
		for (int i=0;i<m; i++)
			
		{
			dataMatrix.setItemName(i, this.getItemName(i+this.nOfItems-m));
			dataMatrix.setItemDescription(i, this.getItemDescription(i+this.nOfItems-m));
			dataMatrix.setItemValueRow(i,this.getItemValueRow(i+this.nOfItems-m));
		}
		return dataMatrix;
	}
	
	
	
	/**
	 * Swaps two row vectors in a data matrix
	 * 
	 * @param iItem first row to swap
	 * @param jItem second row to swap
	 */	
	public void swapItems(int iItem, int jItem)
	{
		//Swaps Item Names
		String itemName;
		itemName = this.getItemName(iItem);
		this.setItemName(iItem, this.getItemName(jItem));
		this.setItemName(jItem, itemName);
		//Swaps Item Descriptions
		String itemDescription;
		itemDescription = this.getItemDescription(iItem);
		this.setItemDescription(iItem, this.getItemDescription(jItem));
		this.setItemDescription(jItem, itemDescription);
		//Swaps vector values  
		double itemVector[];
		itemVector = this.getItemValueRow(iItem);
		this.setItemValueRow(iItem, this.getItemValueRow(jItem));
		this.setItemValueRow(jItem, itemVector);
	}
	/**
	 * Swaps two vectors column in a data matrix
	 *  
	 * @param iFeatIndex first column to swap
	 * @param jFeatIndex second column to swap
	 */
	public void swapFeatures(int iFeatIndex, int jFeatIndex)
	{
		//Swaps feature names
		String featureName;
		featureName = this.getFeatureName(iFeatIndex);
		this.setFeatureName(iFeatIndex, this.getFeatureName(jFeatIndex));
		this.setFeatureName(jFeatIndex, featureName);
		//Swaps vector values
		double itemVector[];
		itemVector = this.getItemValueColumn(iFeatIndex);
		this.setItemValueColumn(iFeatIndex, this.getItemValueColumn(jFeatIndex));
		this.setItemValueColumn(jFeatIndex, itemVector);
	}
	/**
	 * Removes a row with index 'g' from data Matrix
	 * 
	 * @param g index of removed row
	 * @return a new Data_Matrix object without the specified row 
	 */
	public DataMatrix removeItem(int g)
	{
		DataMatrix dataMatrix = new DataMatrix((this.getNOfItem()-1), this.getNOfFeature());
		dataMatrix.setFeatureName(this.getFeatureName());

		if (g != 0)
		{
			//Copies Pattern Matrix by row Before Item g
			for (int i = 0; i < g; i++)
			{
				dataMatrix.setItemName(i, this.getItemName(i));
				dataMatrix.setItemDescription(i, this.getItemDescription(i));
				dataMatrix.setItemValueRow(i, this.getItemValueRow(i));
			}

		}

		if (g != (this.getNOfItem()-1))
		{
			//Copies Pattern Matrix by row After Item g
			for (int i = (g+1); i < this.getNOfItem(); i++)
			{
				dataMatrix.setItemName((i-1), this.getItemName(i));
				dataMatrix.setItemDescription((i-1), this.getItemDescription(i));
				dataMatrix.setItemValueRow((i-1), this.getItemValueRow(i));
			}
		}
		return dataMatrix;
	}
	/**
	 * Removes a column (feature) with index 'e' from Data Matrix
	 * 
	 * @param e index of removed column
	 * @return a new Data_Matrix object without the specified column
	 */
	public DataMatrix removeFeature(int e)
	{
		DataMatrix dataMatrix = new DataMatrix(this.getNOfItem(), (this.getNOfFeature()-1));

		dataMatrix.setItemName(this.getItemName());
		dataMatrix.setItemDescription(this.getItemDescription());
		if (e != 0)
		{
			//Copies Pattern Matrix by column Before Feature e
			for (int j = 0; j < e; j++)
			{
				dataMatrix.setFeatureName(j, this.getFeatureName(j));
				dataMatrix.setItemValueColumn(j, this.getItemValueColumn(j));
			}
		}

		if (e != (this.getNOfFeature()-1))
		{
			//Copies Pattern Matrix by column After Feature e
			for (int j = (e+1); j < this.getNOfFeature() ; j++)
			{
				dataMatrix.setFeatureName((j-1), this.getFeatureName(j));
				dataMatrix.setItemValueColumn((j-1), this.getItemValueColumn(j));
			}
		}
		return dataMatrix;
	}
	
	/**
	 * Extract a column (feature) with index 'e' from Data Matrix
	 * 
	 * @param e index of extracted column
	 * @return a vector of the specified column
	 */
	public double[] extractFeature(int e)
	{
		double[] feature= new double[this.getNOfItem()];
		
		for(int i=0; i<this.getNOfItem(); i++)
			feature[i]=this.getItemValue(i, e);
		
		return feature;
	}
	
	/**
	 * Extract a  row (gene) with index 'e' from Data Matrix
	 * 
	 * @param e index of extracted row
	 * @return a vector of the specified row
	 */
	public double[] extractRow(int e)
	{
		double[] r= new double[this.getNOfFeature()];
		
		for(int i=0; i<this.getNOfFeature(); i++)
			r[i]=this.getItemValue(e, i);
		
		return r;
	}

	/**
	 * Extracts minimum value in row rowIndex
	 * 
	 * @param rowIndex the specified row index
	 * @return a double value representing the minimum value in a row
	 */
	public double extractRowMin(int rowIndex)
	{
		double rowMin = this.getItemValue(rowIndex, 0);
		for(int i = 1; i < this.getNOfFeature(); i++)
		{
			if(rowMin > this.getItemValue(rowIndex, i))
				rowMin = this.getItemValue(rowIndex, i);
		} 
		return rowMin;  
	}

	/**
	 * Extracts maximum value in row rowIndex
	 * 
	 * @param rowIndex the specified row index
	 * @return a double value representing the maximum value in a row
	 */
	public double extractRowMax(int rowIndex)
	{
		double rowMax = this.getItemValue(rowIndex, 0);
		for(int i = 1; i < this.getNOfFeature(); i++)
		{
			if(rowMax < this.getItemValue(rowIndex, i))
				rowMax = this.getItemValue(rowIndex, i);
		}
		return rowMax;
	}

	/**
	 * Extracts minimum value in column columnIndex
	 * 
	 * @param columnIndex the specified column index
	 * @return a double value representing the minimum value in a column
	 */
	public double extractColumnMin(int columnIndex)
	{
		double columnMin = this.getItemValue(0, columnIndex);
		for(int i = 1; i < this.getNOfItem(); i++)
		{
			if(columnMin > this.getItemValue(i, columnIndex))
				columnMin = this.getItemValue(i, columnIndex);
		}
		return columnMin;
	}

	/**
	 * Extracts maximum value in column columnIndex
	 * 
	 * @param columnIndex the specified column index
	 * @return a double value representing the maximum value in a column
	 */
	public double extractColumnMax(int columnIndex)
	{
		double columnMax = this.getItemValue(0, columnIndex);
		for(int i = 1; i < this.getNOfItem(); i++)
		{
			if(columnMax < this.getItemValue(i, columnIndex))
				columnMax = this.getItemValue(i, columnIndex);
		}
		return columnMax;
	}

	/**
	 * Number of feature accessor method
	 * 
	 * @return an integer representing the number of columns in a Data Matrix 
	 */     		
	public int getNOfFeature() 
	{
		return this.nOfFeatures; 
	}
	/**
	 * Number of items accessor method 
	 * 
	 * @return an integer representing the number of items in a data matrix 
	 */
	public int getNOfItem() 
	{
		return this.nOfItems;
	}
	/**
	 * Loads Data Matrix from a text file
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
		File inputDMatrix = new File(fName);

		FileReader inputStream = new FileReader(inputDMatrix);
		BufferedReader bInputR = new BufferedReader(inputStream);

		//Reads the first line of the file and sets the number of items and the number of Features
		temp = bInputR.readLine();
		StringTokenizer sFirst = new StringTokenizer(temp);
		this.nOfItems = Integer.parseInt(sFirst.nextToken());
		this.nOfFeatures = Integer.parseInt(sFirst.nextToken());
		this.itemName = new String[this.nOfItems];
		this.itemDescription = new String[this.nOfItems];
		this.featureName = new String[this.nOfFeatures];
		this.itemValue = new double[this.nOfItems][this.nOfFeatures];
		//Ends to read First Line

		temp = bInputR.readLine();
		StringTokenizer stCHeader = new StringTokenizer(temp);
		stCHeader.nextToken();//jump header of gene name column
		stCHeader.nextToken();//jump header of gene description column

		//reads second line in file and sets column header (i.e. Feature Name)
		for (int i = 0; i < this.getNOfFeature(); i++) 
		{
			this.setFeatureName(i, stCHeader.nextToken());
		}
		//reads Item Name, Item Description and Matrix Value
		for (int i = 0; i < this.getNOfItem(); i++)
		{
			temp = bInputR.readLine();
			StringTokenizer stMatrix = new StringTokenizer(temp);
			this.setItemName(i, stMatrix.nextToken());
			this.setItemDescription(i, stMatrix.nextToken());
			for (int j = 0; j < nOfFeatures; j++)
			{
				this.setItemValue(i, j, Double.parseDouble(stMatrix.nextToken()));
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
		HSSFCell cellr = row.getCell((short)0);
		this.nOfItems=(int) cellr.getNumericCellValue();
		HSSFCell cellf = row.getCell((short)1);
		this.nOfFeatures =(int)cellf.getNumericCellValue();
		this.itemName = new String[this.nOfItems];
		this.itemDescription = new String[this.nOfItems];
		this.featureName = new String[this.nOfFeatures];
		this.itemValue = new double[this.nOfItems][this.nOfFeatures];

		//Reads first row
		row= sheet.getRow(1);
		for (int j=0; j<this.nOfFeatures; j++)
		{
			HSSFCell cell = row.getCell((short)(j+2)); 
			this.setFeatureName(j,cell.getStringCellValue());
		}

		//Reads data
		for (int i=1;i<this.nOfItems+1;i++)
		{
			row= sheet.getRow(i+1);
			HSSFCell cell = row.getCell((short)0); //read gene name
			this.setItemName(i-1, cell.getStringCellValue());
			cell = row.getCell((short)1); //read gene description
			this.setItemDescription(i-1, cell.getStringCellValue());
			for (int j=0; j<this.nOfFeatures; j++)
			{
				cell = row.getCell((short)(j+2)); //indice della colonna
				this.setItemValue(i-1, j, cell.getNumericCellValue());
  			}
		}

	}

	/**
	 * Stores data matrix into a text file 
	 * 
	 * @param fName the name of the file to be stored
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */	
	public void storeToFile(String fName) throws FileNotFoundException, IOException, Exception
	{
		File outputDMatrix = new File(fName);

		FileWriter stInputW = new FileWriter(outputDMatrix);
		BufferedWriter bwInput = new BufferedWriter(stInputW);
		bwInput.write(this.getNOfItem()+"\t"+this.getNOfFeature());
		bwInput.newLine();
		bwInput.write("Item\tDescription\t");
		for(int j = 0; j < this.getNOfFeature(); j++)
		{
			bwInput.write(this.getFeatureName(j)+"\t");	
		}
		bwInput.newLine();

		for(int i = 0; i < this.getNOfItem(); i++)
		{
			bwInput.write(this.getItemName(i)+"\t"+this.getItemDescription(i));
			for(int j = 0; j < this.getNOfFeature(); j++)
			{
				bwInput.write("\t"+this.getItemValue(i, j));
			}
			bwInput.newLine();
		}
		bwInput.close();
		stInputW.close();
	}

	/**
	 * Stores data matrix into a excel file
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
		
		HSSFRow row;
		row = sheet.createRow((short)0);
		HSSFCell cellr = row.createCell((short)0);
		cellr.setCellValue((double)this.getNOfItem());
		HSSFCell cellf = row.createCell((short)1);
		cellf.setCellValue((double)this.getNOfFeature());
		
		//CReate first row
		row = sheet.createRow(1);
		HSSFCell cellID = row.createCell((short)0);
		cellID.setCellValue("Item");
		cellID = row.createCell((short)1);
		cellID.setCellValue("Description");
		for (int j=0; j<this.getNOfFeature(); j++)
		{
			HSSFCell cell = row.createCell((short)(j+2)); 
			cell.setCellValue(this.getFeatureName(j));
		}

		//Reads data
		for (int i=0; i<this.getNOfItem(); i++)
		{
			row = sheet.createRow(i+2);
			HSSFCell cell = row.createCell((short)0);
			cell.setCellValue(this.getItemName(i));
			cell = row.createCell((short)1);
			cell.setCellValue(this.getItemDescription(i));
			for (int j=0; j<this.getNOfFeature(); j++)
			{
				cell = row.createCell((short)(j+2));
				cell.setCellValue(this.getItemValue(i, j));
  			}
		}
		out.flush();
		
		wb.write(out);
		out.close();
	}
	
}








