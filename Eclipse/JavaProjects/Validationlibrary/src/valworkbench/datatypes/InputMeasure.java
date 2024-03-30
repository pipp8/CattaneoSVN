package valworkbench.datatypes;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.StringTokenizer;

import valworkbench.exceptions.DirNotFoundException;
import valworkbench.exceptions.FileAlgNotFoundException;
import valworkbench.exceptions.InvalidCmndLineException;
import valworkbench.exceptions.NotValidOptionException;
/**
 * The InputMeasure class provides an object to collect all necessary parameters
 * to execute validation measure.
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0
 */
public abstract class InputMeasure {
	private String measureName;			
	private int kMin, kMax;				
	private String algorithmPath;  		
	private String algorithmName;		
	private String datasetName;			
	private String datasetType;			
	private String algCommandLine; 		
	private String outputPath = null;     		
	private String algInputPath;   		 
	private String algOutputPath;  		
	private String trueSolPath;			
	private boolean initExtFlag;   		
	private String initAlgName;			
	private String initExtAlgPath;		
	private String initExtCommandLine;	
	private String initExtInpPath;  	
	private String initExtOutPath;		
	private boolean adjustmentFactor;
	private boolean predictNOfCluster;
	private int numberOfIteration;
	private int numberOfResample;
	private int numberOfSteps;			 
	private int percentage;				
	private String resample;			
	private String type;
	private int numberOfRun;
	private String logPartitional;   
	private String outputHierarchical;
	private String hAlgPath;
	private boolean hierarchical;
	private boolean replaceAk;
	private double pMax;
	private double dMin;
	private String externalName;
	private int bValue;
	/**
	 * Adjustment factor flag accessor method
	 * 
	 * @return boolean value for adjustment factor
	 */
	public boolean isAdjustmentFactor() { 
		return adjustmentFactor;
	}
	/**
	 * Adjustment factor flag mutator method 
	 * 
	 * @param adjustmentFactor a boolean value indicating the use of adjustment factor
	 */
	public void setAdjustmentFactor(boolean adjustmentFactor) { 
		this.adjustmentFactor = adjustmentFactor;
	}
	/**
	 * Predict Number of cluster flag mutator method 
	 *
	 * @return boolean value for prediction number of cluster request
	 */	
	public boolean isPredictNOfCluster() {
		return predictNOfCluster;
	}
	/**
	 * Predict Number of cluster flag accessor method 
	 * 
	 * @param predictNOfCluster a boolean value indicating prediction number of cluster request
	 */
	public void setPredictNOfCluster(boolean predictNOfCluster) {
		this.predictNOfCluster = predictNOfCluster;
	}
	/**
	 * Algorithm command line accessor method
	 * 
	 * @return a String representing algorithm command line
	 */
	public String getAlgCommandLine() {
		return algCommandLine;
	}
	/**
	 * Algorithm command line mutator method
	 * 
	 * @param algCommandLine a String representing algorithm command line
	 * @throws InvalidCmndLineException
	 */
	public void setAlgCommandLine(String algCommandLine) throws NullPointerException, InvalidCmndLineException 
	{ 
		String token;
		StringTokenizer st = new StringTokenizer(algCommandLine);
		
		int count=0;
		while(st.hasMoreTokens())
		{
			token = st.nextToken();
			
			if (token.contentEquals("<inputfile>"))
			{
				count += 1; 
			}
			else if (token.contentEquals("<nofcluster>"))
			{
				count += 1;
			}
			else if (token.contentEquals("<outputfile>"))
			{
				count += 1;
			}
			else if (token.contentEquals("<nsteps>") && ((this.measureName == "FOM Fast") || (this.measureName == "FOM Fast Precision") || (this.measureName == "WCSS Fast") || (this.measureName == "WCSS Fast Precision") || (this.measureName == "Gap Statistics Fast")))
			{
				count += 1;
			}
			else if (token.contentEquals("<extinit>") && this.initExtFlag)
			{
				count += 1;
			}
			else if (token.contentEquals("<extinit>") && !this.initExtFlag)
			{
				throw new InvalidCmndLineException();
			}
			else if (token.contentEquals("<nsteps>") && ((this.measureName != "FOM Fast") || (this.measureName != "FOM Fast Precision") || (this.measureName != "WCSS Fast") || (this.measureName != "WCSS Fast Precision") || (this.measureName != "Gap Statistics Fast")))
			{
				throw new InvalidCmndLineException();
			}
		}
		if (count < 3)
		{
			throw new InvalidCmndLineException();
		}
		else if (count >= 3)
		{
			this.algCommandLine = algCommandLine;
		}
	}
	/**
	 * Algorithm path accessor method
	 * 
	 * @return a String representing algorithm path
	 */
	public String getAlgorithmPath() {
		return algorithmPath;
	}
	/**
	 * Algorithm path mutator method
	 * 
	 * @param algorithmPath a String representing algorithm path name
	 * @throws FileAlgNotFoundException 
	 */
	public void setAlgorithmPath(String algorithmPath)throws NullPointerException, FileAlgNotFoundException  
	{ 
		if (this.checkFilePath(algorithmPath) | algorithmPath.toLowerCase().contentEquals("hierarchical"))
		{		
			this.algorithmPath = algorithmPath;
		}
		else
		{
			throw new FileAlgNotFoundException(algorithmPath);
		}
	}
	/**
	 * Initialization algorithm command line accessor method 
	 * 
	 * @return a String representing algorithm command line
	 */
	public String getInitExtCommandLine() {
		return initExtCommandLine;
	}
	/**
	 * Initialization algorithm command line mutator method  
	 * 
	 * @param initExtCommandLine a String representing command line
	 * @throws InvalidCmndLineException
	 */
	public void setInitExtCommandLine(String initExtCommandLine) throws InvalidCmndLineException
	{  
		String token;
		StringTokenizer st = new StringTokenizer(initExtCommandLine);
		
		int count=0;
		
		while(st.hasMoreTokens())
		{
			token = st.nextToken();
			
			if (token.contentEquals("<inputfile>"))
			{
				count += 1;
			}
			else if (token.contentEquals("<nofcluster>"))
			{
				count += 1;
			}
			else if (token.contentEquals("<outputfile>"))
			{
				count += 1;
			}
		}
		if (count < 3)
		{
			throw new InvalidCmndLineException();
		}
		else if (count == 3)
		{
			this.initExtCommandLine = initExtCommandLine;
		}
	}
	/**
	 * External initialization flag accessor method 
	 * 
	 * @return a boolean value indicating external initialization
	 */
	public boolean isInitExtFlag() { 
		return initExtFlag;
	}
	/**
	 * External initialization flag mutator method 
	 * 
	 * @param initExtFlag a boolean value indicating the use of external initialization
	 */
	public void setInitExtFlag(boolean initExtFlag) { 
		this.initExtFlag = initExtFlag;
	}
	/**
	 * Input path for external initialization accessor method
	 * 
	 * @return a String representing path name to external initialization input data file 
	 */
	public String getInitExtInpPath() { 
		return initExtInpPath;
	}
	/**
	 * Input path for external initialization mutator method 
	 * 
	 * @param initExtInpPath a String representing path name to external initialization input data file
	 */
	public void setInitExtInpPath(String initExtInpPath){
		this.initExtInpPath = initExtInpPath;
	}
	/**
	 * Output path for external initialization accessor method 
	 * 
	 * @return a String representing path name to external initialization output data file
	 */
	public String getInitExtOutPath() {
		return initExtOutPath;
	}
	/**
	 * Output path for external initialization mutator method
	 * 
	 * @param initExtOutPath a String representing path name to external initialization output data file
	 */
	public void setInitExtOutPath(String initExtOutPath){ 
		this.initExtOutPath = initExtOutPath;
	}
	/**
	 * Initialization algorithm path accessor method
	 * 
	 * @return a String representing initialization algorithm path
	 */
	public String getInitExtAlgPath() {
		return initExtAlgPath;
	}
	/**
	 * Initialization algorithm path mutator method 
	 * 
	 * @param initExtAlgPath a String representing initialization algorithm path
	 * @throws FileAlgNotFoundException 
	 */
	public void setInitExtAlgPath(String initExtAlgPath) throws FileAlgNotFoundException 
	{ 
		if (this.checkFilePath(initExtAlgPath) | initExtAlgPath.toLowerCase().contentEquals("hierarchical"))
		{		
			this.initExtAlgPath = initExtAlgPath;
		}
		else
		{
			throw new FileAlgNotFoundException(initExtAlgPath); 
		}
	}
	/**
	 * Maximum number of clusters required accessor method 
	 * 
	 * @return an integer representing the maximum number of clusters required
	 */
	public int getKMax() {
		return kMax;
	}
	/**
	 * Maximum number of clusters required mutator method  
	 * 
	 * @param max an integer representing maximum number of clusters required
	 */
	public void setKMax(int max) throws NotValidOptionException {
		if ((max != 0) && ((max >= this.getKMin()) || (this.getKMin() == 0)))		
			kMax = max;
		else
			throw new NotValidOptionException();
	}
	/**
	 * Minimum number of clusters required accessor method 
	 * 
	 * @return an integer representing the minimum number of clusters required
	 */
	public int getKMin() {
		return kMin;
	}
	/**
	 * Minimum number of clusters required mutator method 
	 * 
	 * @param min an integer representing minimum number of clusters required
	 */
	public void setKMin(int min) throws NotValidOptionException	{
		if((min > 0) && ((min <= this.getKMax()) || (this.getKMax() == 0)))
			kMin = min;
		else
			throw new NotValidOptionException();
	}
	
	/**
	 * Number of resample accessor method
	 * 
	 * @return the number of resamples for gap statistics measure
	 */
	public int getNumberOfResample() {
		return numberOfResample;
	}
	/**
	 * Number of resample mutator method
	 * 
	 * @param numberOfResample an integer representing the number of resamples
	 */
	public void setNumberOfResample(int numberOfResample) {
		this.numberOfResample = numberOfResample;
	}
	/**
	 * Number of iterations accessor method
	 * 
	 * @return an integer representing number of iterations
	 */
	public int getNumberOfIteration() {  
		return numberOfIteration;
	}
	/**
	 * Number of iterations mutator method
	 * 
	 * @param numberOfIteration an integer representing number of iterations
	 * @throws NotValidOptionException 
	 */
	public void setNumberOfIteration(int numberOfIteration) throws NotValidOptionException 
	{
		if(numberOfIteration <= 0)
		{
			throw new NotValidOptionException(Integer.toString(numberOfIteration), "numberOfIteration");
		}
		else
		{
			this.numberOfIteration = numberOfIteration;
		}	
	}
	/**
	 * Number of steps accessor method
	 * 
	 * @return an integer representing number of steps
	 */		
	public int getNumberOfSteps() {
		return numberOfSteps;
	}
	/**
	 * Number of steps mutator method 
	 * 
	 * @param numberOfSteps an integer representing number of steps
	 */
	public void setNumberOfSteps(int numberOfSteps) {
		this.numberOfSteps = numberOfSteps;
	}
	/**
	 * Output path accessor method
	 * 
	 * @return a String representing output path
	 */
	public String getOutputPath() {   
		return outputPath;
	}
	/**
	 * Output path mutator method 
	 * 
	 * @param outputPath a String representing output path
	 */
	public void setOutputPath(String outputPath) throws DirNotFoundException
	{     
		if(outputPath.contentEquals(""))
		{
			throw new DirNotFoundException();
		}
		else
		{
			this.outputPath = outputPath;
		}
	}
	
	/**
	 * Percentage accessor method 
	 * 
	 * @return an integer representing a percentage
	 */
	public int getPercentage() {
		return percentage;
	}
	/**
	 * Percentage mutator method
	 * 
	 * @param percentage an integer representing a percentage
	 * @throws NotValidOptionException
	 */
	public void setPercentage(int percentage) throws NotValidOptionException
	{  
		if((percentage > 100) || (percentage == 0))
		{
			throw new NotValidOptionException(Integer.toString(percentage), "percentage");
		}
		else
		{
			this.percentage = percentage;
		}
	}
	/**
	 * Type accessor method 
	 * 
	 * @return a String equal to p or e for Gap Statistics and a String equal to A, C, S for 
	 * FOM hierarchical measure
	 */	
	public String getType() {
		return type;
	}
	/**
	 * Type mutator method 
	 * 
	 * @param type a String representing distance type for Gap Statistics 
	 *       and algorithm type for FOM hierarchical
	 * @throws NotValidOptionException
	 */
	public void setType(String type) throws NotValidOptionException 
	{ 
		if( type.toLowerCase().contentEquals("a") | type.toLowerCase().contentEquals("c") 
		| type.toLowerCase().contentEquals("s"))
		{
			this.type = type;
		}
		else
		{
			throw new NotValidOptionException(type, "type");
		}
	}
		
	/**
	 * Resamples accessor method  
	 * 
	 * @return a String representing type of resample in Gap Statistics
	 */
	public String getResample() {
		return resample;
	}
	/**
	 * Resamples mutator method  
	 * 
	 * @param resample a String representing the type of resample for the  measure that require it 
	 * it takes one of the following value: m (Monte Carlo analysis with poisson sampling ), c (Column sampling), p 
	 * (Monte carlo analysis with poisson resample on principal components)
	 * @throws NotValidOptionException 
	 */
	public void setResample(String resample) throws NotValidOptionException
	{ 
		if(resample.contentEquals("m") || resample.contentEquals("c") || resample.contentEquals("p"))
		{
			this.resample = resample;
		}
		else
		{
			throw new NotValidOptionException(resample, "resample");
		}
	}
	/**
	 * Algorithm input path accessor method 
	 * 
	 * @return a String representing algorithm input path
	 */
	public String getAlgInputPath() {
		return algInputPath;
	}
	/**
	 * Algorithm input path mutator method
	 * 
	 * @param algInputPath a String representing algorithm input path
	 */
	public void setAlgInputPath(String algInputPath){ 
		this.algInputPath = algInputPath;
	}
	/**
	 * Algorithm output path accessor method 
	 * 
	 * @return a String representing algorithm output path
	 */
	public String getAlgOutputPath() {
		return algOutputPath;
	}
	/**
	 * Algorithm output path mutator method 
	 * 
	 * @param algOutputPath a String representing algorithm output path
	 */
	public void setAlgOutputPath(String algOutputPath){ 
		this.algOutputPath = algOutputPath;
	}
	/**
	 * Algorithm name accessor method
	 * 
	 * @return a String representing algorithm name
	 */
	public String getAlgorithmName() {
		return algorithmName;
	}
	/**
	 * Algorithm name mutator method 
	 * 
	 * @param algorithmName a String representing algorithm name
	 */
	public void setAlgorithmName(String algorithmName) {
		this.algorithmName = algorithmName;
	}
	/**
	 * Dataset name accessor method
	 * 
	 * @return a String representing dataset name
	 */
	public String getDatasetName() {
		return datasetName;
	}
	/**
	 * Dataset name mutator method  
	 * 
	 * @param datasetName a String representing dataset name
	 */
	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}
	/**
	 * Dataset type accessor method 
	 * 
	 * @return a String representing dataset type
	 */
	public String getDatasetType() {
		return datasetType;
	}
	/**
	 * Dataset type mutator method
	 * 
	 * @param datasetType a String representing dataset type
	 */
	public void setDatasetType(String datasetType) {
		this.datasetType = datasetType;
	}
	/**
	 * Initialization algorithm name Accessor method
	 * 
	 * @return a String representing initialization algorithm name
	 */
	public String getInitAlgName() {
		return initAlgName;
	}
	/**
	 * Initialization algorithm name Mutator method 
	 * 
	 * @param initAlgName a String representing initialization algorithm name
	 */
	public void setInitAlgName(String initAlgName) {
		this.initAlgName = initAlgName;
	}
	/**
	 * Measure name accessor method 
	 * 
	 * @return a String representing measure name
	 */
	public String getMeasureName() {
		return measureName;
	}
	/**
	 * Measure name mutator method 
	 * 
	 * @param measureName a String representing measure name
	 */
	public void setMeasureName(String measureName) {
		this.measureName = measureName;
	}
	/**
	 * True solution path accessor method
	 * 
	 * @return a String representing true solution data file path
	 */
	public String getTrueSolPath() {
		return trueSolPath;
	}
	/**
	 * True solution path mutator method 
	 * 
	 * @param trueSolPath a String representing true solution data file path
	 * @throws FileNotFoundException 
	 */
	public void setTrueSolPath(String trueSolPath) throws FileNotFoundException
	{ 		
		if (this.checkFilePath(trueSolPath))
		{		
			this.trueSolPath = trueSolPath;
		}
		else
		{
			throw new FileNotFoundException(); 
		}
	}
	/**
	 * Current auxiliary directory accessor method
	 * 
	 * @return a String representing auxiliary directory path 
	 * @throws DirNotFoundException
	 */
	public String getCurrentAuxDir() throws DirNotFoundException, NullPointerException
	{
		if(this.getOutputPath() == null)
		{
			throw new DirNotFoundException();
		}
		String auxPath = this.getOutputPath()+File.separator+"Auxiliary";
		File auxDir = new File(auxPath);
		if(!auxDir.exists())auxDir.mkdirs();
		return auxPath;
	}
	/**
	 * Number of runs in Gap statistics accessor method
	 * 
	 * @return the number of runs used in gap statistics measure
	 */
	public int getNumberOfRun() {
		return numberOfRun;
	}
	/**
	 * Number of runs in Gap statistics mutator method
	 * 
	 * @param numberOfRun an integer representing the number fo runs used in Gap statistics
	 */
	public void setNumberOfRun(int numberOfRun) {
		this.numberOfRun = numberOfRun;
	}
	/**
	 * Partitional log file accessor method
	 * 
	 * @return a String representing name of log file
	 */	
	public String getLogPartitional() {
		return logPartitional;
	}
	/**
	 * Partitional log file mutator method 
	 * 
	 * @param logPartitional a String representing name of log file
	 */
	public void setLogPartitional(String logPartitional) {
		this.logPartitional = logPartitional;
	}
	/**
	 * Auxiliary file name for hierarchical algorithm accessor method 
	 * 
	 * @return a String representing auxiliary file name for hierarchical algorithm
	 */
	public String getOutputHierarchical() {
		return outputHierarchical;
	}
	/**
	 * Auxiliary filename for hierarchical algorithm mutator method 
	 * 
	 * @param outputHierarchical a String representing auxiliary file name for hierarchical algorithm
	 */
	public void setOutputHierarchical(String outputHierarchical) {
		this.outputHierarchical = outputHierarchical;
	}
	/**
	 * hierarchical algorithm path accessor method
	 * 
	 * @return the path name to the external hierarchical algorithm used from hierarchical class
	 */	
	public String getHAlgPath() {
		return hAlgPath;
	}
	/**
	 * hierarchical algorithm path mutator method
	 * 
	 * @param hierarchicalAlgorithmPath
	 */
	public void setHAlgPath(String hierarchicalAlgorithmPath) {
		this.hAlgPath = hierarchicalAlgorithmPath;
	}
	/**
	 * hierarchical flag for Consensus, accessor method
	 * 
	 * @return a boolean value indicating that the algorithm used from consensus is hierarchical
	 */
	public boolean isHierarchical() {
		return hierarchical;
	}
	/**
	 * hierarchical flag for Consensus, mutator method
	 * 
	 * @param hierarchical
	 */
	public void setHierarchical(boolean hierarchical) {
		this.hierarchical = hierarchical;
	}
	/**
	 * Replace Ak flag for Consensus, accessor method
	 * 
	 * @return 
	 */	
	public boolean isReplaceAk() {
		return replaceAk;
	}
	/**
	 * Replace Ak flag for Consensus, mutator method
	 * 
	 * @param replaceAk
	 */
	public void setReplaceAk(boolean replaceAk) {
		this.replaceAk = replaceAk;
	}
	/**
	 * maximum p-value, confidence level for CLEST internal measure accessor method
	 * 
	 * @return a double representing the level of confidence
	 */	
	public double getPMax() {
		return pMax;
	}
	/**
	 * Maximum p-value, confidence level for CLEST internal measure mutator method
	 * 
	 * @param max a double representing the level of confidence
	 */
	public void setPMax(double max) {
		pMax = max;
	}
	/**
	 * Minimum difference statistic d-value, for CLEST internal measure accessor method
	 * 
	 * @return a double value representing the minimum difference statistic
	 */
	public double getDMin() {
		return dMin;
	}
	/**
	 * Minimum difference statistic d-value, for CLEST internal measure mutator method
	 * 
	 * @param min a double value representing the minimum difference statistic
	 */
	public void setDMin(double min) {
		dMin = min;
	}
	/**
	 * Name of external measure for CLEST internal measure accessor method
	 * 
	 * @return a string representing the name of the external measure for CLEST
	 */
	public String getExternalName() {
		return externalName;
	}
	/**
	 * Name of external measure for CLEST internal measure mutator method
	 * 
	 * @param externalName a string representing the name of the external measure for CLEST
	 */
	public void setExternalName(String externalName) {
		this.externalName = externalName;
	}
	/**
	 * b value for F-Measure accessor method
	 * 
	 * @return a integer value representing the b value
	 */	
	public int getBValue() {
		return bValue;
	}
	/**
	 * b value for F-Measure mutator method
	 * 
	 * @param value a integer value representing the b value 
	 */
	public void setBValue(int value) {
		bValue = value;
	}
	/**
	 * Checks correctness of parameters setting 
	 * 
	 * @return a boolean value equal to true if parameters are correctly set false otherwise
	 */
	public boolean checkInputMeasure()
	{
		boolean checkMeasure = true;
		
		if(this.getKMin() == 0)checkMeasure = false;
		else if(this.getKMax() == 0)checkMeasure = false;
		else if(this.getOutputPath() == null)checkMeasure = false;
		else if(this.getAlgorithmPath() == null)checkMeasure = false;
		else if(this.getAlgInputPath() == null)checkMeasure = false;
		else if(this.getAlgOutputPath() == null)checkMeasure = false;
		else if(this.getAlgCommandLine() == null)checkMeasure = false;
		else if(this.isInitExtFlag())
		{
			if(this.getInitExtAlgPath() == null)checkMeasure = false;
			else if (this.getInitExtInpPath() == null)checkMeasure = false;
			else if (this.getInitExtOutPath() == null)checkMeasure = false;
			else if (this.getInitExtCommandLine() == null)checkMeasure = false;
		}
		else if (this.measureName.contentEquals("Adjusted Rand"))
		{
			if(this.getTrueSolPath() == null)checkMeasure = false;
		}
		else if(this.measureName.contentEquals("2 Norm FOM"))
		{
			//NOP
		}
		else if(this.measureName.contentEquals("2 Norm Fast FOM"))
		{
			if(this.getNumberOfSteps() == 0)checkMeasure = false;
			if(this.getNumberOfIteration() == 0)checkMeasure = false;
			if(this.getPercentage() == 0)checkMeasure = false;
		}
		else if(this.measureName.contentEquals("2 Norm FOM hierarchical") || this.measureName.contentEquals("2 Norm FOM with hierarchical Init."))
		{
			if(this.getType() == null)checkMeasure = false;
		}
		else if(this.measureName.contentEquals("Consensus Cluster"))
		{
			if((this.getNumberOfIteration() == 0)|(this.getPercentage() == 0))checkMeasure = false;
		}
		else if (this.measureName.contentEquals("Gap Statistic"))
		{
			if ((this.getNumberOfIteration() == 0)|(this.getResample().equals(null))|(this.getType().equals(null)))
				checkMeasure = false;
		}
		return checkMeasure;
	}
	/**
	 * Checks existence of the given input path
	 * 
	 * @param fileName a String representing path file name
	 * @return a boolean value equal to true if file exists, 
	 * false otherwise
	 */
	private boolean checkFilePath(String fileName)
	{
		boolean checkFile;
		
		File filePointer = new File(fileName);
		
		if (!filePointer.exists())
		{
			checkFile = false;
		}
		else
		{
			checkFile = true;
		}
		
	return checkFile;
	}
	/**
	 * 
	 * @param path
	 * @return
	 */
	public String extractName(String path)
	{
		String name = null;
		String temp = null;
		StringTokenizer stName = new StringTokenizer(path,  File.separator);
		for (int i = 0; i < (stName.countTokens()-1); i++)stName.nextToken();
		temp = stName.nextToken();
		StringTokenizer stName2 = new StringTokenizer(temp, ".");
		name = stName2.nextToken();
		return name;
	}	
}
