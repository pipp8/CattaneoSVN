package valworkbench.measures.internal.FOM;

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
 * The FOM_Generic_Hierarchical_Init class provides an Object that encapsulates 
 * methods and all the informations for computing Figure Of Merit with Hierarchical 
 * Initialization. This class has to be used only for clustering methods that 
 * need an external Hierarchical initialization. It also allows to reduce the computing time nedeed 
 * to obtain hierarchical clustering solutions 
 * (Average Linkage, Complete Linkage and Single Linkage methods).
 *  This measure takes in input a data matrix and a set of 
 * parameters collected through an Input_Measure object. Measure values 
 * are returned in a Measure_Vector object.
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0
 * @see FigureOfMerit 
 *
 */
public class FOMHierarchicalInit extends FigureOfMerit {
	private final String MEASURE_NAME = "FOM H. Init.";
	/**
	 * Default constructor method
	 *
	 */
	public FOMHierarchicalInit()
	{
		//Default constructor method
	}
	/**
	 * Class constructor specifying input data matrix, input parameters
	 * and a true-solution clustering data
	 * 
	 * @param dataMatrix input data matrix
	 * @see DataMatrix
	 * @param mParameters measure parameters
	 * @see InputMeasure
	 */
	public FOMHierarchicalInit(DataMatrix dataMatrix, InputMeasure mParameters)
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
	public MeasureVector computeMeasure() throws FileNotFoundException, IOException, NumberFormatException, Exception
	{
		this.aggregateFOMValue = new MeasureVector((parameters.getKMax()- parameters.getKMin())+1);
		DataMatrix patMatFom = new DataMatrix();
		String commandLine = null;
		int exitValue;
		
		this.fOMekMatrix = new double[(parameters.getKMax()- parameters.getKMin())+1][dataMatrix.getNOfFeature()];
		
		for (int e = 0; e < dataMatrix.getNOfFeature(); e++)
		{
			patMatFom = dataMatrix.removeFeature(e);
			System.out.println("Execute FOM Hierarchical Init. feature "+e+"\r");
			
			patMatFom.storeToFile(parameters.getAlgInputPath());
			//Path redirection for init algorithm
			//String initPath = parameters.getAlgOutputPath()+"OutCLS"+".txt";
			//Composes command line for Hierarchical Init
			//String command = "\""+this.parameters.getHAlgPath()+"\""+" "+"\""+parameters.getAlgInputPath()+"\""+" "+Integer.toString((parameters.getKMax()+1))+" "+parameters.getType()+" "+"\""+initPath+"\"";
			
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
				initPath = parameters.getAlgOutputPath()+"OutCLS_"+parameters.getKMax()+".txt";
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
		
			//Loads cluster matrix obtained from precomputing
			clusterList.loadFromFile(initPath);
			//Loads similarity matrix
			simMatrix.loadFromFile(parameters.getAlgOutputPath()+parameters.getOutputHierarchical());
			
			if(parameters.getKMax()==this.dataMatrix.getNOfItem())
			{
				this.clusterMatrix = clusterList.clusterListToCMatrix();
				if (parameters.isAdjustmentFactor() & this.adjFactor(this.dataMatrix.getNOfItem(), parameters.getKMax())!=Double.NaN)
					this.fOMekMatrix[(parameters.getKMax()- parameters.getKMin())][e] = this.fOMek(e, parameters.getKMax())/this.adjFactor(this.dataMatrix.getNOfItem(), parameters.getKMax());
				else this.fOMekMatrix[(parameters.getKMax()- parameters.getKMin())][e] = this.fOMek(e, parameters.getKMax());
				if(e == 0)
				{ 
					this.aggregateFOMValue.setNOfCluster((parameters.getKMax()- parameters.getKMin()), parameters.getKMax());
				}
			}
						
			//Initializes Inner Hierarchical algorithm
			HLink hierarchicalLink = new HLink();
		    hierarchicalLink.setClusterList(clusterList);
		    hierarchicalLink.setSimilarityMatrix(simMatrix);
								
			for (int k = maxNumberOfCluster; k >= parameters.getKMin(); k--)
			{
				if(e == 0)
				{				
					this.aggregateFOMValue.setNOfCluster((parameters.getKMax()- parameters.getKMin())-(parameters.getKMax()- k), k);
				}
				//Defines output path
				String tempPath = parameters.getAlgOutputPath()+"OutCLS_"+Integer.toString(k)+".txt";
				
				//Executes Inner Hierarchical Algorithm
				simMatrix = hierarchicalLink.hlinkageStep(hierarchicalLink.getSimilarityMatrix(), parameters.getType());
				//Assigns Cluster_Matrix Object of Hlink to Local CMatrix
				hierarchicalLink.setSimilarityMatrix(simMatrix);
												
				//Writes initialization hierarchical Cluster_Matrix 
				hierarchicalLink.getClusterList().storeToFile(parameters.getInitExtOutPath());
							
				//Composes command line to execute external algorithm
				commandLine = composeCmndLine(k, "\""+parameters.getAlgorithmPath()+"\"", "\""+parameters.getAlgInputPath()+"\"", "\""+tempPath+"\"", 
						"\""+parameters.getInitExtOutPath()+"\"", 0, parameters.getAlgCommandLine(), parameters.isInitExtFlag());
				
				//Executes external algorithm
				exitValue = this.executeAlgorithm(commandLine, tempPath);
				
				//Verifies algorithm execution
				if (exitValue == 1)
				{
					this.fOMekMatrix[(parameters.getKMax()- parameters.getKMin())-(parameters.getKMax()- k)][e] = Double.NaN;
				}
				else if (exitValue == 0)
				{
					this.clusterMatrix.loadFromFile(tempPath);
					
					if (parameters.isAdjustmentFactor() & this.adjFactor(this.dataMatrix.getNOfItem(), k)!=Double.NaN)
						this.fOMekMatrix[(parameters.getKMax()- parameters.getKMin())-(parameters.getKMax()- k)][e] = this.fOMek(e, k)/this.adjFactor(this.dataMatrix.getNOfItem(), k);
					else this.fOMekMatrix[(parameters.getKMax()- parameters.getKMin())-(parameters.getKMax()- k)][e] = this.fOMek(e, k);
				}
			}
			patMatFom = null;
		}
		
		//Computes aggregate FOM on fOMekMatrix
		this.aggregateFOMValue.setMeasureValue(this.aggregateFOM());
		this.headerData = this.writeHeader();
		
		if(this.parameters.isPredictNOfCluster())
		{
			if(Double.isNaN(this.aggregateFOMValue.getMeasureValue((this.parameters.getKMax()-this.parameters.getKMin()))))
			{
				this.bestNumberOfCluster = new MeasureVector(1);
				this.bestNumberOfCluster.setMeasureValue(0, Double.POSITIVE_INFINITY);
				this.bestNumberOfCluster.storeToFile(parameters.getOutputPath()+File.separator+"BestNumberOfCluster.txt");
			}
			else
			{
				this.gFOM = this.computeGFOM(this.aggregateFOMValue);
				this.bestNumberOfCluster = this.estimatedNumberOfCluster(this.gFOM);
				this.gFOM.storeToFile(parameters.getOutputPath()+File.separator+"GeometricFOMDiff.txt");
				this.bestNumberOfCluster.storeToFile(parameters.getOutputPath()+File.separator+"BestNumberOfCluster.txt");
			}
		}
				
		this.headerData.storeToFile(parameters.getOutputPath()+File.separator+"Measure_Header.vhf");
		this.aggregateFOMValue.storeToFile(parameters.getOutputPath()+File.separator+"Measure_Values.txt");
		
	return aggregateFOMValue;
	}
	/**
	 * Collects informations about the experiment.
	 * 
	 * @return the header data for the experiment
	 * @see HeaderData
	 */
	protected HeaderData writeHeader()
	{
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
		if (this.parameters.isAdjustmentFactor())header.setMeasParameters("Adjustment Factor yes");
		else header.setMeasParameters("Adjustment Factor no");
		return header;
	}		
}
