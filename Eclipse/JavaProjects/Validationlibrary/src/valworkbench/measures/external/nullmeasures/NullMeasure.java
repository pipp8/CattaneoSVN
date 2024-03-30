package valworkbench.measures.external.nullmeasures;
import java.io.FileNotFoundException;
import java.io.IOException;

import valworkbench.datatypes.ClusterMatrix;
import valworkbench.datatypes.DataMatrix;
import valworkbench.datatypes.HeaderData;
import valworkbench.datatypes.InputMeasure;
import valworkbench.datatypes.MeasureVector;
import valworkbench.measures.Measure;
/**
 * The NullMeasure class provides an object that encapsulates methods and states 
 * informations for computing Null Measure. This class takes in input a Data 
 * Matrix and an InputMeasure object. Values of the measure are equal to <code>NaN</code> and are 
 * returned in a MeasureVector object.
 * The effect of computing such a measure is basically to produce clustering 
 * solutions with the given algorithm A and Benchmark dataset D  
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0
 * @see Measure
 */
public abstract class NullMeasure extends Measure{
	protected InputMeasure parameters;
	protected DataMatrix dataMatrix;
	protected ClusterMatrix clusterMatrix = new ClusterMatrix();
	protected MeasureVector nullValue;
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
	 * @param mParameters input paramters
	 * @see InputMeasure
	 */
	public void setParametersMeasure(InputMeasure mParameters){
		this.parameters = mParameters;
	}
	
	/**
	 * Measure_Vector accessor method
	 * 
	 * @return the measures vector
	 * @see MeasureVector
	 */
	public MeasureVector getNullValue(){
		return this.nullValue;
	}
	
	/**
	 * Clustering solutions obtained through algorithm execution accessor method
	 * 
	 * @param numberSolution the number of clustering solutions required
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
	 * Header_Data accessor Method
	 * 
	 * @return the header data collected in the experiment
	 * @see HeaderData
	 */
	public HeaderData getHeaderData(){
		return this.headerData;
	}
}
