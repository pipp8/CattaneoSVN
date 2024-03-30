package valworkbench.measures.internal.FOM;

import java.io.FileNotFoundException;
import java.io.IOException;

import valworkbench.datatypes.ClusterMatrix;
import valworkbench.datatypes.DataMatrix;
import valworkbench.datatypes.HeaderData;
import valworkbench.datatypes.InputMeasure;
import valworkbench.datatypes.MeasureVector;
import valworkbench.measures.Measure;
/**
 * The FigureOfMerit is the base class for all FOM measures. 
 * A FigureOfMerit Object encapsulates all the informations 
 * needed for the measure. The information fields are:
 * <UL>
 * <LI>An InputMeasure object containing all the informations 
 *     needed to compute the figure of merit
 * <LI>An input Data Matrix
 * <LI>A measure vector containing aggregate FOM values
 * <LI>A ClusterMatrix object used to load 
 * clustering solution 
 * <LI>A data header containing data collected in the experiment
 * <LI>A matrix of double containing FOM(e, k) values
 * </UL>
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0
 * 
 */
public abstract class FigureOfMerit extends Measure {
	protected InputMeasure parameters;
	protected DataMatrix dataMatrix;
	protected MeasureVector aggregateFOMValue;
	protected MeasureVector gFOM;
	protected MeasureVector bestNumberOfCluster;
	protected ClusterMatrix clusterMatrix = new ClusterMatrix();
	protected HeaderData headerData;
	protected double fOMekMatrix[][];
	/**
	 * Computes Measure values
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	public abstract MeasureVector computeMeasure() throws FileNotFoundException, IOException, NumberFormatException, Exception;
	/**
	 * Collects information about the experiment.
	 * 
	 * @return a reference to a Header_Data object
	 * @see HeaderData
	 */
	protected abstract HeaderData writeHeader();
	/**
	 * Computes figure of merit values on feature e and cluster k
	 * 
	 * @param e analized feature 
	 * @param k number of analized cluster
	 * @return a double value representing figure of merit value 
	 * on feature e and cluster k
	 */
	protected double fOMek(int e, int k)
	{   double featureEOnCi[]; 
		double meanValue;      
		double mSE = 0;
				
		for(int i = 0; i < k ; i++)
		{
			
		    featureEOnCi = new double[clusterMatrix.getClusterSize(i)];
			
		    for (int s = 0; s < clusterMatrix.getClusterSize(i); s++)
		    {
		    	featureEOnCi[s] = dataMatrix.getItemValue(clusterMatrix.getCMatrixValue(i, s), e);
		    }
			
		    meanValue = meanVector(featureEOnCi);
		    
			for (int j = 0; j < clusterMatrix.getClusterSize(i); j++)
			{
				mSE += meanSquareError(featureEOnCi[j], meanValue);
			}
		}
		return Math.sqrt((((double)1/dataMatrix.getNOfItem())* mSE));
	}
	/**
	 * Computes the aggregate FOM values 
	 *
	 * @return a vector of double containing aggregate FOM Values
	 */	
	protected double[] aggregateFOM()
	{
		double aggregateFOM[] = new double[(parameters.getKMax()- parameters.getKMin()+1)];
		
		for(int k = 0; k < (parameters.getKMax()- parameters.getKMin()+1); k++)
		{
			int count = 0; 
			
			for(int e = 0; e < this.dataMatrix.getNOfFeature(); e++)
				if(fOMekMatrix[k][e] != Double.NaN)count += 1;
			
			if(count < ((this.dataMatrix.getNOfFeature()*10)/100))
				aggregateFOM[k]=Double.NaN;
			else 
			{
				double mean = meanVector(fOMekMatrix[k]);
				for(int e = 0; e < this.dataMatrix.getNOfFeature(); e++)
				{
					if (fOMekMatrix[k][e] == Double.NaN)
						fOMekMatrix[k][e] =  mean;
				}
			}
			
			for(int e = 0; e < this.dataMatrix.getNOfFeature(); e++)
			{
				if (aggregateFOM[k] == Double.NaN)break;
				else aggregateFOM[k] += fOMekMatrix[k][e];
			}
 		}
		return aggregateFOM;
	}
	/**
	 * Computes the mean value of vector given in input
	 * 
	 * @param vector a vector of double 
	 * @return  mean value
	 */
	protected double meanVector(double vector[])
	{
		double meanValue = 0;
		double sum = 0;
		
		for(int i = 0; i < vector.length; i++)
		{
			sum += vector[i];
		}
		meanValue = (double) sum /vector.length;
		return meanValue; 
	}
	/**
	 * Computes the mean square error
	 * 
	 * @param expLevel the expression level
	 * @param meanExpLevel the mean value of expression level on cluster Ci
	 * @return
	 */
	protected double meanSquareError(double expLevel, double meanExpLevel)
	{
		return Math.pow((expLevel - meanExpLevel), 2);
	}
	/**
	 * Computes the adjustment factor; if the number of elements in data matrix and the 
	 * number of cluster are equal, returns the <code>NaN</code> value 
	 * 
	 * @param n number of elements in data Matrix
	 * @param k number of clusters 
	 * @return the Adjustment Factor 
	 */	
	protected double adjFactor(int n, int k)
	{  
		double adjustedValue;
		
		if (parameters.isAdjustmentFactor())
		{
			adjustedValue = (double)Math.sqrt((double)(n - k)/n);
			if (adjustedValue == 0)adjustedValue = Double.NaN;
		}
		else 
		{
			adjustedValue = Double.NaN;
		}
		return adjustedValue;  
	}
	/**
	 * Compute geometric FOM for prediction of correct number of clusters
	 * 
	 * @param aggFOM a Measure_Vector containing aggregate FOM Value
	 * @return a Measure_Vector containing difference between straight line and aggregate FOM value
	 */		
	protected MeasureVector computeGFOM(MeasureVector aggFOM)
	{
		MeasureVector straightLine = new MeasureVector(aggFOM.getNOfEntries());
		MeasureVector gapFOM = new MeasureVector(aggFOM.getNOfEntries());
		MeasureVector logAggFOM = new MeasureVector(aggFOM.getNOfEntries());
		double y1, x1, y2, x2;
				
		for(int i = 0; i < logAggFOM.getNOfEntries(); i++)
		{
			logAggFOM.setMeasureValue(i, Math.log10(aggFOM.getMeasureValue(i)));
			logAggFOM.setNOfCluster(i, aggFOM.getNOfCluster(i));
		}
				
		x1 = (double)logAggFOM.getNOfCluster(0);
		y1 = logAggFOM.getMeasureValue(0)+20;
		x2 = (double)logAggFOM.getNOfCluster(logAggFOM.getNOfEntries()-1);
		y2 = logAggFOM.getMeasureValue(logAggFOM.getNOfEntries()-1)+20;
		
		straightLine.setMeasureValue(0, (logAggFOM.getMeasureValue(0)+20));
		straightLine.setNOfCluster(0, logAggFOM.getNOfCluster(0));
		straightLine.setMeasureValue((logAggFOM.getNOfEntries()-1), (logAggFOM.getMeasureValue((logAggFOM.getNOfEntries()-1))+20));
		straightLine.setNOfCluster((logAggFOM.getNOfEntries()-1), logAggFOM.getNOfCluster(logAggFOM.getNOfEntries()-1));
				
		for(int x = 1; x < logAggFOM.getNOfEntries()-1; x++)
		{
			straightLine.setMeasureValue(x, (((y2-y1)/(x2-x1)) * (logAggFOM.getNOfCluster(x)-x1) + y1));
			straightLine.setNOfCluster(x, logAggFOM.getNOfCluster(x));
		}
		
		for(int i = 0; i < aggFOM.getNOfEntries(); i++)
		{
			gapFOM.setMeasureValue(i, (straightLine.getMeasureValue(i)-logAggFOM.getMeasureValue(i)));
			gapFOM.setNOfCluster(i, logAggFOM.getNOfCluster(i));
		}
			
		return gapFOM;
	}
	/**
	 * Computes the estimated number for Geometric FOM Method
	 * 
	 * @param gapFOM a measure vector containing gap FOM values
	 * @return a Measure_Vector with a sinngle entry containing the first maximum 
	 * of the Gap FOM index
	 * @see MeasureVector 
	 */
	public MeasureVector estimatedNumberOfCluster(MeasureVector gapFOM)
	{
		MeasureVector estimateNCluster = new MeasureVector(1);
		
		int i=0;
		int j=1;
		boolean stop = true;
			
		while(stop)
		{
			if(gapFOM.getMeasureValue(j) < gapFOM.getMeasureValue(i))
			{
				estimateNCluster.setMeasureValue(0, gapFOM.getMeasureValue(i));
				estimateNCluster.setNOfCluster(0, gapFOM.getNOfCluster(i));
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
	 * @param dataMatrix input data matrix
	 * @see DataMatrix
	 */
	public void setInputPMatrix(DataMatrix dataMatrix)
	{
		this.dataMatrix = dataMatrix;
	}
	/**
	 * Input_Measure mutator method
	 *  
	 * @param mParameters an Input_Measure object
	 * @see InputMeasure
	 */
	public void setParametersMeasure(InputMeasure mParameters)
	{
		this.parameters = mParameters;
	}
	/**
	 * Measure_Vector accessor method
	 * 
	 * @return a reference to a Measure_Vector object containing aggregate FOM values
	 * @see MeasureVector
	 */
	public MeasureVector getAggregateFOMValue()
	{
		return this.aggregateFOMValue;
	}
	/**
	 * Header_Data accessor Method
	 * 
	 * @return a reference to a Header_Data object
	 * @see HeaderData
	 */
	public HeaderData getHeaderData()
	{
		return this.headerData;
	}
		
	
}
