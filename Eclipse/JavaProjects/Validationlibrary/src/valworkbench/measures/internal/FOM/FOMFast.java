package valworkbench.measures.internal.FOM;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import valworkbench.datatypes.ClusterList;
import valworkbench.datatypes.DataMatrix;
import valworkbench.datatypes.HeaderData;
import valworkbench.datatypes.InputMeasure;
import valworkbench.datatypes.ListNode;
import valworkbench.datatypes.MeasureVector;
import valworkbench.datatypes.SimilarityMatrix;
/**
 * The FOMFast class provides an Object that encapsulates methods and all the
 * informations for computing Fast Figure Of Merit. This measure takes in input 
 * a data matrix and a set of parameters collected through an 
 * Input_Measure object. Values of the measure are computed with an heuristics 
 * approach and returned in a Measure_Vector object. 
 * In this class clustering solutions submitted to validation are obtained through the 
 * following scheme: first solution is produced from the external clustering algorithm, 
 * the next ones are obtained by merging  two clusters belonging to the last
 * clustering solution obtained from the algorithm. The external clustering algorithm is 
 * again executed when the required number of clusters is a multiple of the integer value 
 * contained in the numberOfIteration field, or it is always executed if the percentage of 
 * the number of steps used to reach the convergence is higher than zero. 
 *   
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0
 * @see FigureOfMerit 
 *
 */
public class FOMFast extends FigureOfMerit {
	private final String MEASURE_NAME = "FOM Fast";
	private ClusterList clusterList;
	private double centroid[][];
	/**
	 * Default constructor method
	 *
	 */
	public FOMFast()
	{
		//Default constructor method
	}
	/**
	 * Class constructor specifying input data matrix and input parameters
	 * 
	 * @param dataMatrix input data matrix
	 * @see DataMatrix
	 * @param mParameters measure parameters
	 * @see InputMeasure
	 */
	public FOMFast(DataMatrix dataMatrix, InputMeasure mParameters)
	{
		this.parameters = mParameters;
		this.dataMatrix = dataMatrix;
	}
	/**
	 * Computes measure values
	 *
	 * @return an Measure_Vector object containing measure values
	 * @see MeasureVector
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	public MeasureVector computeMeasure() throws FileNotFoundException, IOException, NumberFormatException, Exception
	{
		this.aggregateFOMValue = new MeasureVector((parameters.getKMax()- parameters.getKMin())+1);
		DataMatrix patMatFom;
		String commandLine = null;
		boolean isexecAlgorithm;
		int exitValue; 
		
		//Allocates fOMMatrix
		this.fOMekMatrix = new double[(parameters.getKMax()- parameters.getKMin())+1][dataMatrix.getNOfFeature()];
		int maxIndex = (parameters.getKMax()-parameters.getKMin());  
		
		for (int e = 0; e < dataMatrix.getNOfFeature(); e++)
		{
			//FIRST CLUSTERING ALGORITHM EXECUTION. 
						
			patMatFom = dataMatrix.removeFeature(e);
			System.out.println("Execute Fast FOM feature "+e);
			
			//Writes number of clusters in MVector 
			this.aggregateFOMValue.setNOfCluster(maxIndex, parameters.getKMax());
			
			//Writes input data matrix
			patMatFom.storeToFile(parameters.getAlgInputPath());
			
			//Defines output Path
			String tempPath = parameters.getAlgOutputPath()+"OutCLS_"+Integer.toString(parameters.getKMax())+".txt";
			
			//Composes command line for algorithm execution
			commandLine = composeCmndLine(parameters.getKMax(), "\""+parameters.getAlgorithmPath()+"\"", "\""+parameters.getAlgInputPath()+"\"", "\""+tempPath+"\"", 
					"\""+parameters.getInitExtOutPath()+"\"", parameters.getNumberOfSteps(), parameters.getAlgCommandLine(), false);
			
			//Executes Algorithm
			exitValue = this.executeAlgorithm(commandLine, tempPath);
			//Verifies execution
			
			if (exitValue == 1)
			{
				this.fOMekMatrix[(parameters.getKMax()- parameters.getKMin())-(parameters.getKMax()- parameters.getKMax())][e] = Double.NaN;
			}
			else if (exitValue == 0)
			{
				//Loads Cluster Matrix
				//Exception
				this.clusterMatrix.loadFromFile(tempPath); 
				this.clusterList = new ClusterList(this.clusterMatrix);
			}
			
			if (parameters.isAdjustmentFactor() & this.adjFactor(this.dataMatrix.getNOfItem(), parameters.getKMax())!=Double.NaN)
				this.fOMekMatrix[maxIndex][e] = this.fOMek(e, parameters.getKMax())/this.adjFactor(this.dataMatrix.getNOfItem(), parameters.getKMax());
			else this.fOMekMatrix[maxIndex][e] = this.fOMek(e, parameters.getKMax());
						
			//END FIRST ALGORITHM EXECUTION 
			
			ClusterList clsList = new ClusterList((this.clusterMatrix.getNOfCluster()-1));
			
			int nsteps=0;
			isexecAlgorithm = false;
			
			//Computes centroid
			if (parameters.getKMax()!=parameters.getKMin())
			{
				this.centroid = this.computeCentroid(this.dataMatrix, this.clusterList);
											
				clsList = this.mergeClusters(this.clusterList, this.centroid);
				
				//Steps convergence variable
				int stepsUsed = this.readSteps();
				nsteps = (int)Math.floor((double)(Math.min(stepsUsed, parameters.getNumberOfSteps())*parameters.getPercentage())/100);
								
				//Executes algorithm only if nsteps != 0
				if(nsteps == 0) isexecAlgorithm = false;
				else isexecAlgorithm = true;
			}
			
			boolean flag = false;
			int residualRange = parameters.getKMax()-1;
			
			//Successive Iteration in Fast FOM Computing
			for (int k = residualRange; k >= parameters.getKMin(); k--)
			{		
				if(e == 0)
				{ 
					aggregateFOMValue.setNOfCluster((residualRange - parameters.getKMin())-(residualRange- k), k);
				}
				
				if ((parameters.getNumberOfIteration() != 0) & (parameters.getNumberOfIteration() < parameters.getKMax()))
				{
					if(((k % parameters.getNumberOfIteration()) == 0) & (isexecAlgorithm == false))
					{
						flag = true;
						isexecAlgorithm = true;
						nsteps = 1;
					}
					else if ((flag == true) & ((k % parameters.getNumberOfIteration())!=0))
					{
						flag = false;
						isexecAlgorithm = false;
					}
				}
								
				if(isexecAlgorithm)
				{
					clsList.storeToFile(parameters.getInitExtOutPath());
					
					//Defines output path
					tempPath = null;
					tempPath = parameters.getAlgOutputPath()+"OutCLS_"+Integer.toString(k)+".txt";
					
					String tagCommand = "<inputfile> <nofcluster> <nsteps> <extinit> <outputfile>";
					
					//Composes command line for algorithm execution
					commandLine = composeCmndLine(k, "\""+parameters.getAlgorithmPath()+"\"", "\""+parameters.getAlgInputPath()+"\"", "\""+tempPath+"\"", 
							"\""+parameters.getInitExtOutPath()+"\"", nsteps, tagCommand, parameters.isInitExtFlag());
					
					//Executes external algorithm
					exitValue = this.executeAlgorithm(commandLine, tempPath);
					
					//Verifies algorithm execution
					if (exitValue == 1)
					{
						this.fOMekMatrix[(parameters.getKMax()- parameters.getKMin())-(parameters.getKMax()- k)][e] = Double.NaN;
					}
					else if(exitValue == 0)
					{
						this.clusterList.loadFromFile(tempPath);
						
						this.clusterMatrix = this.clusterList.clusterListToCMatrix();
					}
				}
				else
				{
					this.clusterList = clsList;
					this.clusterMatrix = this.clusterList.clusterListToCMatrix();
				}
				
				if (parameters.isAdjustmentFactor() & this.adjFactor(this.dataMatrix.getNOfItem(), k)!=Double.NaN)
					this.fOMekMatrix[(residualRange - parameters.getKMin())-(residualRange - k)][e] = this.fOMek(e, k)/this.adjFactor(this.dataMatrix.getNOfItem(), k);
				else this.fOMekMatrix[(residualRange - parameters.getKMin())-(residualRange - k)][e] = this.fOMek(e, k);
				
				clsList = null;
				
				//Computes centroid
				this.centroid = this.computeCentroid(this.dataMatrix, this.clusterList);
								
				//Merges clusters
				clsList = this.mergeClusters(this.clusterList, this.centroid);
			}
			//End main loop 
			patMatFom = null;
		}
		
		//Computes aggregate FOM on fOMekMatrix
		this.aggregateFOMValue.setMeasureValue(this.aggregateFOM());
		this.headerData = this.writeHeader();
		
		if(this.parameters.isPredictNOfCluster())
		{
			this.gFOM = this.computeGFOM(this.aggregateFOMValue);
			this.bestNumberOfCluster = this.estimatedNumberOfCluster(this.gFOM);
			this.gFOM.storeToFile(parameters.getOutputPath()+File.separator+"GeometricFOMDiff.txt");
			this.bestNumberOfCluster.storeToFile(parameters.getOutputPath()+File.separator+"BestNumberOfCluster.txt");
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
		
		header.setAlgorithmName(this.parameters.getAlgorithmName()+" init. external file"); 
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
			else if (temp.equals("<nsteps>"));
			else
			{
				algParam = algParam.concat(temp);
				temp = null;
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
	/**
	 * Computes centroid from cluster list given in input
	 * 
	 * @param dataMatrix the input data matrix
	 * @param clusterList the cluster list
	 * @return a matrix of double in which each row represents a centroid
	 */
	private double[][] computeCentroid(DataMatrix dataMatrix, ClusterList clusterList)
	{
		ListNode pointer; 
		
		double centroid[][] = new double[clusterList.getNOfCluster()][dataMatrix.getNOfFeature()];
		for(int i = 0; i < clusterList.getNOfCluster(); i++)
		{
			pointer = clusterList.getClusterList(i);
			for(int j = 0; j < clusterList.getCount(i); j++)
			{
				for(int y = 0; y <dataMatrix.getNOfFeature(); y++)
				{
					centroid[i][y] += dataMatrix.getItemValue(Integer.parseInt(pointer.element.toString()), y);
				}
				pointer = pointer.next;
			}
			//Computes Mean 
			for(int x = 0; x < dataMatrix.getNOfFeature(); x++)
			{
				centroid [i][x] = (double) centroid[i][x]/clusterList.getCount(i);
			}
		}
		return centroid;
	}
	/**
	 * Merges two clusters in a cluster list
	 * 
	 * @param clusterList the given cluster list
	 * @see ClusterList
 	 * @param centroid a matrix of double in which each row 
 	 * represents a centroid
	 * @return a Cluster_List object
	 */
	private ClusterList mergeClusters(ClusterList clusterList, double centroid[][])
	{
		int fusionIndex[], minIndex, maxIndex;
		ClusterList clsList = new ClusterList((clusterList.getNOfCluster()-1));
		SimilarityMatrix centroidSimilarity = new SimilarityMatrix(clusterList.getNOfCluster());
		
		//Computes similarity Matrix
		centroidSimilarity.fillSMatrix(centroid, clusterList.getNOfCluster());
		//Extracts max similarity index
		fusionIndex = centroidSimilarity.maxSimilarityIndex();
				
		//Selects minimum row
		if(fusionIndex[0] < fusionIndex[1])
		{
			minIndex = fusionIndex[0];
			maxIndex = fusionIndex[1];
		}
		else
		{
			minIndex = fusionIndex[1];
			maxIndex = fusionIndex[0];
		}
		
		//List Fusion
		clusterList.listUnion(minIndex, maxIndex);
		
		//Copies ClusterList
		for(int i = 0; i < maxIndex; i++)
		{
			clsList.setClusterList(i, clusterList.getClusterList(i));
			clsList.setTail(i, clusterList.getTail(i));
			clsList.setCount(i, clusterList.getCount(i));
		}
	
		for(int i = maxIndex+1; i < clusterList.getNOfCluster(); i++)
		{
			clsList.setClusterList(i-1, clusterList.getClusterList(i));
			clsList.setTail(i-1, clusterList.getTail(i));
			clsList.setCount(i-1, clusterList.getCount(i));
		}
				
		return clsList;
	}
	/**
	 * reads the number of steps used from a partitional algorithm to
	 * reach the convergence.
	 * 
	 * @return an integer representing the number of steps used from 
	 * the algorithm. In particular return -1 if an error occurs.
	 */
	private int readSteps()
	{
		int numberOfSteps;
		String pathName, temp=" "; 
		pathName = parameters.getAlgOutputPath()+parameters.getLogPartitional();
		
		File kMeanLFile = new File(pathName);
		//Opens Log file 
		try
		{
			FileReader fKmeansLR = new FileReader(kMeanLFile);
			BufferedReader bKmeansLR = new BufferedReader(fKmeansLR);
			//Reads Log File
			for(int i=0; i<8; i++)
				temp = bKmeansLR.readLine();
			StringTokenizer stepReader = new StringTokenizer(temp);
			for(int i=0; i<3; i++)
			{
				stepReader.nextToken();
			}
			//Reads number of steps
			numberOfSteps = Integer.parseInt(stepReader.nextToken());
			bKmeansLR.close();
			fKmeansLR.close();
		}
		catch(Exception ioexc)
		{
			numberOfSteps = -1;
		}
		return numberOfSteps;
	}
}
