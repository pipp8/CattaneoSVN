package valworkbench.measures.internal.Gap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

import valworkbench.datatypes.ClusterMatrix;
import valworkbench.datatypes.DataMatrix;
import valworkbench.datatypes.HeaderData;
import valworkbench.datatypes.InputMeasure;
import valworkbench.datatypes.MeasureVector;
import valworkbench.measures.Measure;
import valworkbench.nullmodels.SingularValueDecomposition;
/**
 * The GapStatistics is the base class for all Gap statistics measures. 
 * A Gap_Statistics Object encapsulates all the informations 
 * needed for the measure. The information fields are:
 * <UL>
 * <LI>An InputMeasure object containing all the informations 
 *     needed to compute the Gap Statistics
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
public abstract class GapStatistics extends Measure {
	protected InputMeasure parameters;
	protected DataMatrix dataMatrix;
	protected ClusterMatrix clusterMatrix = new ClusterMatrix();
	protected MeasureVector gapValue;
	protected HeaderData headerData;
	protected MeasureVector wK;
	protected MeasureVector wbK;
	protected MeasureVector gapRun;
	protected double wCSSK[];
	protected double wCSSbK[][];
	protected double bounds[][];
	protected double decomposition[][];
		
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
	 * Collects information about the experiment.
	 * 
	 * @return the header data for the experiment
	 * @see HeaderData
	 */
	protected abstract HeaderData writeHeader();
	
	/**
	 * initialize resample
	 * @param resampleType a String indicating the resample method to use
	 *//*
	protected void initializeResample(String resampleType)
	{
		if(resampleType.equals("c"))
		{
		}
		else if(resampleType.equals("m"))
		{
			this.bounds=this.boundingExtraction();
		}
		else if(resampleType.equals("p"))
		{
			double[][] X = dataMatrix.getItemValue();
			double [] mean = new double[dataMatrix.getNOfFeature()];
			
			//Set Feature Mean to zero
			for(int i=0; i<dataMatrix.getNOfFeature(); i++)
				mean[i]=mean(dataMatrix.getItemValueColumn(i));
			for (int i=0 ; i<dataMatrix.getNOfItem();i++)
				for (int j=0; j < dataMatrix.getNOfFeature(); j++)
					X[i][j]-=mean[j];
			
			//Singular Decomposition
			SingularValueDecomposition matrix=new SingularValueDecomposition(X, this.dataMatrix.getNOfItem(),this.dataMatrix.getNOfFeature());
			this.decomposition= new double[this.dataMatrix.getNOfFeature()][this.dataMatrix.getNOfFeature()];
			double [][]V=matrix.getV();
			
			//Generate Vt (in decomposition) from V
			for (int i=0; i< this.dataMatrix.getNOfFeature(); i++)
				for (int j=0; j<this.dataMatrix.getNOfFeature();j++)
					this.decomposition[i][j]=V[j][i];
			
			//Compute X'
			DataMatrix Xp = new DataMatrix(this.dataMatrix.getNOfItem(),this.dataMatrix.getNOfFeature());

	        //   Multiplication  C =A x B
	        for(int i=0; i<this.dataMatrix.getNOfItem(); i++){
	            for(int j=0; j<this.dataMatrix.getNOfFeature(); j++){
	            	double tmp=0;
	                for(int k=0; k<this.dataMatrix.getNOfFeature(); k++){
	                	tmp+= X[i][k]*V[k][j];
	                }
	                Xp.setItemValue(i, j, tmp);
	            }
	        }
	       
	        //Draw uniform feature z' with method (a) on X'
	        this.bounds  = new double[Xp.getNOfFeature()][2];
			
			for(int i = 0; i < Xp.getNOfFeature(); i++)
			{
				bounds[i][0] = Xp.extractColumnMin(i);
				bounds[i][1] = Xp.extractColumnMax(i);
			}
	        
		}
	}
	
	
	*//**
	 * resamples data using Monte Carlo or permutational method on Data_Matrix object
	 * 
	 * @param resampleType a String indicating the resample method to use
	 * @return a reference to a resampled Data_Matrix object 
	 *//*
	*//**
	 * @param resampleType
	 * @return
	 *//*
	protected DataMatrix resample(String resampleType)
	{
		DataMatrix dataMatrix = null;
		
		if(resampleType.equals("c"))
		{
			dataMatrix = this.columnSampling();
			try
			{
				dataMatrix.storeToFile(this.parameters.getAlgOutputPath()+"DataSampling.txt");
			}
			catch(Exception exc)
			{
				System.out.println("An error occurred!");
				System.exit(0);
			}
		}
		else if(resampleType.equals("m"))
		{
			dataMatrix = this.monteCarloSampling(this.bounds);
		}
		else if(resampleType.equals("p"))
		{
			dataMatrix = this.principalComponentsSampling();
		}
		return dataMatrix; 
	}
		
	*//**
	 * Extracts bounds values from Data_Matrix for applying Monte Carlo resample method 
	 * 
	 * @return a double matrix containing bounds value
	 *//*
	*//**
	 * @return
	 *//*
	protected double[][] boundingExtraction()
	{
		double bounds[][] = new double[this.dataMatrix.getNOfFeature()][2];
		
		for(int i = 0; i < this.dataMatrix.getNOfFeature(); i++)
		{
			bounds[i][0] = this.dataMatrix.extractColumnMin(i);
			bounds[i][1] = this.dataMatrix.extractColumnMax(i);
		}
		
		return bounds;  
	}
	
	*//**
	 * Computes the Monte Carlo resample method
	 * 
	 * @param bounds a matrix of double containing bounds value
	 * @return a reference to a resampled Data_Matrix object
	 *//*
	*//**
	 * @param bounds
	 * @return
	 *//*
	protected DataMatrix monteCarloSampling(double bounds[][])
	{
		DataMatrix dataMatrix = new DataMatrix(this.dataMatrix.getNOfItem(), this.dataMatrix.getNOfFeature());
		double sigmaValue;
		
		for(int i = 0; i < this.dataMatrix.getNOfItem(); i++)
		{
			for(int j = 0; j < this.dataMatrix.getNOfFeature(); j++)
			{
				sigmaValue = Math.random();
				dataMatrix.setItemValue(i, j, (bounds[j][0] + ((bounds[j][1] - bounds[j][0]) * sigmaValue)));
			}
		}
		return dataMatrix;
	}
	*//**
	 * Computes the Principal Compinents resample method
	 * 
	 * @param dataMatrix is a original data matrix
	 * @return a reference to a resampled Data_Matrix object
	 * @see DataMatrix
	 *//*
	*//**
	 * @return
	 *//*
	protected DataMatrix principalComponentsSampling()
	{
	
		DataMatrix Zp= this.monteCarloSampling(this.bounds);      
		
        //Compute Z=Z' x Vt
		DataMatrix Z = new DataMatrix(this.dataMatrix.getNOfItem(), this.dataMatrix.getNOfFeature());
       
        for(int i=0; i<Z.getNOfItem(); i++){
            for(int j=0; j<Z.getNOfFeature(); j++){
            	double tmp=0;
                for(int k=0; k<Z.getNOfFeature(); k++){
                	tmp += Zp.getItemValue(i, k)*this.decomposition[k][j];
                }
                Z.setItemValue(i, j,tmp);
            }
        } 
        // give reference data Z       
		return Z;
	}
	
	*//**
	 * Computes the permutational resample method
	 * @return a reference to a resampled Data_Matrix object
	 *//*
	*//**
	 * @return
	 *//*
	protected DataMatrix columnSampling()
	{
		DataMatrix dataMatrix = new DataMatrix(this.dataMatrix.getNOfItem(), this.dataMatrix.getNOfFeature());
		double row[] = new double[this.dataMatrix.getNOfFeature()];
			
		for(int i = 0; i < this.dataMatrix.getNOfItem(); i++)
		{
			row = this.dataMatrix.getItemValueRow(i);
			for(int j = 0; j < this.dataMatrix.getNOfFeature(); j++)
			{
				int r = (int)(Math.random() * (j+1));    // int between 0 and j
			    double swap = row[r];
				row[r] = row[j];
				row[j] = swap;
			}
			dataMatrix.setItemValueRow(i, row);
		}
		return dataMatrix;
	}
*/	/**
	 * Computes the mean value on WCSSk vector
	 * 
	 * @param wCSSk a vector of double containing wCSSk values
	 * @return a double representing the computed mean value
	 */
	protected double computeMean(double wCSSk[])
	{
		double mean = 0;
	    for(int i = 0; i < wCSSk.length; i++)
	    {
	    	mean += wCSSk[i];
	    }
	    mean = (double)  mean / wCSSk.length;
	    return mean;
	}
	
	/**
	 * Computes the standard deviation on wCSSk values
	 * 
	 * @param mean the mean value on wCSSK vector
	 * @param wCSSK a vector of double containing wCSSk values
	 * @return a double representing the computed standard deviation
	 */
	protected double computeStandardDeviation(double mean, double wCSSK[])
	{
		double mSE = 0; 
		double standardDeviation = 0;				
		for(int i = 0; i < wCSSK.length; i++)
		{
			mSE += Math.pow((wCSSK[i] - mean), 2); 
		}
		standardDeviation = Math.sqrt((double) mSE / wCSSK.length);
		
		return standardDeviation; 
	}
		
	
	/**
	 * Computes the Sk value on WCSSk vector
	 * 
	 * @param wCSSk a vector of double containing wCSSk values
	 * @return the Sk value
	 */
	protected double computeSk(double wCSSK[])
	{
		return (computeStandardDeviation(this.computeMean(wCSSK), wCSSK) * (Math.sqrt( 1 + ((double) 1 / this.wCSSbK.length))));
	}
	
	/**
	 * Computes the Gap Statistics on wCSSk vector
	 * 
	 * @param wCSSk a vector of double containing wCSSk values
	 * @param wCSSiK a vector of double containing wCSSik values
	 */
	protected void computeGapStatistics(double wCSSK[])
	{
		for(int k = 0; k < wCSSK.length; k++)
		{
			double column[] = this.getColumnWCSSbK(k);
			double meanWCSSK = this.computeMean(column);
			
			//Handling Missing Value
			if(wCSSK[k] != Double.NaN)
			{  	
				this.gapValue.setMeasureValue(k, (meanWCSSK - wCSSK[k]));
				this.gapValue.setNOfCluster(k, (k + this.parameters.getKMin()));
			}
			else
			{
				this.gapValue.setMeasureValue(k, Double.NaN);
				this.gapValue.setNOfCluster(k, (k + this.parameters.getKMin()));
			}
		}
	}
			
	/**
	 * Selects the number of clusters with the best gap value
	 * 
	 * @param wCSSk a vector of double containing wCSSk values
	 * @return an integer representing the best gap value
	 */
	protected int selectBestK()
	{
		int bestK = 0; 
		for(int k = 0; k < (this.gapValue.getSizeMVector()-1); k++)
		{
			if((this.gapValue.getMeasureValue(k) != Double.NaN) &  (this.gapValue.getMeasureValue(k+1) != Double.NaN))
			{
				if(this.gapValue.getMeasureValue(k) >= 
					(this.gapValue.getMeasureValue(k+1) - (this.computeSk(this.getColumnWCSSbK(k+1)))))
				{
					bestK = k;
					break;
				}
			}
		}
		return bestK;
	}
		
	/**
	 * wCSSbK row accessor method
	 * 
	 * @param rowWCSSbK a row index for wCSSbk matrix
	 * @return a vector of double representing the required row from wCSSbk matrix
	 */
	protected double[] getRowWCSSbK(int rowWCSSbK)
	{
		double row[] = new double[(this.parameters.getKMax()-this.parameters.getKMin())+1];
		
		for(int k = 0; k < ((this.parameters.getKMax() - this.parameters.getKMin())+1); k++)
		{
			row[k] = this.wCSSbK[rowWCSSbK][k];
		}
		return row;
	}
	
	/**
	 * wCSSbK column accessor method
	 * 
	 * @param columnWCSSbK a column index for wCSSbk matrix
	 * @return a vector of double representing the required column from wCSSbk matrix
	 */
	protected double[] getColumnWCSSbK(int columnWCSSbK)
	{
		double column[] = new double[this.parameters.getNumberOfIteration()];
		
		for(int b = 0; b < this.parameters.getNumberOfIteration(); b++)
		{
			column[b] = this.wCSSbK[b][columnWCSSbK];
		}
		return column;
	}
	
	/**
	 * Stores the best gap and the number of clusters values in a file 
	 * 
	 * @param fName the name of the file to be stored
	 * @param bestK the best number of clusters
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */
	protected void storeBestGap(String fName, int bestK) throws FileNotFoundException, IOException, Exception
	{
		File bestGap = new File(fName);
		
		FileWriter stInputW = new FileWriter(bestGap);
		BufferedWriter bwInput = new BufferedWriter(stInputW);
		bwInput.write("Best K Value is : "+this.gapValue.getNOfCluster(bestK));
		bwInput.newLine();
		bwInput.write("Best Gap Value is : "+this.gapValue.getMeasureValue(bestK));
		bwInput.newLine();
		bwInput.close();
		stInputW.close();
	}

	/**
	 * Loads the best gap and the number of clusters values from a file 
	 * 
	 * @param runNumber an integer indicating the run number to be loaded from a file
	 * @return a integer representing the best number of clusters value
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	protected int loadBestGap(int runNumber) throws FileNotFoundException, 
	IOException, NumberFormatException, Exception
	{
		File bestGap = new File(this.parameters.getAlgOutputPath()+"BestGap_Run_"+Integer.toString(runNumber)+".txt");
		
		int bestK;
		FileReader stOutputR = new FileReader(bestGap);
		BufferedReader brOutput = new BufferedReader(stOutputR);
		String temp = brOutput.readLine();
		StringTokenizer st = new StringTokenizer(temp);
		for(int i = 0; i < 5; i++)
		{
			st.nextToken();
		}
		bestK = Integer.parseInt(st.nextToken());
		return bestK;
	} 
	
	
	/**
	 * Gap Measure_Vector accessor method
	 * 
	 * @param runNumber an integer representing the run number to be loaded from a file
	 * @return a reference to a Measure_Vector object containing Gap value data
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	public MeasureVector getGapMeasureVector(int runNumber) throws FileNotFoundException, 
	IOException, NumberFormatException, Exception
	{
		MeasureVector measureVector = new MeasureVector();
		String path = this.parameters.getOutputPath()+"GapValues_Run_"+Integer.toString(runNumber)+".txt";
		measureVector.loadFromFile(path);
		return measureVector;
	}
	
	/**
	 * wCSSbK accessor method
	 * 
	 * @return a reference to a Double matrix containing WCSSbk values
	 */
	public double[][] getWCSSbK(){
		return this.wCSSbK;
	}
		
	/**
	 * Data_Matrix mutator method
	 *  
	 * @param dataMatrix input data matrix
	 * @see DataMatrix
	 */
	public void setInputDMatrix(DataMatrix dataMatrix){
		this.dataMatrix = dataMatrix;
	}
	
	/**
	 * Input_Measure mutator method 
	 *  
	 * @param mParameters a reference to an Input_Measure object
	 * @see InputMeasure
	 */
	public void setParametersMeasure(InputMeasure mParameters){
		this.parameters = mParameters;
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
	
	
	/**
	 * Compute the mean of the value
	 * @param x vector of input
	 * @return the mean of the value in x 
	 */
	/**
	 * @param x
	 * @return
	 */
	private double mean(double []x)
	{
		double sum=0;
		for(int i=0;i<x.length;i++)
			sum+=x[i];
		if (x.length!=0)
			return (sum/((double)x.length));
		else
			return Double.NaN;
	}

		
}
