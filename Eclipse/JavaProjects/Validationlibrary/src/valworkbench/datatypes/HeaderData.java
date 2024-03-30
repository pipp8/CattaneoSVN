package valworkbench.datatypes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.StringTokenizer;
/**
 * The HeaderData class provides an object representing informations about 
 * validation experiment. An object of type Header_Data contains a record with the following 
 * fields: 'calendarExp' (containing the date and time of validation experiment), 
 * 'algorithmName' (containing a string corresponding to the name of the 
 * algorithm used in validation experiment), 'algParameters' (containing 
 * a string representing the options used in command line to execute the algorithm used 
 * in validation experiment), 'datasetName' (containing the name of the dataset used 
 * in validation experiment), 'datasetType' (containing the type of dataset 
 * selected for validation experiment, i. e. normalized/standardized or raw), 
 * 'measureName' (containing a string corresponding to the name of validation measure 
 * used in validation experiment), 'measParameters'(containing the options used for validation 
 * measure selected).
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro 
 * @version 1.0
 */
public class HeaderData {
	private Calendar calendarExp;
	private String algorithmName;
	private String algParameters;
	private String datasetName;
	private String datasetType;
	private String measureName;
	private String measParameters;
	/**
	 * Default constructor method
	 *
	 */
	public HeaderData()
	{
		//Default constructor method
	}
	/**
	 * Constructs a new Header_Data object by decoding the specified string.
	 * @param algorithmName a String representing the name of the algorithm used in validation experiment
	 * @param algParameters a String representing the parameters of the external algorithm used in validation experiment
	 * @param datasetName a String representing the name of the dataset used in validation experiment
	 * @param datasetType a String representing the type of dataset normalized/standardized or raw
	 * @param measureName a String representing the name of the measure used in validation experiment
	 * @param measParameters  a String representing the parameters used for the validation measure applied
	 * in validation experiment
	 */
	public HeaderData(String algorithmName, String algParameters, String datasetName, String datasetType, String measureName, String measParameters)
	{
		this.setDateTime();
		this.setAlgorithmName(algorithmName);
		this.setAlgParameters(algParameters);
		this.setDatasetName(datasetName);
		this.setDatasetType(datasetType);
		this.setMeasureName(measureName);
		this.setMeasParameters(measParameters);
	}
	/**
	 * Experiment date accessor method
	 * 
	 * @return date of experiment
	 */
	public String getDateExp() 
	{
		//verificare il bug sul numero del mese viene restituito un numero di mese inferiore di un unità
		//Il +1 della Seconda riga
		return ( Integer.toString(this.calendarExp.get(Calendar.DAY_OF_MONTH))+"/"+
				 Integer.toString((this.calendarExp.get(Calendar.MONTH)+1))+"/"+
				 Integer.toString(this.calendarExp.get(Calendar.YEAR)));				
	}
	/**
	 * Experiment time accessor method 
	 * 
	 * @return time of experiment
	 * 
	 */
	public String getTimeExp()
	{
		return ( Integer.toString(this.calendarExp.get(Calendar.HOUR_OF_DAY))+":"+
	    	     Integer.toString(this.calendarExp.get(Calendar.MINUTE))+":"+
				 Integer.toString(this.calendarExp.get(Calendar.SECOND)));
	}
	/**
	 * 
	 * Sets date and time for experiment
	 *
	 */
	public void setDateTime()
	{
		this.calendarExp = Calendar.getInstance();
	}
	/**
	 * Algorithm name accessor method
	 * 
	 * @return a String representing algorithm name
	 */
	public String getAlgorithmName() 
	{
		return algorithmName;
	}
	/**
	 * Algorithm name mutator method
	 * 
	 * @param algorithmName a String representing algorithm name
	 */
	public void setAlgorithmName(String algorithmName) 
	{
		this.algorithmName = algorithmName;
	}
	/**
	 * Algorithm parameters accessor method
	 * 
	 * @return a String representing algorithm parameters
	 */
	public String getAlgParameters()
	{
		return algParameters;
	}
	/**
	 * Algorithm parameters mutator method
	 * 
	 * @param algParameters a String representing algorithm parameters
	 */
	public void setAlgParameters(String algParameters)
	{
		this.algParameters = algParameters;
	}
	/**
	 * Dataset name accessor method
	 * 
	 * @return a String representing dataset name
	 */
	public String getDatasetName() 
	{
		return datasetName;
	}
	/**
	 * Dataset name mutator method
	 * 
	 * @param datasetName a String representing dataset Name
	 */
	public void setDatasetName(String datasetName) 
	{
		this.datasetName = datasetName;
	}
	/**
	 * Dataset type accessor method
	 * 
	 * @return a String representing dataset type
	 */
	public String getDatasetType()
	{
		return datasetType;		
	}
	/**
	 * Dataset type mutator method
	 * 
	 * @param datasetType a String representing dataset type
	 */
	public void setDatasetType(String datasetType)
	{
		this.datasetType = datasetType;
	}
	/**
	 * Measure name accessor method
	 * 
	 * @return a String representing measure name
	 */
	public String getMeasureName() 
	{
		return measureName;
	}
	/**
	 * Measure name mutator method
	 * 
	 * @param measureName a String representing measure name
	 */
	public void setMeasureName(String measureName) 
	{
		this.measureName = measureName;
	}
	/**
	 * Measure parameters accessor method
	 * 
	 * @return a String representing measure parameters
	 */
	public String getMeasParameters()
	{
		return measParameters;
	}
	/**
	 * Measure parameters mutator method
	 * 
	 * @param measParameters a String representing measure parameters
	 */
	public void setMeasParameters(String measParameters)
	{
		this.measParameters = measParameters;
	}
	/**
	 * Loads header data from a text file
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
		File inputHeadFile = new File(fName);
		
		FileReader fHR = new FileReader(inputHeadFile);
		BufferedReader bHeadR = new BufferedReader(fHR);
		temp = bHeadR.readLine();
		StringTokenizer stD = new StringTokenizer(temp);
		stD.nextToken();
		String date = stD.nextToken();
		temp = bHeadR.readLine();
		StringTokenizer stT = new StringTokenizer(temp);
		stT.nextToken();
		String time = stT.nextToken();
		StringTokenizer stDate = new StringTokenizer(date, "/");
		StringTokenizer stTime = new StringTokenizer(time, ":");
		int day = Integer.parseInt(stDate.nextToken());
		int month = Integer.parseInt(stDate.nextToken());
		int year = Integer.parseInt(stDate.nextToken());
		int hour = Integer.parseInt(stTime.nextToken());
		int minute = Integer.parseInt(stTime.nextToken());
		int seconds = Integer.parseInt(stTime.nextToken());
		
		this.calendarExp = Calendar.getInstance();
		this.calendarExp.set(Calendar.DAY_OF_MONTH, day);
		this.calendarExp.set(Calendar.MONTH, month-1);
		this.calendarExp.set(Calendar.YEAR, year);
		this.calendarExp.set(Calendar.HOUR_OF_DAY, hour);
		this.calendarExp.set(Calendar.MINUTE, minute);
		this.calendarExp.set(Calendar.SECOND, seconds);
		
		//Parse Third Line (i.e. Algorithm Name and parameters) 
		temp = bHeadR.readLine();
		
		StringTokenizer stAlg = new StringTokenizer(temp, "\t");
		stAlg.nextToken();
		StringTokenizer stAlgName = new StringTokenizer(stAlg.nextToken(), "-");
		this.algorithmName = stAlgName.nextToken();
		//String parameters = ""; 
		
		this.algParameters = stAlgName.nextToken();
				
		//Parse Forth Line (i.e. Dataset Name and Type)
		temp = bHeadR.readLine();
		StringTokenizer stDataSet = new StringTokenizer(temp);
		stDataSet.nextToken();
		this.datasetName = stDataSet.nextToken();
		this.datasetType = stDataSet.nextToken();
		
		//Parse Fifth Line (i.e. Measure Name and Parameters) 
		temp = bHeadR.readLine();
		
		StringTokenizer stMeas = new StringTokenizer(temp, "\t");
		stMeas.nextToken();
		String mName = stMeas.nextToken();
		StringTokenizer name = new StringTokenizer(mName, "-");
		this.setMeasureName(name.nextToken());
		
		String measParam = name.nextToken();
		this.setMeasParameters(measParam);
		
		//End Parsing
		bHeadR.close();
		fHR.close();
	}
	/**
	 * Stores header data into a text file 
	 * 
	 * @param fName the name of the file to be stored
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */	
	public void storeToFile(String fName) throws FileNotFoundException, IOException, Exception
	{
		File outHeadFile = new File(fName);
	
		FileWriter fHW = new FileWriter(outHeadFile);
		BufferedWriter bHeadW = new BufferedWriter(fHW);
		bHeadW.write("Date:\t"+this.getDateExp());
		bHeadW.newLine();
		bHeadW.write("Time:\t"+this.getTimeExp());
		bHeadW.newLine();
		bHeadW.write("Algorithm:\t"+this.getAlgorithmName()+" - "+this.getAlgParameters());
		bHeadW.newLine();
		bHeadW.write("Dataset:\t"+this.getDatasetName()+" "+this.getDatasetType());
		bHeadW.newLine();
		bHeadW.write("Measure:\t"+this.getMeasureName()+"-"+this.getMeasParameters()); 
		bHeadW.close();
		fHW.close();
	}
	
}
