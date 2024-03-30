package valworkbench.measures.internal.KL;

import java.io.FileNotFoundException;
import java.io.IOException;

import valworkbench.datatypes.ClusterMatrix;
import valworkbench.datatypes.DataMatrix;
import valworkbench.datatypes.HeaderData;
import valworkbench.datatypes.InputMeasure;
import valworkbench.datatypes.MeasureVector;
import valworkbench.measures.Measure;
/**
 * The KrzanowskiLai is the base class for all KL measures. 
 * A KrzanowskiLai Object encapsulates all the informations 
 * needed for the measure. The information fields are:
 * <UL>
 * <LI>An InputMeasure object containing all the informations 
 *     needed to compute the Krzanowski and Lai Measure
 * <LI>An input Data Matrix
 * <LI>A ClusterMatrix object used to load 
 * clustering solution 
 * <LI>A data header containing data collected in the experiment
 * </UL>
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0 
 * 
 */
public abstract class KrzanowskiLai extends Measure {
	protected InputMeasure parameters;
	protected DataMatrix dataMatrix;
	protected ClusterMatrix clusterMatrix = new ClusterMatrix();
	protected MeasureVector intIndex;
	protected MeasureVector kLk;
	protected MeasureVector estimateNCluster;
	protected HeaderData headerData;
	
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
	public abstract MeasureVector computeMeasure() throws FileNotFoundException, IOException, NumberFormatException, Exception; 
	
	/**
	 * Collects informations about the experiment.
	 * 
	 * @return the header data for the experiment
	 * @see HeaderData
	 */
	protected abstract HeaderData writeHeader();
	
	/**
	 * Computes the pooled difference between within clusters 
	 * sum of squares values
	 * 
	 * @param intIndex a measure vector containing an internal 
	 * measure index
	 * @see MeasureVector
	 * @return a vector of double containing the difference between 
	 * within clusters sum of squares values
	 */
	protected double[] computeDiffK(MeasureVector intIndex, int p)
	{
		double diffK[] = new double[intIndex.getNOfEntries()-1];
				
		for(int i=0; i<(intIndex.getNOfEntries()-1); i++)
		{
			diffK[i]=((Math.pow((intIndex.getNOfCluster(i)), ((double)2/p)) * intIndex.getMeasureValue(i))
					-(Math.pow(intIndex.getNOfCluster(i+1),  ((double)2/p)) * intIndex.getMeasureValue(i+1)));
		}
		return diffK; 
	}
	
	/**
	 * Computes the Krzanowski and Lai index
	 * 
	 * @param wCSSK a measure vector containing within 
	 * clusters sum of squares values
	 * @see MeasureVector
	 * @return a Measure_Vector containing the Krzanowski and Lai index values
	 */
	protected MeasureVector computeKL(MeasureVector wCSSK, int nOfFeatures)
	{
		double diffK[];
		MeasureVector kLk = new MeasureVector(wCSSK.getNOfEntries()-2);
		diffK = this.computeDiffK(wCSSK, nOfFeatures);
			
		for(int i = 0; i<kLk.getNOfEntries(); i++)
		{
			kLk.setNOfCluster(i, wCSSK.getNOfCluster(i+1));
			kLk.setMeasureValue(i, (diffK[i]/diffK[i+1]));
		}
			
		return kLk;
	}
	
	/**
	 * Computes the estimated number of clusters for the Krzanowski and Lai index
	 * 
	 * @param kLk a measure vector containing Krzanowski and Lai index values
	 * @return a Measure_Vector with a sinngle entry containing the maximum value 
	 * of the Krzanowski and Lai index
	 * @see MeasureVector
	 */
	public MeasureVector estimatedNumberOfCluster(MeasureVector kLk)
	{
		MeasureVector estimateNCluster = new MeasureVector(1);
		estimateNCluster.setNOfCluster(0, kLk.getMaxMeasureValueNOfCluster());
		estimateNCluster.setMeasureValue(0, kLk.getMaxMeasureValue());
		return estimateNCluster;
	}
		
	/**
	 * Data_Matrix mutator method
	 * 
	 * @param dataMatrix the input data matrix
	 * @see DataMatrix
	 */
	public void setInputPMatrix(DataMatrix dataMatrix){
		this.dataMatrix = dataMatrix;
	}
	
	/**
	 * Input_Measure mutator method
	 *  
	 * @param mParameters input parameters
	 * @see InputMeasure
	 */
	public void setParametersMeasure(InputMeasure mParameters){
		this.parameters = mParameters;
	}
		
	/**
	 * Measure_Vector accessor method
	 * 
	 * @return a reference to a Measure_Vector object
	 * @see MeasureVector
	 */
	public MeasureVector getKLValue(){
		return this.kLk;
	}
		
	/**
	 * Header_Data accessor Method
	 * 
	 * @return the header data collected in the experiment
	 * @see HeaderData
	 */
	public HeaderData getHeaderData(){
		return this.headerData;
	}
}
