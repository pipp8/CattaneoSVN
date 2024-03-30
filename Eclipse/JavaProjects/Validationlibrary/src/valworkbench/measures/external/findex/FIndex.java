package valworkbench.measures.external.findex;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.StringTokenizer;

import valworkbench.datatypes.ClusterMatrix;
import valworkbench.datatypes.DataMatrix;
import valworkbench.datatypes.HeaderData;
import valworkbench.datatypes.InputMeasure;
import valworkbench.datatypes.MeasureVector;
import valworkbench.measures.Measure;
/** 
 ** The FIndex class provides an object that encapsulates methods 
 * and states information for computing F-Measure Index. This class 
 * takes in input a Data Matrix and an InputMeasure object. 
 * Measure values are returned in a MeasureVector  
 * object with values ranging between 0 and 1.
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @see MeasureVector
 * @version 1.0
 */
public class FIndex extends Measure {
	private final String MEASURE_NAME = "F-Index";
	private InputFIndex parameters;
	private DataMatrix dataMatrix;
	private MeasureVector fIndex;
	private ClusterMatrix clusterMatrix = new ClusterMatrix();
	private ClusterMatrix trueSolution =  new ClusterMatrix();
	private HeaderData headerData;
	/**
	 * Default constructor method
	 *
	 */		
	public FIndex()
	{
		//Default constructor method
	}
	/**
	 * Class constructor specifying input data matrix, input parameters
	 * and True-solution
	 * 
	 * @param dataMatrix a reference to a DataMatrix object
	 * @see DataMatrix
	 * @param mParameters a reference to an Input_Measure object
	 * @see InputFIndex
	 * @param trueSolution a reference to a Cluster_Matrix object 
	 * @see ClusterMatrix
	 */	
	public FIndex(DataMatrix dataMatrix, InputFIndex mParameters, ClusterMatrix trueSolution)
	{
		this.parameters = mParameters;
		this.dataMatrix = dataMatrix;
		this.trueSolution = trueSolution;
	}
	/**
	 * Computes measure values
	 *
	 * @return a Measure_Vector object containing measure values
	 * ranging between <code>0</code> and <code>1</code>
	 * @see MeasureVector
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NumberFormatException
	 * @throws Exception
	 */	
	public MeasureVector computeMeasure() throws FileNotFoundException, IOException, NumberFormatException, Exception 
	{
		this.fIndex = new MeasureVector((parameters.getKMax()- parameters.getKMin())+1);
		
		String commandLine = null;
		int exitValue;
		
		this.dataMatrix.storeToFile(parameters.getAlgInputPath());
		
		
		for (int k = parameters.getKMin(); k <= parameters.getKMax(); k++)
		{
			String tempPath = parameters.getAlgOutputPath()+"OutCLS_"+Integer.toString(k)+".txt";
			if (parameters.isInitExtFlag())
			{
				commandLine = composeCmndLine(k, "\""+parameters.getInitExtAlgPath()+"\"", "\""+parameters.getInitExtInpPath()+"\"", 
						"\""+parameters.getInitExtOutPath()+"\"", null, 0, parameters.getInitExtCommandLine(), false);
				
				System.out.println("Execute initializzation with "+parameters.getInitAlgName()+" for "+Integer.toString(k)+" clusters");
				exitValue = this.executeAlgorithm(commandLine, parameters.getInitExtOutPath());
				if (exitValue == 1)
				{
					parameters.setInitExtFlag(false);
				}
			}
			commandLine = composeCmndLine(k, "\""+parameters.getAlgorithmPath()+"\"", "\""+parameters.getAlgInputPath()+"\"", "\""+tempPath+"\"", 
					parameters.getInitExtOutPath(), 0, parameters.getAlgCommandLine(), parameters.isInitExtFlag());
			
			System.out.println("Execute clustering algorithm "+parameters.getAlgorithmName()+" for "+Integer.toString(k)+" clusters");
			exitValue = this.executeAlgorithm(commandLine, tempPath); 

			if (exitValue == 1)
			{ 
				this.fIndex.setNOfCluster((k-parameters.getKMin()), k);
				this.fIndex.setMeasureValue((k-parameters.getKMin()), Double.NaN);
			}
			else if (exitValue == 0)
			{
				this.clusterMatrix.loadFromFile(tempPath);
				this.fIndex.setNOfCluster((k-parameters.getKMin()), k);	
				this.fIndex.setMeasureValue((k-parameters.getKMin()), computeFMeasure(this.trueSolution, this.clusterMatrix, dataMatrix.getNOfItem(), this.parameters.getBValue()));
			}
		}
		
		this.headerData = this.writeHeader();
		
		this.headerData.storeToFile(parameters.getOutputPath()+File.separator+"Measure_Header.vhf");
		this.fIndex.storeToFile(parameters.getOutputPath()+File.separator+"Measure_Values.txt");
		
		return fIndex;
	}
	/**
	 * Collects information about the experiment.
	 * 
	 * @return a reference to a Header_Data object
	 * @see HeaderData
	 */
	protected HeaderData writeHeader() 
	{
		HeaderData header = new HeaderData();
		String stringParam = null;
		
		if (this.parameters.isInitExtFlag())
		{
			header.setAlgorithmName(this.parameters.getAlgorithmName()+" init. "+this.parameters.getInitAlgName()); 
		}
		else
		{
			header.setAlgorithmName(this.parameters.getAlgorithmName());
		}
		stringParam = Integer.toString(this.parameters.getKMin())+"_"+Integer.toString(this.parameters.getKMax()); 
		String algParam = "";
		StringTokenizer st = new StringTokenizer(this.parameters.getAlgCommandLine()); 
		for (int i = 0; i <= st.countTokens(); i++)
		{
			String temp = st.nextToken();
			if (temp.equals("<inputfile>"));
			else if (temp.equals("<outputfile>"));
			else if (temp.equals("<nofcluster>"));
			else if (temp.equals("<extinit>"))
			{
				StringTokenizer  stInit = new StringTokenizer(this.parameters.getInitExtCommandLine());
				for (int j = 0; j <= stInit.countTokens(); j++)
				{
					String temp2 = stInit.nextToken();
					if (temp2.equals("<inputfile>"));
					else if (temp2.equals("<outputfile>"));
					else if (temp2.equals("<nofcluster>"));
					else
					{
						algParam = algParam.concat(" "+temp2);
					}
				}
			}
			else 
			{
				algParam = algParam.concat(" "+temp);
			}
		}
		header.setAlgParameters(stringParam+" "+algParam); 
		header.setDatasetName(this.parameters.getDatasetName());
		header.setDatasetType(this.parameters.getDatasetType());	
		header.setDateTime();
		header.setMeasureName(this.MEASURE_NAME);
		header.setMeasParameters(" - ");
		return header;
	}
	/**
	 * Computes the F-Measure index for a given clustering solution and
	 * a given true-solution 
	 * 
	 * @param trueSolution the given true_solution
	 * @param clusterSolution the given cluster solution
	 * @param nOfItem the total number of items of the analized data matrix
	 * @return a double value representing the F-Measure index
	 */	
	public double computeFMeasure(ClusterMatrix trueSolution, ClusterMatrix clusterSolution, int nOfItem, int b)
	{
		int ntkTable [][] = fillNtkTable(trueSolution, clusterSolution);
		double ptCktable [][] = fillPtCkTable(ntkTable, trueSolution, clusterSolution);
		double rtCktable [][] = fillRtCkTable(ntkTable, trueSolution, clusterSolution);
		double ftCktable [][] = fillFtCkTable(ptCktable, rtCktable, trueSolution, clusterSolution, b);
		
		return overallFMeasure(ftCktable, trueSolution, clusterSolution, nOfItem);
	}
	/**
	 * Fills the Ntk table needed to compute the F-Measure index
	 * 
	 * @param trueSolution the given true-solution
	 * @param clusterSolution the given clustering solution
	 * @return a matrix of integer representing the Ntk table
	 */
	private int[][] fillNtkTable(ClusterMatrix trueSolution, ClusterMatrix clusterSolution)
	{
		int ntkTable [][] = new int[trueSolution.getNOfCluster()][clusterSolution.getNOfCluster()];
		for(int i = 0; i < trueSolution.getNOfCluster(); i++)
		{
			for(int j = 0; j < clusterSolution.getNOfCluster(); j++)
			{
				for(int k=0; k<trueSolution.getClusterSize(i); k++)
				{
					for(int h=0; h<clusterSolution.getClusterSize(j); h++)
					{
						if(trueSolution.getCMatrixValue(i, k) == clusterSolution.getCMatrixValue(j, h))
							ntkTable[i][j]+=1;
					}
				}
			}
		}
		return ntkTable;
	}
	/**
	 * Fills the PtCk table (Precision table) needed to compute the F-Measure index
	 * 
	 * @param ntkTable a table containing Ntk value
	 * @param trueSolution the given true-solution
	 * @param clusterSolution the given clustering solution
	 * @return a matrix of integer representing the PtCk table
	 */
	private double[][] fillPtCkTable(int ntkTable[][], ClusterMatrix trueSolution, ClusterMatrix clusterSolution)
	{
		double ptCkTable [][] = new double[trueSolution.getNOfCluster()][clusterSolution.getNOfCluster()];
		for(int i = 0; i < trueSolution.getNOfCluster(); i++)
		{
			for(int j = 0; j < clusterSolution.getNOfCluster(); j++)
			{
				ptCkTable[i][j]= (double) ntkTable[i][j]/clusterSolution.getClusterSize(j);
 			}
		}
		return ptCkTable; 
	}
	/**
	 * Fills the RtCk table (Recall table) needed to compute the F-Measure index
	 * 
	 * @param ntkTable a table containing Ntk value
	 * @param trueSolution the given true-solution
	 * @param clusterSolution the given clustering solution
	 * @return a matrix of integer representing the RtCk table
	 */
	private double[][] fillRtCkTable(int ntkTable[][], ClusterMatrix trueSolution, ClusterMatrix clusterSolution)
	{
		double rtCkTable [][] = new double[trueSolution.getNOfCluster()][clusterSolution.getNOfCluster()];
		for(int i = 0; i < trueSolution.getNOfCluster(); i++)
		{
			for(int j = 0; j < clusterSolution.getNOfCluster(); j++)
			{
				rtCkTable[i][j] = (double) ntkTable[i][j]/trueSolution.getClusterSize(i);
			}
		}
		return rtCkTable;
	}
	/**
	 * Fills the FtCk table needed to compute the F-Measure index
	 * 
	 * @param ptCkTable a table containing PtCk value
	 * @param rtCkTable a table containing RtCk value
	 * @param trueSolution the given true-solution
	 * @param clusterSolution the given clustering solution
	 * @return a matrix of integer representing the FtCk table
	 */
	private double[][] fillFtCkTable(double ptCkTable[][], double rtCkTable[][], ClusterMatrix trueSolution, ClusterMatrix clusterSolution, int b)
	{
		double ftCkTable [][] = new double[trueSolution.getNOfCluster()][clusterSolution.getNOfCluster()];
		for(int i = 0; i < trueSolution.getNOfCluster(); i++)
		{
			for(int j = 0; j < clusterSolution.getNOfCluster(); j++)
			{
				if((ptCkTable[i][j]==0) && (rtCkTable[i][j]==0))
				{
					ftCkTable[i][j]=0;
				}
				else
				{
					ftCkTable[i][j] = (double) (((Math.pow(b, 2)+1)*ptCkTable[i][j]*rtCkTable[i][j])/(Math.pow(b,2)*ptCkTable[i][j]+rtCkTable[i][j]));
				}
			}
		}
		return ftCkTable;
	}
	/**
	 * Computes the max value from a vector of double
	 *  
	 * @param vector a given vector of double 
	 * @return a double representing the maximum value of the input vector
	 */
	private double maxVector(double vector[])
	{
		double maxValue = vector[0];
		
		for(int i = 1; i < vector.length; i++)
		{
			if (vector[i] > maxValue)maxValue = vector[i];
		}
		return maxValue;
	}	
	/**
	 * Computes the overall F-Measure value
	 * 
	 * @param ftCkTable a table containing FtCk value
	 * @param trueSolution the given true-solution
	 * @param clusterSolution the given clustering solution
	 * @param nOfItem the total number of items of the analized data matrix
	 * @return a double value representing the F-Measure value
	 */
	private double overallFMeasure(double ftCkTable[][], ClusterMatrix trueSolution, ClusterMatrix clusterSolution, int nOfItem)
	{
		double fValue = 0.0;
		double temp = 0.0;
		
		for(int i = 0; i < trueSolution.getNOfCluster() ; i++)
		{
			temp = (double) trueSolution.getClusterSize(i) / nOfItem;
			fValue += (double)temp * maxVector(ftCkTable[i]);
			temp = 0.0;
		}
				
		return fValue;
	}
	/**
	 * Data_Matrix mutator method
	 * 
	 * @param patternMatrix a reference to an input Data_Matrix object
	 */
	public void setDataMatrix(DataMatrix patternMatrix) {
		this.dataMatrix = patternMatrix;
	}
	/**
	 * Input_Measure mutator method
	 * 
	 * @param parameters a reference to Input_Measure object
	 * @see InputFIndex 
	 */
	public void setParameters(InputFIndex parameters) {
		this.parameters = parameters;
	}
	/**
	 * True-solution Cluster_Matrix mutator method
	 * 
	 * @param trueSolution a reference to Cluster_Matrix object
	 * @see ClusterMatrix
	 */
	public void setTrueSolution(ClusterMatrix trueSolution) {
		this.trueSolution = trueSolution;
	}
	/**
	 * Measure_Vector accessor method
	 * 
	 * @return a Measure_Vector containing the F-Measure indices
	 * @see MeasureVector
	 */
	public MeasureVector getfMeasure() {
		return this.fIndex;
	}
	/**
	 * Clustering solution Cluster_Matrix accessor method
	 *
	 * @param numberSolution the number of required clustering solution
	 * @return a Cluster_Matrix object
	 * @see ClusterMatrix
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	public ClusterMatrix getClusteringSolution(int numberSolution) throws FileNotFoundException, IOException, NumberFormatException, Exception
	{
		ClusterMatrix clusterMatrix = new ClusterMatrix();
		String path = parameters.getAlgOutputPath()+"OutCLS_"+Integer.toString(numberSolution)+".txt";
		clusterMatrix.loadFromFile(path);
		return clusterMatrix;
	}
	/**
	 * Header_Data accessor method
	 * 
	 * @return a reference to a Header_Data object containing data collected in the experiment
	 * @see HeaderData
	 */
	public HeaderData getHeaderData(){
		return this.headerData;
	}
		
}
