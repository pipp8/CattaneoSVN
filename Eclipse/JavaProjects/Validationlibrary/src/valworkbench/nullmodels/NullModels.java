package valworkbench.nullmodels;

import valworkbench.datatypes.DataMatrix;
/**
 * The NullModels class provides object and state information to 
 * compute a null model resamples data over a specific 
 * DataMatrix 
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0
 */
public class NullModels {
	public double bounds[][];
	public DataMatrix dataMatrix;
	public double decomposition[][];
	public String resampleType;
	public String algPath; 
	
	/**
	 * Default class Constructor
	 * 
	 */
	public NullModels(){
		//Empty Constructor Method
	}
		
	/**
	 * Class constructor specifying Data Matrix, decomposition vector
	 * type of resample, and algorithm path's 
	 *  
	 * @param dataMatrix the data matrix used for the resample
	 * @param decomposition the decomposition vector
	 * @param resampleType the type of resample
	 * @param algPath the algorithm path's
	 */
	public NullModels(DataMatrix dataMatrix, double decomposition[][], String resampleType, String algPath)
	{
		this.dataMatrix = dataMatrix;
		this.decomposition = decomposition;
		this.resampleType = resampleType;
		this.algPath = algPath;
		this.initializeResample();
	}
	/**
	 * Allows to initialize the resampling process
	 * 
	 * @param resampleType a String indicating the resample method to use
	 */
	private void initializeResample()
	{
		if(this.resampleType.equals("c"))
		{
		}
		else if(this.resampleType.equals("m"))
		{
			this.bounds=this.boundingExtraction();
		}
		else if(this.resampleType.equals("p"))
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
	/**
	 * Computes resamples using Monte Carlo or permutational method on dataMatrix object
	 * 
	 * @return a reference to a resampled dataMatrix object 
	 */
	public DataMatrix resample()
	{
		DataMatrix dataMatrix = null;
		
		if(this.resampleType.equals("c"))
		{
			dataMatrix = this.columnSampling();
			try
			{
				dataMatrix.storeToFile(this.algPath+"DataSampling.txt");
			}
			catch(Exception exc)
			{
				System.out.println("An error occurred!");
				System.exit(0);
			}
		}
		else if(this.resampleType.equals("m"))
		{
			dataMatrix = this.monteCarloSampling(this.bounds);
		}
		else if(this.resampleType.equals("p"))
		{
			dataMatrix = this.principalComponentsSampling();
		}
		return dataMatrix; 
	}
		
	/**
	 * Extracts bounds from dataMatrix to define rectangular resample area
	 * 
	 * @return a double matrix containing bounds value
	 */
	public double[][] boundingExtraction()
	{
		double bounds[][] = new double[this.dataMatrix.getNOfFeature()][2];
		
		for(int i = 0; i < this.dataMatrix.getNOfFeature(); i++)
		{
			bounds[i][0] = this.dataMatrix.extractColumnMin(i);
			bounds[i][1] = this.dataMatrix.extractColumnMax(i);
		}
		
		return bounds;  
	}
	
	/**
	 * Computes Montecarlo resample method
	 * 
	 * @param bounds a couple of double vector containing bounds value
	 * @return a reference to a resampled Data_Matrix object
	 */
	private DataMatrix monteCarloSampling(double bounds[][])
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
		
	/**
	 * Computes the Principal Components resampling method
	 * 
	 * @return a reference to a dataMatrix object containing resample data
	 * @see DataMatrix
	 */
	private DataMatrix principalComponentsSampling()
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
	
	/**
	 * Computes a resampling via a column permutation on the original data
	 * 
	 * @return a reference to a resampled DataMatrix object
	 * @see DataMatrix
	 */
	private DataMatrix columnSampling()
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
	
	/**
	 * Computes mean over the double value x
	 * 
	 * @param x input vector
	 * @return mean on x vector values
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

	/**
	 * dataMatrix accessor method
	 * 
	 * @return the DataMatrix object
	 */
	public DataMatrix getDataMatrix() {
		return dataMatrix;
	}
	
	/**
	 * dataMatrix mutator method
	 * 
	 * @param dataMatrix a DataMatrix object
	 */
	public void setDataMatrix(DataMatrix dataMatrix) {
		this.dataMatrix = dataMatrix;
	}

	/**
	 * decomposition vector accessor method
	 * 
	 * @return the decomposition vector
	 */
	public double[][] getDecomposition() {
		return decomposition;
	}

	/**
	 * decomposition vector mutator method
	 * 
	 * @param decomposition a vector of double containing the decomposition values
	 */
	public void setDecomposition(double[][] decomposition) {
		this.decomposition = decomposition;
	}

	/**
	 * resampleType accessor method 
	 * 
	 * @return a string representing the resample type
	 */
	public String getResampleType() {
		return resampleType;
	}

	/**
	 * resampleType mutator method
	 * 
	 * @param resampleType a String containing the type of resample, possible values are:
	 * c that stands for column sampling, m that stands for Monte calo sampling, p that stands
	 * for principal components sampling methods
	 */
	public void setResampleType(String resampleType) {
		this.resampleType = resampleType;
	}

	/**
	 * Algorithm path accessor method 
	 *  
	 * @return a String representing the algorithm path
	 */
	public String getAlgPath() {
		return algPath;
	}

	/**
	 * Algorithm path mutator method
	 * 
	 * @param algPath a String representing the algorithm path 
	 */
	public void setAlgPath(String algPath) {
		this.algPath = algPath;
	}
		
}
