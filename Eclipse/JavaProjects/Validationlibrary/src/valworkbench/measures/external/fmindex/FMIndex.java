package valworkbench.measures.external.fmindex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.StringTokenizer;

import valworkbench.datatypes.ClusterMatrix;
import valworkbench.datatypes.DataMatrix;
import valworkbench.datatypes.HeaderData;
import valworkbench.datatypes.MeasureVector;
import valworkbench.measures.Measure;
/** 
 ** The FMIndex class provides an object that encapsulates methods 
 * and states information for computing Fowlkes and Mallows Index. 
 * This class takes in input a Data Matrix and an InputFMIndex object. 
 * Measure values are returned in a MeasureVector  
 * object with values ranging between 0 and 1.
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @see MeasureVector
 * @version 1.0
 */
public class FMIndex extends Measure {
	private final String MEASURE_NAME = "FM-Index";
	private InputFMIndex parameters;
	private DataMatrix dataMatrix;
	private MeasureVector fMIndex;
	private ClusterMatrix clusterMatrix = new ClusterMatrix();
	private ClusterMatrix trueSolution =  new ClusterMatrix();
	private HeaderData headerData;
	/**
	 * Default constructor method
	 *
	 */	
	public FMIndex()
	{
		//Default constructor Method
	}
	/**
	 * Class constructor specifying input data matrix, input parameters
	 * and True-solution
	 * 
	 * @param dataMatrix a reference to a Data_Matrix object
	 * @see DataMatrix
	 * @param mParameters a reference to an Input_Measure object
	 * @see InputFMIndex
	 * @param trueSolution a reference to a Cluster_Matrix object 
	 * @see ClusterMatrix
	 */
	public FMIndex(DataMatrix dataMatrix, InputFMIndex mParameters, ClusterMatrix trueSolution)
	{
		this.parameters = mParameters;
		this.dataMatrix = dataMatrix;
		this.trueSolution = trueSolution;
	}
	/**
	 * Computes measure values
	 *
	 * @return a Measure_Vector object containing measure values
	 * ranging between <code>1</code> and <code>-1</code>
	 * @see MeasureVector
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NumberFormatException
	 * @throws Exception
	 */	
	public MeasureVector computeMeasure() throws FileNotFoundException,IOException, NumberFormatException, Exception 
	{
		this.fMIndex = new MeasureVector((parameters.getKMax()- parameters.getKMin())+1);
		
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
				this.fMIndex.setNOfCluster((k-parameters.getKMin()), k);
				this.fMIndex.setMeasureValue((k-parameters.getKMin()), Double.NaN);
			}
			else if (exitValue == 0)
			{
				this.clusterMatrix.loadFromFile(tempPath);
				this.fMIndex.setNOfCluster((k-parameters.getKMin()), k);	
				this.fMIndex.setMeasureValue((k-parameters.getKMin()), computefMIndex(this.trueSolution, this.clusterMatrix, dataMatrix.getNOfItem()));
			}
		}
		
		this.headerData = this.writeHeader();
		
		this.headerData.storeToFile(parameters.getOutputPath()+File.separator+"Measure_Header.vhf");
		this.fMIndex.storeToFile(parameters.getOutputPath()+File.separator+"Measure_Values.txt");
			
	return fMIndex;		
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
	 * Computes the Fowlkes and Mallows index for a given clustering solution and
	 * a given true-solution 
	 * 
	 * @param trueSolution the given true_solution
	 * @param clusterSolution the given cluster solution
	 * @param nOfItem the total number of items of the analized data matrix
	 * @return a double value representing the Fowlkes and Mallows index
	 */
	public double computefMIndex(ClusterMatrix trueSolution, ClusterMatrix clusterSolution, int nOfItem)
	{
		double fMIndexValue=0;
		int contingencyTable[][];
		int tkValue=0;
		int pkValue=0;
		int qkValue=0;
					
		//Filling contingency table
		contingencyTable = this.fillContingencyTable(trueSolution, clusterSolution);
		
		//Computes Tk
		for(int i=0; i<trueSolution.getNOfCluster(); i++)
		{
			for(int j=0; j<clusterSolution.getNOfCluster(); j++)
			{
				tkValue += Math.pow(contingencyTable[i][j], 2);
			}
		}
		tkValue = tkValue - nOfItem;
		
		//Computes Pk
		pkValue = computePk(trueSolution, nOfItem);
		
		//Computes Qk
		qkValue = computeQk(clusterSolution, nOfItem);		
		
		//Compute FM Index Value
		fMIndexValue = ((double) tkValue / (double)(Math.sqrt(pkValue) * Math.sqrt(qkValue))); 
							
		return fMIndexValue;
	}
	/**
	 * Computes Pk value for a given true-solution and
	 *  
	 */
	private int computePk(ClusterMatrix trueSolution, int nOfItem)
	{
		int pkValue=0;
		
		for(int i=0; i<trueSolution.getNOfCluster(); i++)
		{
			pkValue += Math.pow(trueSolution.getClusterSize(i), 2);
		}
		pkValue = pkValue - nOfItem;
			
		return pkValue;
	}
	/**
	 * Computes Qk value for a given Clustering Solution and
	 * 
	 */
	private int computeQk(ClusterMatrix clusterSolution, int nOfItem)
	{
		int qkValue=0;
		
		for(int i=0; i<clusterSolution.getNOfCluster(); i++)
		{
			qkValue += Math.pow(clusterSolution.getClusterSize(i), 2);
		}
		qkValue = qkValue - nOfItem;
		
		return qkValue; 
	}
	/**
	 * Fills the contingency table needed to compute the FM Index
	 * 
	 * @param trueSolution the given true-solution
	 * @param clusterSolution the given clustering solution
	 * @return a matrix of integer representing the contingency table
	 */
	private int[][] fillContingencyTable(ClusterMatrix trueSolution, ClusterMatrix clusterSolution)
	{
		//Compares True-Solution and Clustering Solution
		int contingencyTable[][] = new int[trueSolution.getNOfCluster()][clusterSolution.getNOfCluster()];
				
		//Filling Contingency Table
		for(int i=0; i<trueSolution.getNOfCluster(); i++)
		{
			for(int j=0; j<clusterSolution.getNOfCluster(); j++)
			{
				for(int k=0; k<trueSolution.getClusterSize(i); k++)
				{
					for(int h=0; h<clusterSolution.getClusterSize(j); h++)
					{
						if(trueSolution.getCMatrixValue(i, k) == clusterSolution.getCMatrixValue(j, h))
							contingencyTable[i][j]+=1;
					}
				}
			}
		}
		return contingencyTable;
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
	 * @see InputFMIndex 
	 */
	public void setParameters(InputFMIndex parameters) {
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
	 * @return a Measure_Vector containing the adjusted rand indices
	 * @see MeasureVector
	 */
	public MeasureVector getFMMeasure() {
		return this.fMIndex;
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
