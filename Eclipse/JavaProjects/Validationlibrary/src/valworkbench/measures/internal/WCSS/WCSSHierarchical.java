package valworkbench.measures.internal.WCSS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import valworkbench.algorithms.HLink;
import valworkbench.datatypes.ClusterList;
import valworkbench.datatypes.DataMatrix;
import valworkbench.datatypes.HeaderData;
import valworkbench.datatypes.InputMeasure;
import valworkbench.datatypes.MeasureVector;
import valworkbench.datatypes.SimilarityMatrix;
/**
 * The WCSSHierarchical class provides an object that encapsulates methods and all the 
 * informations for computing WCSS Measure. This class has to be used only for hierarchical clustering methods 
 * and allows to reduce the computing time nedeed to obtain hierarchical clustering solutions 
 * (Average Linkage, Complete Linkage and Single Linkage methods).
 * This class takes in input a Data Matrix and an InputMeasure object. 
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
public class WCSSHierarchical extends WithinClustersSumSquares {
	private final String MEASURE_NAME = "WCSS Hierarchical";
	/**
	 * Default constructor
	 *
	 */
	public WCSSHierarchical()
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
	public WCSSHierarchical(DataMatrix dataMatrix, InputMeasure mParameters)
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
		
		int exitValue;
		this.wCSS = new MeasureVector((parameters.getKMax()- parameters.getKMin())+1);
				
		this.dataMatrix.storeToFile(parameters.getAlgInputPath());
		
		String tempPath =" ";
		String command =" ";
		int maxNumberOfCluster = parameters.getKMax();
		
		if(parameters.getKMax()<this.dataMatrix.getNOfItem())
		{
			tempPath = parameters.getAlgOutputPath()+"OutCLS"+".txt";
			command = "\""+this.parameters.getHAlgPath()+"\""+" "+"\""+parameters.getAlgInputPath()+"\""+" "+Integer.toString((parameters.getKMax()+1))+" "+parameters.getType()+" "+"\""+tempPath+"\"";
			maxNumberOfCluster = parameters.getKMax();
		}
		else if(parameters.getKMax()==this.dataMatrix.getNOfItem())
		{
			tempPath = parameters.getAlgOutputPath()+"OutCLS"+".txt";
			command = "\""+this.parameters.getHAlgPath()+"\""+" "+"\""+parameters.getAlgInputPath()+"\""+" "+Integer.toString(parameters.getKMax())+" "+parameters.getType()+" "+"\""+tempPath+"\"";
			maxNumberOfCluster = parameters.getKMax()-1;
		}
		
		//Precomputing
		ClusterList clusterList = new ClusterList();
		SimilarityMatrix simMatrix = new SimilarityMatrix();
		exitValue = 0;
		try
		{
			exitValue = this.executeAlgorithm(command, tempPath);
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
		
		clusterList.loadFromFile(tempPath);
		simMatrix.loadFromFile(parameters.getAlgOutputPath()+parameters.getOutputHierarchical());
		//End Precomputing

		if(parameters.getKMax()==this.dataMatrix.getNOfItem())
		{
			this.clusterMatrix = clusterList.clusterListToCMatrix(); 
			this.wCSS.setNOfCluster((parameters.getKMax()- parameters.getKMin()), parameters.getKMax());
			this.wCSS.setMeasureValue((parameters.getKMax()- parameters.getKMin()), this.computeW(clusterMatrix));
		}
		
		HLink hierarchicalLink = new HLink();
	    hierarchicalLink.setClusterList(clusterList);
	    hierarchicalLink.setSimilarityMatrix(simMatrix);
		
	    for (int k = maxNumberOfCluster; k >= parameters.getKMin(); k--) //Ciclo con numero di fusioni
		{
	    	simMatrix = hierarchicalLink.hlinkageStep(hierarchicalLink.getSimilarityMatrix(), parameters.getType());
			hierarchicalLink.setSimilarityMatrix(simMatrix);
			this.clusterMatrix = hierarchicalLink.getClusterList().clusterListToCMatrix();
			
			System.out.println("Execute WCSS Hierarchical for "+Integer.toString(k)+" Clusters");
			
			this.wCSS.setNOfCluster((parameters.getKMax()- parameters.getKMin())-(parameters.getKMax()- k), k);
			this.wCSS.setMeasureValue((parameters.getKMax()- parameters.getKMin())-(parameters.getKMax()- k), this.computeW(clusterMatrix));
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
		
		header.setAlgorithmName(this.parameters.getAlgorithmName()); 
		
		stringParam = Integer.toString(this.parameters.getKMin())+"_"+Integer.toString(this.parameters.getKMax()); 
		String algParam = "";
		
		if(this.parameters.getType().contentEquals("A") || this.parameters.getType().contentEquals("a"))
			algParam = "Average Linkage";
		else if(this.parameters.getType().contentEquals("C") || this.parameters.getType().contentEquals("c"))
			algParam = "Complete Linkage";
		else if(this.parameters.getType().contentEquals("S") || this.parameters.getType().contentEquals("s"))
		    algParam = "Single Linkage";
		
		header.setAlgParameters(stringParam+" "+algParam); 
		header.setDatasetName(this.parameters.getDatasetName());
		header.setDatasetType(this.parameters.getDatasetType());	
		header.setDateTime();
		header.setMeasureName(this.MEASURE_NAME);
		header.setMeasParameters(" - ");
		
	return header;
	}
	
}
