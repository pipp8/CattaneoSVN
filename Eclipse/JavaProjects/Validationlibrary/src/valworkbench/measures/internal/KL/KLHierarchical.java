package valworkbench.measures.internal.KL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import valworkbench.datatypes.DataMatrix;
import valworkbench.datatypes.HeaderData;
import valworkbench.datatypes.InputMeasure;
import valworkbench.datatypes.MeasureVector;
import valworkbench.measures.internal.WCSS.WCSSHierarchical;

public class KLHierarchical extends KrzanowskiLai {
	private final String MEASURE_NAME = "KL Hierarchical";
	/**
	 * Default constructor
	 *
	 */
	public KLHierarchical()
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
	public KLHierarchical(DataMatrix dataMatrix, InputMeasure mParameters)
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
		int upperLimit = parameters.getKMax();
		int lowerLimit = parameters.getKMin();
		parameters.setKMin(lowerLimit-1);
		parameters.setKMax(upperLimit+1);
				
		this.intIndex = new MeasureVector((parameters.getKMax()- parameters.getKMin())+1);
		this.estimateNCluster = new MeasureVector(1);
			
		WCSSHierarchical wCSSHier = new WCSSHierarchical(dataMatrix, parameters);
		this.intIndex = wCSSHier.computeMeasure();
		parameters.setKMin(lowerLimit);
		parameters.setKMax(upperLimit);
		
		File deleter = new File(parameters.getOutputPath()+File.separator+"Measure_Header.vhf");
		deleter.delete();
		deleter = new File(parameters.getOutputPath()+File.separator+"Measure_Values.txt");
		deleter.delete();
		
		System.out.println("Compute KL index");
		this.kLk = this.computeKL(this.intIndex, this.dataMatrix.getNOfFeature());
		System.out.println("Estimate number of clusters");
		this.estimateNCluster = this.estimatedNumberOfCluster(this.kLk);
		this.headerData = this.writeHeader();
		this.headerData.storeToFile(parameters.getOutputPath()+File.separator+"Measure_Header.vhf");
		this.kLk.storeToFile(parameters.getOutputPath()+File.separator+"Measure_Values.txt");
		this.estimateNCluster.storeToFile(parameters.getOutputPath()+File.separator+"nOfCluster_Value.txt");	
		
		return kLk;
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
