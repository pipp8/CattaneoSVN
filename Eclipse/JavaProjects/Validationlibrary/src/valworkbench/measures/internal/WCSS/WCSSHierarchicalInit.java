package valworkbench.measures.internal.WCSS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.StringTokenizer;

import valworkbench.algorithms.HLink;
import valworkbench.datatypes.ClusterList;
import valworkbench.datatypes.DataMatrix;
import valworkbench.datatypes.HeaderData;
import valworkbench.datatypes.InputMeasure;
import valworkbench.datatypes.MeasureVector;
import valworkbench.datatypes.SimilarityMatrix;
/**
 * The WCSSHierarchicalInit class provides an object that encapsulates methods and all the 
 * informations for computing WCSS Measure. This class has to be used only for clustering methods that 
 * need an external Hierarchical initialization. It also allows to reduce the computing time nedeed 
 * to obtain measure values relative to hierarchical clustering solutions 
 * (Average Linkage, Complete Linkage and Single Linkage methods).
 * This class takes in input a DataMatrix and an InputMeasure object. 
 * Values of the measure are returned in a MeasureVector object. 
 * 
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0
 * @see WithinClustersSumSquares
 *
 */
public class WCSSHierarchicalInit extends WithinClustersSumSquares {
	private final String MEASURE_NAME = "WCSS H. Init.";
	/**
	 * Default constructor
	 *
	 */
	public WCSSHierarchicalInit()
	{
		//Default Constructor
	}
	
	/**
	 * Class constructor specifying input data matrix and input parameters
	 * 
	 * @param dataMatrix input data matrix
	 * @see DataMatrix
	 * @param mParameters measure parameters
	 * @see InputMeasure
	 */
	public WCSSHierarchicalInit(DataMatrix dataMatrix, InputMeasure mParameters)
	{
		this.parameters = mParameters;
		this.dataMatrix = dataMatrix;
	}
	
	/**
	 * Computes measure values
	 *
	 * @return a Measure_Vector object containing measure values
	 * @see MeasureVector
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	public MeasureVector computeMeasure() throws FileNotFoundException,
			IOException, NumberFormatException, Exception {
		this.wCSS = new MeasureVector((parameters.getKMax()- parameters.getKMin())+1);
		this.dataMatrix.storeToFile(parameters.getAlgInputPath());
		String commandLine = null;
		int exitValue;
				
		String initPath =" ";
		String command =" ";
		int maxNumberOfCluster = parameters.getKMax();
			
		if(parameters.getKMax()<this.dataMatrix.getNOfItem())
		{
			initPath = parameters.getAlgOutputPath()+"OutCLS"+".txt";
			command = "\""+this.parameters.getHAlgPath()+"\""+" "+"\""+parameters.getAlgInputPath()+"\""+" "+Integer.toString((parameters.getKMax()+1))+" "+parameters.getType()+" "+"\""+initPath+"\"";
			maxNumberOfCluster = parameters.getKMax();
		}
		else if(parameters.getKMax()==this.dataMatrix.getNOfItem())
		{
			initPath = parameters.getAlgOutputPath()+"OutCLS"+".txt";
			command = "\""+this.parameters.getHAlgPath()+"\""+" "+"\""+parameters.getAlgInputPath()+"\""+" "+Integer.toString(parameters.getKMax())+" "+parameters.getType()+" "+"\""+initPath+"\"";
			maxNumberOfCluster = parameters.getKMax()-1;
		}
		
		//Executes external algorithm for precomputing
		ClusterList clusterList = new ClusterList();
		SimilarityMatrix simMatrix = new SimilarityMatrix();
		
		exitValue = 0;
		
		try
		{
			exitValue = this.executeAlgorithm(command, initPath);
		}
		catch(Exception ie)
		{
			if (exitValue == 1)
			{
				System.out.println("Error in precomputing!! Program terminate ! ");
				System.exit(0);
			}
			System.out.println(ie);
		}
		
		//Loads cluster matrix  obtained from precomputing
		clusterList.loadFromFile(initPath);
		//Loads similarity matrix
		simMatrix.loadFromFile(parameters.getAlgOutputPath()+parameters.getOutputHierarchical());
		
		if(parameters.getKMax()==this.dataMatrix.getNOfItem())
		{
			this.clusterMatrix = clusterList.clusterListToCMatrix(); 
			this.wCSS.setNOfCluster((parameters.getKMax()- parameters.getKMin()), parameters.getKMax());
			this.wCSS.setMeasureValue((parameters.getKMax()- parameters.getKMin()), this.computeW(clusterMatrix));	
		}
				
		//Initializes Inner Hierarchical algorithm
		HLink hierarchicalLink = new HLink();
	    hierarchicalLink.setClusterList(clusterList);
	    hierarchicalLink.setSimilarityMatrix(simMatrix);
		
	    for (int k = maxNumberOfCluster; k >= parameters.getKMin(); k--)
		{
	    	System.out.println("Execute WCSS Hierarchical Init. for "+Integer.toString(k)+" Clusters");
	    	
	    	//Defines output path
			String tempPath = parameters.getAlgOutputPath()+"OutCLS_"+Integer.toString(k)+".txt";
		
			//Executes Inner Hierarchical Algorithm
			simMatrix = hierarchicalLink.hlinkageStep(hierarchicalLink.getSimilarityMatrix(), parameters.getType());
			//Assigns CMatrix Object of Hlink to Local CMatrix
			hierarchicalLink.setSimilarityMatrix(simMatrix);
											
			//Writes initialization hierarchical CMatrix 
			hierarchicalLink.getClusterList().storeToFile(parameters.getInitExtOutPath());
						
			//Composes command line to execute external algorithm
			commandLine = composeCmndLine(k, "\""+parameters.getAlgorithmPath()+"\"", "\""+parameters.getAlgInputPath()+"\"", "\""+tempPath+"\"", 
					"\""+parameters.getInitExtOutPath()+"\"", 0, parameters.getAlgCommandLine(), parameters.isInitExtFlag());
			
			//Executes external algorithm
			exitValue = this.executeAlgorithm(commandLine, tempPath);
			
			if(exitValue == 1)
			{
				this.wCSS.setNOfCluster((parameters.getKMax()- parameters.getKMin()) - (parameters.getKMax()- k), k);
				this.wCSS.setMeasureValue((parameters.getKMax()- parameters.getKMin()) - (parameters.getKMax()- k), Double.NaN);
			}
			else
			{
				this.clusterMatrix.loadFromFile(tempPath); 
				this.wCSS.setNOfCluster((parameters.getKMax()- parameters.getKMin()) - (parameters.getKMax()- k), k);
				this.wCSS.setMeasureValue((parameters.getKMax()- parameters.getKMin()) - (parameters.getKMax()- k), this.computeW(clusterMatrix));
			}
		}
	    
	    this.headerData = this.writeHeader();
	    
	    if(this.parameters.isPredictNOfCluster())
		{
			this.gWCSS = this.computeGWCSS(this.wCSS);
			this.bestNumberOfCluster = this.estimatedNumberOfCluster(this.gWCSS);
			this.gWCSS.storeToFile(parameters.getOutputPath()+File.separator+"GeometricWCSSDiff.txt");
			this.bestNumberOfCluster.storeToFile(parameters.getOutputPath()+File.separator+"BestNumberOfCluster.txt");
		}
	    	    
		this.headerData.storeToFile(parameters.getOutputPath()+File.separator+"Measure_Header.vhf");
		this.wCSS.storeToFile(parameters.getOutputPath()+File.separator+"Measure_Values.txt");
				
		return this.wCSS;
	}
	
	/**
	 * Collects informations about the experiment.
	 * 
	 * @return the header data for the experiment
	 * @see HeaderData
	 */
	protected HeaderData writeHeader() {
		HeaderData header = new HeaderData();
		String stringParam = null;
				
		if(this.parameters.getType().contentEquals("A") || this.parameters.getType().contentEquals("a"))
			header.setAlgorithmName(this.parameters.getAlgorithmName()+" init. Inner Hierarchical Average Linkage");
		else if(this.parameters.getType().contentEquals("C")|| this.parameters.getType().contentEquals("c"))
			header.setAlgorithmName(this.parameters.getAlgorithmName()+" init. Inner Hierarchical Complete Linkage");
		else if(this.parameters.getType().contentEquals("S")|| this.parameters.getType().contentEquals("s"))
			header.setAlgorithmName(this.parameters.getAlgorithmName()+" init. Inner Hierarchical Single Linkage");
		
		stringParam = Integer.toString(this.parameters.getKMin())+"_"+Integer.toString(this.parameters.getKMax()); 
		String algParam = "";
		StringTokenizer st = new StringTokenizer(this.parameters.getAlgCommandLine());

		for (int i = 0; i <= st.countTokens(); i++)
		{
			String temp = st.nextToken();
			if (temp.equals("<inputfile>"));
			else if (temp.equals("<outputfile>"));
			else if (temp.equals("<nofcluster>"));
			else if (temp.equals("<extinit>"));
			else
			{
				algParam = algParam.concat(temp);
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
	
}
