package valworkbench.measures.internal.CLEST;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import valworkbench.datatypes.ClusterMatrix;
import valworkbench.datatypes.DataMatrix;
import valworkbench.datatypes.HeaderData;
import valworkbench.datatypes.InputMeasure;
import valworkbench.datatypes.MeasureVector;
import valworkbench.measures.Measure;
import valworkbench.measures.external.adjustedRand.AdjustedRand;
import valworkbench.measures.external.findex.FIndex;
import valworkbench.measures.external.fmindex.FMIndex;
import valworkbench.nullmodels.NullModels;

/**
 * The Clest class provides an object that encapsulates methods and
 * states information for computing CLEST measure. This class takes
 * in input a DataMatrix and an InputMeasure object. Values of
 * measure are returned in a Measure_Vector object. The effect of
 * computing such measure is basically to find a correct number of clusters with
 * the given algorithm A and benchmark dataset D.
 * 
 * 
 * 
 * @author Raffaele Giancarlo
 * @author Filippo Utro
 * @author Davide Scaturro
 * @version 1.0
 * @see Measure
 * 
 */
public class Clest extends Measure{

	private final String MEASURE_NAME = "CLEST";
	private InputCLEST parameters;
	private DataMatrix dataMatrix;
	private DataMatrix learn, test;
	private MeasureVector pk, dk;
	private HeaderData headerData;
	private MeasureVector bestK = new MeasureVector(1);;
	private int nGeneselect;
	private double bounds[][];
	private double decomposition[][];
	
	/**
	 * Default class Constructor
	 * 
	 */
	public Clest() {
		// Empty constructor method
	}

	/**
	 * Class constructor specifying input data Matrix and input parameters
	 * 
	 * @param dataMatrix input data Matrix
	 * @see DataMatrix 
	 * @param mParameters measure parameters
	 * @see InputMeasure
	 * 
	 */
	public Clest(DataMatrix dataMatrix, InputCLEST parameters) {
		this.parameters = parameters;
		this.dataMatrix = dataMatrix;
		this.nGeneselect = (int) ((this.dataMatrix.getNOfItem() * this.parameters.getPercentage()) / 100);
		this.pk = new MeasureVector(this.parameters.getKMax()-this.parameters.getKMin()+1);
		this.dk = new MeasureVector(this.parameters.getKMax()-this.parameters.getKMin()+1);
	}
	
	
	/**
	 * Compute the similarity value
	 * 
	 * @param dataMatrix input data Matrix  
	 * @see DataMatrix
	 * @param k number of cluster
	 * @param nGeneselct
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */
	public  double[] computeSimilarity(DataMatrix dataMatrix,int k,int nGeneselct) throws FileNotFoundException, IOException, Exception
	{
		ArrayList<Double> similarity = new ArrayList<Double>(); 
		//double[this.parameters.getNumberOfIteration()];
		for(int i=0;i<this.parameters.getNumberOfIteration();i++)
		{
			//Split the dataMatrix in two sets: Learning and Test
			buildLearningSet(dataMatrix,this.nGeneselect);
			
			//Apply the clustering procedure to Learning Set
			this.learn.storeToFile(this.parameters.getAlgInputPath());
						
			String tmpOut=this.parameters.getAlgOutputPath()
			+ Integer.toString(k)
			+ ".txt";
			
			if (clusterAlgorithmRun(tmpOut,k)==0)
			{

				ClusterMatrix clearn= new ClusterMatrix();
				clearn.loadFromFile(tmpOut);
				//Build the Classifier and Apply the result to Classifier to Test Set
				ClusterMatrix gold=DLDA(this.test,this.learn,clearn,k);
				
				//Apply the clustering procedure to Test Set
				this.test.storeToFile(this.parameters.getAlgInputPath());
				
				clusterAlgorithmRun(tmpOut,k);
				ClusterMatrix  T=new ClusterMatrix();
				T.loadFromFile(tmpOut);

				//compute the External Index
				Double s=externalIndex(gold,T);
				if (!s.isNaN())
					similarity.add(s);

			}
		}

		if(similarity.isEmpty())
			return null;
		else
		{
			double sim[]=new double[similarity.size()];
			int i=0;
			for(Double h:similarity)
				sim[i++]=h;
		
			return sim;
		}
	}
	
	/**
	 * Compute the External Index
	 * @param g Gold Solution 
	 * @param t Clustering solution to verify
	 * @see ClusterMatrix
	 * @return a value of similarity
	 */
	public double externalIndex(ClusterMatrix trueSolution, ClusterMatrix clusterSolution)
	{
		double value = Double.NaN;
		if (this.parameters.getExternalName().equalsIgnoreCase("Adjusted Rand index"))
		{
			AdjustedRand adJRand = new AdjustedRand();
			value = adJRand.computeAdjustedRand(trueSolution.compactClusterMatrix(), clusterSolution, this.dataMatrix.getNOfItem()-this.nGeneselect);
		}
		else if (this.parameters.getExternalName().equalsIgnoreCase("F-Index"))
		{
			FIndex fIndexValue = new FIndex();
			value = fIndexValue.computeFMeasure(trueSolution.compactClusterMatrix(), clusterSolution, this.dataMatrix.getNOfItem()-this.nGeneselect, 1);
		}
		else if (this.parameters.getExternalName().equalsIgnoreCase("FM-Index"))
		{
			FMIndex fMIndexValue = new FMIndex();
			value = fMIndexValue.computefMIndex(trueSolution.compactClusterMatrix(), clusterSolution, this.dataMatrix.getNOfItem()-this.nGeneselect);
		}
		
		return value;
	}
	
	
	private int clusterAlgorithmRun(String outputPath, int k)
	{
		String commandLine = null;
		int exitValue;
		
		if (this.parameters.isInitExtFlag()) {
			// Defines command Line for the external algorithm
			commandLine = composeCmndLine(k,
					"\""+parameters.getInitExtAlgPath()+"\"", "\""+parameters
					.getInitExtInpPath()+"\"", "\""+parameters
					.getInitExtOutPath()+"\"", null, 0, parameters
					.getInitExtCommandLine(), false);

			// Executes algorithm for external initialization
			exitValue = this.executeAlgorithm(commandLine, parameters
					.getInitExtOutPath());

			// checks correct execution
			if (exitValue == 1) {
				// if incorrect execution, sets random initialization
				parameters.setInitExtFlag(false);
			}
		}

		// Composes command line for algorithm execution
		commandLine = composeCmndLine(k,
				"\""+parameters.getAlgorithmPath()+"\"", "\""+parameters
				.getAlgInputPath()+"\"", "\""+outputPath+"\"", "\""+parameters
				.getInitExtOutPath()+"\"", 0, parameters
				.getAlgCommandLine(), parameters
				.isInitExtFlag());

		// Executes the algorithm
		exitValue = this.executeAlgorithm(commandLine, outputPath);

		return exitValue;
	}
	

	/**
	 * Creates an array of a pseudo-random permutation of n numbers
	 * 
	 * @param n
	 * @return an array with a pseudo-random permutation of n.
	 */
	private int[] Permutation(int n) {
		int[] v = new int[n];
		int i;
		for (i = 0; i < n; i++) {
			v[i] = i;
		}
		// Shuffles
		for (i = 0; i < n; i++) {
			int r = (int) (Math.random() * (i + 1)); // int between 0 and i
			int swap = v[r];
			v[r] = v[i];
			v[i] = swap;
		}
		return v;
	}
	
	/**
	 * Split the original data in Learning Set and Training Set 
	 * @param dataMatrix the original data
	 * @see DataMatrix
	 * @param nItemselect size of the learning set
	 */
	public void buildLearningSet(DataMatrix dataMatrix, int nItemselect)
	{
		// Build the permutation 
		int[]permutation=Permutation(dataMatrix.getNOfItem());
		DataMatrix tmp=dataMatrix.copyDMatrix();
		
		// Swaps of rows
		for (int i = 0; i < permutation.length; i++) {
			tmp.swapItems(i, permutation[i]);
		}
		
		//Generate DataMatrix L
		this.learn = new DataMatrix(nItemselect, this.dataMatrix.getNOfFeature());
		this.learn= tmp.selectFirstMItem(nItemselect);
				
		//Generate DataMatrix T
		this.test = new DataMatrix((this.dataMatrix.getNOfItem()-nItemselect), this.dataMatrix.getNOfFeature());
		this.test=tmp.selectLastMItem(this.dataMatrix.getNOfItem()-nItemselect);
	}
	
	/**
	 * Computes measure values
	 * 
	 * @see InputMeasure
	 * @return a Measure_Vector object containing measure values
	 * @see MeasureVector
	 */
	public MeasureVector computeMeasure() throws FileNotFoundException, IOException, Exception
	{
		double tk;
		NullModels nModel = new NullModels(dataMatrix, this.decomposition, this.parameters.getResample(), this.parameters.getAlgorithmPath());
		//nModel.initializeResample(this.parameters.getResample());
		MeasureVector tempor = new MeasureVector(this.parameters.getKMax()-this.parameters.getKMin()+1);
		
		for(int k=0; k<this.parameters.getKMax()-this.parameters.getKMin()+1;k++)
		{
			System.out.println("Execute clustering algorithm "
					+ this.parameters.getAlgorithmName() + " for "
					+ Integer.toString(k + this.parameters.getKMin())
					+ " clusters");
			
			double s[]=computeSimilarity(this.dataMatrix,k+this.parameters.getKMin(),this.nGeneselect);

			if (s==null)
			{
				System.out.println("Missing Value on original data for "+(k+this.parameters.getKMin()));
				this.dk.setMeasureValue(k,Double.NaN);
				this.pk.setMeasureValue(k, Double.NaN);
			}
			else
			{
				tk=computeTk(s);

				//step 3
				//
				ArrayList<Double> tKb =new ArrayList<Double>();
				for(int b=0;b<this.parameters.getNumberOfResample();b++)
				{
					System.out.print("Compute on the resample data at the "
							+ Math.floor((double) (b + 1) / this.parameters.getNumberOfResample() * 100) + "% \n");

					//Generate the null model
					
					DataMatrix nullModel = nModel.resample();
					 
					double[] sB = computeSimilarity(nullModel,k+this.parameters.getKMin(),this.nGeneselect);
					
					if (sB==null)
					{
						System.out.println("Missing Valueon on iteration "+b);	
					}
					else
						//Compute the observed statistic
						tKb.add(computeTk(sB));
				}
				double Tkb[]=new  double[tKb.size()];
				int i=0;
				for (Double f: tKb)
				{
					Tkb[i++]=f.doubleValue();
				}
				
				double tk0=0;
				
				if (!tKb.isEmpty())
					tk0=mean(Tkb);
				
				tempor.setMeasureValue(k, tk0);
				tempor.setNOfCluster(k, k+this.parameters.getKMin());

				//Step 4
				this.dk.setMeasureValue(k, (tk-tk0));
				this.dk.setNOfCluster(k, k+this.parameters.getKMin());
				this.pk.setMeasureValue(k, computePvalue(tk,Tkb));
				this.pk.setNOfCluster(k,k+this.parameters.getKMin());
			}
		}
				
		Map<Integer, Double> K = new HashMap<Integer, Double>(); 
		double max=Double.MIN_VALUE;
		int argmax=0;
		/**
		 * Controllare le operazioni con NaN in caso aggiungere il controllo
		 * 
		 * 
		 */
		for(int k=0; k<this.parameters.getKMax()-this.parameters.getKMin()+1; k++)
		{
			if ((pk.getMeasureValue(k)<=this.parameters.getPMax())&(dk.getMeasureValue(k)>=this.parameters.getDMin()))
			{	if (K.isEmpty())
				{
					max=dk.getMeasureValue(k);
					argmax=k;
				}
				K.put(k, dk.getMeasureValue(k));
			}
		}
		
		//Compute correct number of cluster
		if (K.isEmpty())
		{
			this.bestK.setMeasureValue(0,max);
			this.bestK.setNOfCluster(0, 1);
		}
		else
		{
			for(Integer i:K.keySet())
				if (K.get(i)>max)
				{
					max=K.get(i);
					argmax=i;
				}	
			this.bestK.setMeasureValue(0, max);
			this.bestK.setNOfCluster(0, argmax+this.parameters.getKMin());
		}
		
		//Store into file the correct number of cluster and p-value
		this.bestK.storeToFile(this.parameters.getOutputPath()+ File.separator + "nOfCluster_Value.txt");
		this.pk.storeToFile(this.parameters.getCurrentAuxDir() + File.separator +"p-value.txt");
		tempor.storeToExcelFile(this.parameters.getCurrentAuxDir() + File.separator +"null.xls");
		//Store Measure_Header and Measure_Vector
		this.headerData = this.writeHeader();
		this.headerData.storeToFile(parameters.getOutputPath()+ File.separator + "Measure_Header.vhf");
		this.dk.storeToFile(parameters.getOutputPath()+ File.separator + "Measure_Values.txt");
		
		return this.dk;	
	}
	
	/**
	 * Compute a p-value
	 * @param tk denote the observed similarity statistic the original data
	 * @param tKb denote the vector of the observed similarity statistic the null data
	 * @return p-value.
	 */
	private double computePvalue(double tk, double[]tKb)
	{
		double sum=0;
		
		for(int i=0;i<tKb.length;i++)
			if(tKb[i]>=tk)
				sum+=tKb[i];
		
		return (sum/((double)tKb.length));
	}
	
	/**
	 * Compute the mean of the value
	 * @param x vector of input
	 * @return the mean of the value in x 
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
	 * Compute the variance of the value
	 * @param x vector of input
	 * @return the variance of the value in x 
	 */
	private double variance(double []x)
	{
		double m=mean(x);
		double sum_square=0;
		for(int i=0; i<x.length; i++)
			sum_square+=Math.pow((x[i]-m), 2);
		if (x.length!=0)
			return (sum_square/((double)x.length));
		else
			return Double.NaN;
	}
	
	/**
	 * Compute the observed similarity statistic
	 * @param s 
	 * @return
	 */
	private double computeTk(double []s)
	{
			return median(s);
	}
	
	/**
	 * Compute the median value of x
	 * @param x
	 * @return if length x is even return the middle one, otherwise 
	 * return average of middle two.
	 */
	private double median(double[] x)
	{
		//sort array x
		java.util.Arrays.sort(x);
				
		//compute median
		int middle = x.length/2; 
		if (x.length%2 == 1) {
		    // Odd number of elements -- return the middle one.
		    return x[middle];
		} else {
		    // Even number -- return average of middle two
		    return (x[middle-1] + x[middle]) / 2.0;
		}
	}

	/**
	 * Collects information about the experiment.
	 * @see HeaderData
	 * 
	 */
	protected HeaderData writeHeader() {
		HeaderData header = new HeaderData();
		String stringParam = null;

		if (this.parameters.isInitExtFlag()) {
			header.setAlgorithmName(this.parameters.getAlgorithmName() + " init. "
					+ this.parameters.getInitAlgName());
		} else {
			header.setAlgorithmName(this.parameters.getAlgorithmName());
		}
		stringParam = Integer.toString(this.parameters.getKMin()) + "_"
		+ Integer.toString(this.parameters.getKMax());
		String algParam = "";
		StringTokenizer st = new StringTokenizer(this.parameters.getAlgCommandLine());
		for (int i = 0; i <= st.countTokens(); i++) {
			String temp = st.nextToken();
			if (temp.equals("<inputfile>"))
				;
			else if (temp.equals("<outputfile>"))
				;
			else if (temp.equals("<nofcluster>"))
				;
			else if (temp.equals("<extinit>"))
			{
				StringTokenizer  stInit = new StringTokenizer(this.parameters.getInitExtCommandLine());
				for (int j = 0; j <= stInit.countTokens(); j++)
				{
					String temp2 = stInit.nextToken();
					if (temp2.equals("<inputfile>"));
					else if (temp2.equals("<outputfile>"));
					else if (temp2.equals("<nofcluster>"));
					else
					{
						algParam = algParam.concat(" "+temp2);
					}
				}
			}
			else 
			{
				algParam = algParam.concat(temp);
			}
		}
		header.setAlgParameters(stringParam + " " + algParam);
		header.setDatasetName(this.parameters.getDatasetName());
		header.setDatasetType(this.parameters.getDatasetType());
		header.setDateTime();
		header.setMeasureName(this.MEASURE_NAME);
		String parametri = "";
		parametri = parametri + " Number of Iterations:"
		+ this.parameters.getNumberOfIteration()
		+ " Number of resample:" + this.parameters.getNumberOfResample()
		+ " Percentage of Dataset:" + this.parameters.getPercentage()
		+ "%" + "\n Maximum p-value:" + this.parameters.getPMax()
		+ " Mimimum difference statistic:" + this.parameters.getDMin()
		+ " External Index" + this.parameters.getExternalName() 
		+ "\n Classifier: DLDA";
		header.setMeasParameters(parametri);
		
		return header;
	}
	
	
	/**
	 * Compute DLDA classifier
	 * 
	 * @param test the data of the test set
	 * @param learning the data to learn set
	 * @see DataMatrix
	 * @param clearn cluster matrix of learning data
	 * @see ClusterMatrix
	 * @param k number of clusters
	 * @return a cluster matrix of the test data built by classifier.  
	 */
	@SuppressWarnings("unchecked")
	private ClusterMatrix DLDA(DataMatrix test, DataMatrix learning, ClusterMatrix clearn, int k)
	{
	   //Compute variance of the feature
		double variance[]=new double [learning.getNOfFeature()];
		for(int i=0; i<learning.getNOfFeature(); i++)
			variance[i]=variance(learning.extractFeature(i));
		
		//Compute mu_k_j
		double mu[][]=new double[k][learning.getNOfFeature()];
		for(int clust=0; clust<k; clust++)
		{
			DataMatrix tmp = new DataMatrix(clearn.getClusterSize(clust),learning.getNOfFeature());
			int[] item = clearn.getCMatrixRow(clust);
			
			for(int j=0; j<clearn.getClusterSize(clust); j++)
				tmp.setItemValueRow(j, learning.extractRow(item[j]));
			
			for(int j=0; j<learning.getNOfFeature(); j++)
				mu[clust][j]=mean(tmp.extractFeature(j));
		}
		
		//Compute Classification 
		double []tmp = new double[k];
		Object[] temp = new Object[k];
		for (int i=0; i< k ; i++)
		   temp[i]= new ArrayList<Integer>();
				
		for(int x=0; x<test.getNOfItem(); x++)
		{
			double min=Double.MAX_VALUE;
			int index=0;
			for(int clust=0; clust<k; clust++)
			{
				tmp[clust]=0;
				for(int j=0; j<learning.getNOfFeature(); j++)
					tmp[clust]+=((double)Math.pow((test.getItemValue(x, j)-mu[clust][j]),2))/variance[j];	
			}

			for(int clust=0; clust<k; clust++)
				if (tmp[clust]<min)
				{	
					index=clust;
					min=tmp[clust];
				}

			((ArrayList) temp[index]).add(x);
		}	

		ClusterMatrix C = new ClusterMatrix(k);
		
		for (int clust=0; clust<k ; clust++)
		{
			if (((ArrayList) temp[clust]).size()!=0)
			{
				C.instanceCMatrixRow(clust, ((ArrayList) temp[clust]).size());
				C.setClusterSize(clust, ((ArrayList) temp[clust]).size());
			}
			int i=0;
			for(Object j:((ArrayList)temp[clust]))
				C.setCMatrixValue(clust, i++, Integer.valueOf(j.toString()));
		}
		
		return C;
	}

	
	
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
			//Singular Decomposition
			SingularValueDecomposition matrix = new SingularValueDecomposition(X, this.dataMatrix.getNOfItem(),this.dataMatrix.getNOfFeature());
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
	}*/

	/**
	 * p-value, accessor method
	 * 
	 * @return a vector that contains the p-value
	 * @see MeasureVector
	 */
	public MeasureVector getPk() {
		return pk;
	}

	/**
	 * p-value, accessor method
	 * 
	 * @return a vector that contains the d-value
	 * @see MeasureVector
	 */
	public MeasureVector getDk() {
		return dk;
	}

	/**
	 * Correct number of clusters for CLEST measure, accessor method
	 * 
	 * @return a vector that contains  a correct number of clusters for CLEST measure
	 * @see MeasureVector
	 */
	public MeasureVector getBestK() {
		return bestK;
	}

}
