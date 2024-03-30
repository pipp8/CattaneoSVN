package valworkbench.measures;

import java.io.FileNotFoundException;
import java.io.IOException;

import valworkbench.datatypes.ClusterMatrix;
import valworkbench.datatypes.DataMatrix;
import valworkbench.datatypes.HeaderData;
import valworkbench.datatypes.InputMeasure;
import valworkbench.datatypes.MeasureVector;
/** 
 ** The Silhouette class provides an object that encapsulates methods 
 * and states information for computing Silhouette Index. This class 
 * takes in input a Data Matrix and an InputMeasure object. 
 * Measure values are returned in a Measure_Vector  
 * object with values ranging between 1 and -1.
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @see MeasureVector
 * @version 1.0
 */
public abstract class Silhouette extends Measure {
	protected InputMeasure parameters;
	protected DataMatrix dataMatrix;
	protected MeasureVector silhouetteValue;
	protected ClusterMatrix clusterMatrix = new ClusterMatrix();
	protected HeaderData headerData;
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
	 * 
	 * @param itemIndex
	 * @param clusterSize
	 * @param clusterItems
	 * @return
	 */
	protected double computeAg(int clusterIndex, int clusterSize, int clusterItems[])
	{
		double agValue = 0;
		
		for(int i = 0; i < (clusterSize -1);  i++)
		{
			if(i != clusterIndex)
			{
				agValue += this.euclideanDistance(clusterItems[clusterIndex], clusterItems[i]);
			}
		}
		agValue = agValue / (clusterSize - 1);
						
		return agValue;		
	}
	/**
	 * 
	 * @param itemIndex
	 * @param nnClusterSize
	 * @param nnClusterItems
	 * @param inClusterItems
	 * @return
	 */
	protected double computegCljDist(int itemIndex, int nnClusterSize, int nnClusterItems[], int inClusterItems[])
	{
		double gCljDist = 0;
				
		for(int i = 0; i < nnClusterSize; i++)
		{
			 gCljDist += this.euclideanDistance(inClusterItems[itemIndex], nnClusterItems[i]);
		}
		
		gCljDist = gCljDist / nnClusterSize;
		
		return gCljDist;
	}
	/**
	 * 
	 * @param nOfCluster
	 * @param itemIndex
	 * @param nnClusterSize
	 * @param nnClusterItems
	 * @param inClusterItems
	 * @return
	 */
	protected double computeBg(int itemIndex, ClusterMatrix clusterSolution, int clusterAffiliation)
	{
		double gClusterDistances[] = new double[clusterSolution.getNOfCluster()];
		double minClusterDistance;
				
		for(int i = 0; i < clusterSolution.getNOfCluster(); i++)
		{
			if(i!=clusterAffiliation)
			{
				gClusterDistances[i] = this.computegCljDist(itemIndex, clusterSolution.getClusterSize(i), clusterSolution.getCMatrixRow(i), clusterSolution.getCMatrixRow(clusterAffiliation));
			}
			
		}
		
		//Extraction of minimum value
		minClusterDistance = gClusterDistances[0];
		for(int j = 0; j < clusterSolution.getNOfCluster(); j++)
		{
			if(j != clusterAffiliation)
			{
				if((minClusterDistance == 0) || (minClusterDistance > gClusterDistances[j]))
				{
					minClusterDistance = gClusterDistances[j];
				}
			}
		}
		return minClusterDistance;
	}
	/**
	 * 
	 * @param clusterSolution
	 * @param itemIndex
	 * @return
	 */
	protected double computeSilhouetteG(ClusterMatrix clusterSolution, int itemIndex)
	{		
		int clusterAffiliation = 0;
		int clusterIndex = 0;
		double silhouetteG=0, aG, bG, maxAgBg;
		boolean stop = false;
        		
		//Computes cluster Affiliation for item itemIndex
		for(int i = 0; i < clusterSolution.getNOfCluster(); i++)
		{
			for(int j = 0; j < clusterSolution.getClusterSize(i); j++)
			{
				if(itemIndex == clusterSolution.getCMatrixValue(i, j))
				{
					clusterAffiliation = i;
					clusterIndex = j;
					stop = true;
				}
				if (stop) break;
			}
			if (stop)break;
		}
		
		if(clusterSolution.getClusterSize(clusterAffiliation)==1)
		{
			silhouetteG = 0;
		}
		else
		{
			aG = this.computeAg(clusterIndex, clusterSolution.getClusterSize(clusterAffiliation), clusterSolution.getCMatrixRow(clusterAffiliation));
			bG = this.computeBg(clusterIndex, clusterSolution, clusterAffiliation);
			
			if(aG > bG)
			{
				maxAgBg = aG;
			    silhouetteG = ((double) (bG - aG) / maxAgBg);
			}
			else if (bG > aG)
			{
				maxAgBg = bG;
				silhouetteG = ((double) (bG - aG) / maxAgBg);
			}
				
		}
			
		return silhouetteG;
	}
	/**
	 * 
	 * @param clusterSolution
	 * @param dataMatrix
	 * @return
	 */
	protected double computeAverageSilhouette(ClusterMatrix clusterSolution, DataMatrix dataMatrix)
	{
		double silhouette = 0;
		
		for(int i = 0; i < dataMatrix.getNOfItem(); i++)
		{
			silhouette += this.computeSilhouetteG(clusterSolution, i);
		}
		
		silhouette = silhouette / dataMatrix.getNOfItem();
		
		return silhouette;
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
	 * @return a reference to a Measure_Vector object containing Silhouette values
	 * @see MeasureVector
	 */
	public MeasureVector getAggregateFOMValue()
	{
		return this.silhouetteValue;
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
