package valworkbench.measures.internal.WCSS;

import java.io.FileNotFoundException;
import java.io.IOException;

import valworkbench.datatypes.ClusterMatrix;
import valworkbench.datatypes.DataMatrix;
import valworkbench.datatypes.HeaderData;
import valworkbench.datatypes.InputMeasure;
import valworkbench.datatypes.MeasureVector;
import valworkbench.measures.Measure;
/**
 * The WithinClustersSumSquares is the base class for all 
 * WCSS measures. A WithinClustersSumSquares Object encapsulates all the 
 * informations needed for a measure. These informations include:
 * <UL>
 * <LI>An InputMeasure object containing all the informations 
 *     needed to compute the within clusters sum of squares
 * <LI>An input Data Matrix
 * <LI>A measure vector containing WCSS values
 * <LI>A ClusterMatrix object used to load 
 * clustering solutions 
 * <LI>A data header containing data collected in the experiment
 * <LI>A matrix of double containing FOM(e, k) values
 * </UL>
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0
 * @see Measure
 */
public abstract class WithinClustersSumSquares extends Measure {
	protected InputMeasure parameters;
	protected DataMatrix dataMatrix;
	protected ClusterMatrix clusterMatrix = new ClusterMatrix();
	protected MeasureVector wCSS;
	protected MeasureVector gWCSS;
	protected MeasureVector bestNumberOfCluster;
	protected HeaderData headerData;
	
	/**
	 * Computes measure values
	 *
	 * @return a Measure_Vector object containing measure values
	 * equal to <code>NaN</code>
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
	 * Calculates the W value for an iteration
	 * @param clusterMatrix a reference to a Cluster_Matrix object obtained from algorithm execution 
	 * @return the W value 
	 */
	public double computeW(ClusterMatrix clusterMatrix)
	{
		double d;
		double W=0;

		for (int k=0; k<clusterMatrix.getNOfCluster(); k++)
		{
			d=0;
			int vector[]=clusterMatrix.getCMatrixRow(k);

			for(int j=0; j<clusterMatrix.getClusterSize(k); j++)
			{
				for (int i=0; i<j; i++)
					d+=euclideanDistance(vector[j],vector[i]);
			}
			
			W+=d/(double)(2*clusterMatrix.getClusterSize(k));
			
		}

		return W;	
	}

	/** 
	 * Computes Euclidean distance for index i and j obtained from Data_Matrix object
	 * 
	 * @param typeDist a String equals to e (= euclidean distance), p (= Pearson Coefficient)
	 * @param i index of row in dataMatrix
	 * @param j index of row in dataMatrix
	 * @return the distance between items i and j obtained from Data_Matrix
	 * */
	protected double euclideanDistance(int i, int j)
	{
		double v1[];
		double v2[];
		double distance = 0;

		v1 = this.dataMatrix.getItemValueRow(i);
		v2 = this.dataMatrix.getItemValueRow(j);

		for (int z = 0; z < v1.length; z++)
		{
			distance += Math.pow((v1[z] - v2[z]), 2);
		}

		return Math.sqrt(distance);
	}
		
	/**
	 * Compute geometric WCSS for prediction of correct number of clusters
	 * 
	 * @param wCSS a Measure_Vector containing aggregate FOM Value
	 * @return a Measure_Vector containing difference between straight line and aggregate FOM value
	 */		
	protected MeasureVector computeGWCSS(MeasureVector wCSS)
	{
		MeasureVector straightLine = new MeasureVector(wCSS.getNOfEntries());
		MeasureVector gapWCSS = new MeasureVector(wCSS.getNOfEntries());
		MeasureVector logWCSS = new MeasureVector(wCSS.getNOfEntries());
		double y1, x1, y2, x2;
				
		for(int i = 0; i < logWCSS.getNOfEntries(); i++)
		{
			logWCSS.setMeasureValue(i, Math.log10(wCSS.getMeasureValue(i)));
			logWCSS.setNOfCluster(i, wCSS.getNOfCluster(i));
		}		
		
		x1 = (double)logWCSS.getNOfCluster(0);
		y1 = logWCSS.getMeasureValue(0)+20;
		x2 = (double)logWCSS.getNOfCluster(logWCSS.getNOfEntries()-1);
		y2 = logWCSS.getMeasureValue(logWCSS.getNOfEntries()-1)+20;
		
		straightLine.setMeasureValue(0, (logWCSS.getMeasureValue(0)+20));
		straightLine.setNOfCluster(0, logWCSS.getNOfCluster(0));
		straightLine.setMeasureValue((logWCSS.getNOfEntries()-1), (logWCSS.getMeasureValue((logWCSS.getNOfEntries()-1))+20));
		straightLine.setNOfCluster((logWCSS.getNOfEntries()-1), logWCSS.getNOfCluster(logWCSS.getNOfEntries()-1));
				
		for(int x = 1; x < logWCSS.getNOfEntries()-1; x++)
		{
			straightLine.setMeasureValue(x, (((y2-y1)/(x2-x1)) * (logWCSS.getNOfCluster(x)-x1) + y1));
			straightLine.setNOfCluster(x, logWCSS.getNOfCluster(x));
		}
						
		for(int i = 0; i < logWCSS.getNOfEntries(); i++)
		{
			gapWCSS.setMeasureValue(i, (straightLine.getMeasureValue(i)-logWCSS.getMeasureValue(i)));
			gapWCSS.setNOfCluster(i, logWCSS.getNOfCluster(i));
		}
			
		return gapWCSS;
	}
	
	/**
	 * Computes the estimated number for Geometric WCSS Method
	 * 
	 * @param gapWCSS a measure vector containing gap FOM values
	 * @return a Measure_Vector with a sinngle entry containing the first maximum 
	 * of the Gap FOM index
	 * @see MeasureVector 
	 */
	public MeasureVector estimatedNumberOfCluster(MeasureVector gapWCSS)
	{
		MeasureVector estimateNCluster = new MeasureVector(1);
		
		int i=0;
		int j=1;
		boolean stop = true;
			
		while(stop)
		{
			if(gapWCSS.getMeasureValue(j) < gapWCSS.getMeasureValue(i))
			{
				estimateNCluster.setMeasureValue(0, gapWCSS.getMeasureValue(i));
				estimateNCluster.setNOfCluster(0, gapWCSS.getNOfCluster(i));
				stop = false;
			}	
			else
			{
				i++;
				j++;
			}
		}
						
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
	public MeasureVector getWCSSValue(){
		return this.wCSS;
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
