package valworkbench.measures.internal.FOM;

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
 * The FOMHierarchical class provides an Object that encapsulates methods and all the
 * informations for computing Figure Of Merit for Hierarhical algorithms. This class has to 
 * be used only for hierarchical clustering methods and allows to reduce the computing time 
 * nedeed to obtain hierarchical clustering solutions 
 * (Average Linkage, Complete Linkage and Single Linkage methods).
 * This measure takes in input a data matrix and a set of parameters collected 
 * through an InputMeasure object. Values of the measure are returned in 
 * a MeasureVector object.
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0
 * @see FigureOfMerit 
 *
 */
public class FOMHierarchical extends FigureOfMerit {
	private final String MEASURE_NAME = "FOM Hierarchical";
	/**
	 * Default constructor method
	 *
	 */
	public FOMHierarchical()
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
	public FOMHierarchical(DataMatrix dataMatrix, InputMeasure mParameters)
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
		int exitValue;
		this.aggregateFOMValue = new MeasureVector((parameters.getKMax()- parameters.getKMin())+1);
		DataMatrix patMatFom = new DataMatrix();
		
		this.fOMekMatrix = new double[(parameters.getKMax()- parameters.getKMin())+1][dataMatrix.getNOfFeature()];
		
		for (int e = 0; e < dataMatrix.getNOfFeature(); e++)
		{
			patMatFom = dataMatrix.removeFeature(e);
			
			patMatFom.storeToFile(parameters.getAlgInputPath());
			
			System.out.println("Execute FOM Hierarchical feature "+e+"\r");
			
			//String tempPath = parameters.getAlgOutputPath()+"OutCLS"+".txt";
			//String command = "\""+this.parameters.getHAlgPath()+"\""+" "+"\""+parameters.getAlgInputPath()+"\""+" "+Integer.toString((parameters.getKMax()+1))+" "+parameters.getType()+" "+"\""+tempPath+"\"";
			 
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
				tempPath = parameters.getAlgOutputPath()+"OutCLS_"+parameters.getKMax()+".txt";
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
				if (parameters.isAdjustmentFactor() & this.adjFactor(this.dataMatrix.getNOfItem(), parameters.getKMax())!=Double.NaN)
					this.fOMekMatrix[(parameters.getKMax()- parameters.getKMin())][e] = this.fOMek(e, parameters.getKMax())/this.adjFactor(this.dataMatrix.getNOfItem(), parameters.getKMax());
				else this.fOMekMatrix[(parameters.getKMax()- parameters.getKMin())][e] = this.fOMek(e, parameters.getKMax());
				if(e == 0)
				{ 
					aggregateFOMValue.setNOfCluster((parameters.getKMax()- parameters.getKMin()), parameters.getKMax());
				}
			}
						
			HLink hierarchicalLink = new HLink();
		    hierarchicalLink.setClusterList(clusterList);
		    hierarchicalLink.setSimilarityMatrix(simMatrix);
											
			for (int k = maxNumberOfCluster; k >= parameters.getKMin(); k--)
			{
				//Writes number of clusters in Measure_Vector
				if (e == 0)
				{
					aggregateFOMValue.setNOfCluster((parameters.getKMax()- parameters.getKMin())-(parameters.getKMax()- k), k);
				}
				
				//Executes Inner Hierarchical algorithm 
				simMatrix = hierarchicalLink.hlinkageStep(hierarchicalLink.getSimilarityMatrix(), parameters.getType());
				hierarchicalLink.setSimilarityMatrix(simMatrix);
				this.clusterMatrix = hierarchicalLink.getClusterList().clusterListToCMatrix(); 
				
				
				if (parameters.isAdjustmentFactor() & this.adjFactor(this.dataMatrix.getNOfItem(), k)!=Double.NaN)
						this.fOMekMatrix[(parameters.getKMax()- parameters.getKMin())-(parameters.getKMax()- k)][e] = this.fOMek(e, k)/this.adjFactor(this.dataMatrix.getNOfItem(), k);
					else this.fOMekMatrix[(parameters.getKMax()- parameters.getKMin())-(parameters.getKMax()- k)][e] = this.fOMek(e, k);
			}
			patMatFom = null;
			
		}
		
		this.aggregateFOMValue.setMeasureValue(this.aggregateFOM());
		this.headerData = this.writeHeader();
		
		this.headerData.storeToFile(parameters.getOutputPath()+File.separator+"Measure_Header.vhf");
		this.aggregateFOMValue.storeToFile(parameters.getOutputPath()+File.separator+"Measure_Values.txt");
				
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
				
		header.setAlgorithmName(this.parameters.getAlgorithmName());
		
		stringParam = Integer.toString(this.parameters.getKMin())+"_"+Integer.toString(this.parameters.getKMax()); 
		
		String algParam = null;
			
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
		if (this.parameters.isAdjustmentFactor())header.setMeasParameters("Adjustment Factor yes");
		else header.setMeasParameters("Adjustment Factor no");
		
		return header;
	}
}
