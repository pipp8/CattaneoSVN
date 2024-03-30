/*
 * $Id: ConsensusClustering.java 876 2017-04-08 22:23:09Z cattaneo $
 * $LastChangedDate: 2017-04-09 00:24:20 +0200(Dom, 09 Apr 2017) $
 */

package valworkbench.measures.internal.ConsensusC;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;
import java.util.StringTokenizer;

import valworkbench.datatypes.ClusterMatrix;
import valworkbench.datatypes.ConnettivityMatrix;
import valworkbench.datatypes.ConsensusMatrix;
import valworkbench.datatypes.DataMatrix;
import valworkbench.datatypes.HeaderData;
import valworkbench.datatypes.IndicatorMatrix;
import valworkbench.datatypes.InputMeasure;
import valworkbench.datatypes.MeasureVector;
import valworkbench.exceptions.DirNotFoundException;
import valworkbench.measures.Measure;
import valworkbench.utils.ConsensusWorker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

/**
 * The ConsensusClustering class provides an object that encapsulates methods and
 * states information for computing Consensus Cluster measure. This class takes
 * in input a DataMatrix and an InputMeasure object. Values of
 * mesaure are returned in a MeasureVector object. The effect of
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
public class ConsensusClustering extends Measure  {
	// private ConsensusMatrix consensusMatrix; // può essere una variabile locale a computeMesasure()?
	private final String MEASURE_NAME = "Consensus Clustering";
	private InputConsensus parameters;
	private DataMatrix dataMatrix;
	private MeasureVector measureValue, AkVector, minRelative;
	private HeaderData headerData;
	private String pathOutputCDF, pathOutputElements, pathOutputConsMatrix, pathAk;

	private  double MaxAk;
	private  double[] ak = null;
	
	private static final Logger LOGGER = Logger.getLogger( ConsensusClustering.class.getName() );
	
	/**
	 * Default class Constructor
	 * 
	 */
	public ConsensusClustering() {
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
	public ConsensusClustering(DataMatrix dataMatrix, InputConsensus mParameters)throws DirNotFoundException 
	{
		this.parameters = mParameters;

		this.dataMatrix = dataMatrix;

		this.pathOutputCDF = this.parameters.getCurrentAuxDir()
		+ File.separator + "CDF.txt";

		this.pathOutputElements = this.parameters.getCurrentAuxDir()
		+ File.separator + "Elements.txt";

		this.pathOutputConsMatrix = this.parameters.getCurrentAuxDir()
		+ File.separator + "Consensus_Matrix.txt";

		this.pathAk = this.parameters.getCurrentAuxDir() + File.separator
		+ "AK.txt";
	}

	/**
	 * Computes measure values
	 * 
	 * @see InputMeasure
	 * @return a Measure_Vector object containing measure values
	 * @see MeasureVector
	 */
	public MeasureVector computeMeasure() throws FileNotFoundException, IOException, NumberFormatException, Exception
	{
		// reads number of items
		int N = this.dataMatrix.getNOfItem();

		int B = this.parameters.getNumberOfIteration();
		int nGeneselect = (int) ((N * this.parameters.getPercentage()) / 100);
		
		ak = new double[this.parameters.getKMax() - this.parameters.getKMin() + 1];
		double dK[] = new double[this.parameters.getKMax() - this.parameters.getKMin() + 1];

		boolean flagOut;
		String commandLine = null;
		int exitValue;
		double max_ak=0;
		MaxAk = 0.;
		
		this.measureValue = new MeasureVector(this.parameters.getKMax()
				- this.parameters.getKMin() + 1);	

		this.AkVector=new MeasureVector(this.parameters.getKMax()
				- this.parameters.getKMin() + 1);
		
		this.minRelative = new MeasureVector(1);
		// instance of the matrices
		IndicatorMatrix indMatrix = new IndicatorMatrix(N);
		ConnettivityMatrix connMatrix = new ConnettivityMatrix(N);
		ConsensusMatrix consensusMatrix = new ConsensusMatrix(N);

		// instance temporary matrix
		IndicatorMatrix tmpindMatrix = new IndicatorMatrix(N);
		ConnettivityMatrix tmpconnMatrix = new ConnettivityMatrix(N);
		int Nmissing = 0;
		
		for (int k = 0; k < this.parameters.getKMax()- this.parameters.getKMin() + 1; k++) {

			LOGGER.log( Level.INFO, "Executing clustering algorithm {0} for {1} clusters",
					new Object[]{ parameters.getAlgorithmName(), Integer.toString(k + this.parameters.getKMin())});

			int missingOut = 0;

			//Inizializes Consensus Matrix et all
			consensusMatrix.setConsensusMatrixtoZero();
			indMatrix.setIMatrixtoZero();
			connMatrix.setConnMatrixtoZero();

			//Iteration cicle for Consensus
			for (int b = 0; b < B; b++) {

				LOGGER.log( Level.FINE, "Compute on the resampled data at the {0}%", Math.floor((double) (b + 1) / B * 100));
				
				tmpindMatrix.setIMatrixtoZero();
				tmpconnMatrix.setConnMatrixtoZero();
				//Resample data

				//Creates item permutation and selects items for the computation
				int permutation[] = permutation(this.dataMatrix.getNOfItem());

				int geneData[] = new int[nGeneselect];
				for (int s = 0; s < nGeneselect; s++) {
					geneData[s] = permutation[s];
				}

				resample(nGeneselect, this.dataMatrix, permutation, this.parameters.getAlgInputPath());	// usa lo stesso file come era nell'originale

				//Executes Algorithm for resample data

				//Defines output path
				String tempPath = parameters.getAlgOutputPath()
				+ Integer.toString(k + this.parameters.getKMin())
				+ ".txt";

				if (parameters.isInitExtFlag()) {

					// Defines command Line for the external algorithm
					commandLine = composeCmndLine(k + parameters.getKMin(),
							"\""+parameters.getInitExtAlgPath()+"\"", "\""+parameters
							.getInitExtInpPath()+"\"", "\""+parameters
							.getInitExtOutPath()+"\"", null, 0, parameters
							.getInitExtCommandLine(), false);

					// Executes algorithm for external initialization
					exitValue = this.executeAlgorithm(commandLine, parameters.getInitExtOutPath());

					// checks correct execution
					if (exitValue == 1) {
						// if incorrect execution, sets random initialization
						parameters.setInitExtFlag(false);
					}
				}

				// Composes command line for algorithm execution
				commandLine = composeCmndLine(k + this.parameters.getKMin(),
						"\""+parameters.getAlgorithmPath()+"\"", "\""+parameters.getAlgInputPath()+"\"",
						"\""+tempPath+"\"", "\""+parameters.getInitExtOutPath()+"\"", 0,
						parameters.getAlgCommandLine(), parameters.isInitExtFlag());

				// Executes the algorithm
				exitValue = this.executeAlgorithm(commandLine, tempPath);

				// Checks correct execution
				flagOut = false;
				if (exitValue == 1) {
					LOGGER.log( Level.WARNING, "Missing execution in iteration {0}", b);
				}
				else if (exitValue == 0) {
					// Checks correct output (i.e. Missing out)
					flagOut = checkOut(tempPath);
				}

				if (flagOut) {
					// Loads cluster matrix
					ClusterMatrix clusterMatrix = new ClusterMatrix();
					clusterMatrix.loadFromFile(tempPath);

					ClusterMatrix clusterMatrixReal = unResample( clusterMatrix, permutation,
							k + this.parameters.getKMin());

					// Calculates Connettivity and indicator Matrix
					tmpindMatrix.setIMatrix(geneData);
					tmpconnMatrix.setConnMatrix(clusterMatrixReal);

					// Sums Inidicator and Connettivity Matrix
					indMatrix.addIMatrix(tmpindMatrix);
					connMatrix.addConnMatrix(tmpconnMatrix);

					missingOut++;
				}
			}

			double cdfVector[];

			// Checks number of missing
			if (((double) (missingOut / B)) > 0.1) {

				// Calculates Consensus Matrix
				consensusMatrix.setConsensusMatrix(connMatrix, indMatrix);

				// Calculates CDF values
				double sortVectorelement[] = consensusMatrix.vectorSortElement();
				cdfVector = new double[sortVectorelement.length];
				for (int s = 0; s < sortVectorelement.length; s++) {
					cdfVector[s] = consensusMatrix.calculateCDF(sortVectorelement[s]);
				}

				// Calculates Ak values
				ak[k] = calulateAk(sortVectorelement, cdfVector);
				if (k == 0)
				{
					max_ak = ak[k];
				}
				if ((!this.parameters.isHierarchical()) && (this.parameters.isReplaceAk()))
				{
					if (ak[k] > max_ak)
					{
						max_ak = ak[k];
					}
					else
					{
						ak[k] = max_ak;
					}
				}
				missingOut = 1;
				Nmissing++;
			}
			else {
				ak[k] = Double.NaN;
				missingOut = 0;
				cdfVector = new double[1];
			}

			// Stores CDF, Elements and Consensus Matrix values in a file
			consensusMatrix.storeToFileCDF(this.pathOutputCDF, k
					+ this.parameters.getKMin(), cdfVector, missingOut);
			consensusMatrix.storeToFileElement(this.pathOutputElements, missingOut);
			consensusMatrix.storeToFileConsensusMatrix(
					this.pathOutputConsMatrix, k + this.parameters.getKMin(),
					missingOut);

			this.measureValue.setNOfCluster(k, k + this.parameters.getKMin());
		// Fine loop per ogni k da KMin a KMax
		}
		
		if(ak[this.parameters.getKMax()- this.parameters.getKMin()]<ak[this.parameters.getKMax()- this.parameters.getKMin()-1])
		  ak[this.parameters.getKMax()- this.parameters.getKMin()]=ak[this.parameters.getKMax()- this.parameters.getKMin()-1];
		// Calculates dk values
		dK = calculateDk(ak);

		// Writes Output and Ak values
		this.measureValue.setMeasureValue(dK);
		storeToFileAk(ak);


		this.headerData = this.writeHeader();
		this.headerData.storeToFile(parameters.getOutputPath()
				+ File.separator + "Measure_Header.vhf");
		this.measureValue.storeToFile(parameters.getOutputPath()
				+ File.separator + "Measure_Values.txt");
		
		//Extract Suggested Number of Cluster
		int nOfCluster=0;
		while((nOfCluster<(this.parameters.getKMax()-this.parameters.getKMin()+1))&&(dK[nOfCluster]>dK[nOfCluster+1]))
			nOfCluster++;

		this.minRelative.setNOfCluster(0, nOfCluster+this.parameters.getKMin());
		
		this.minRelative.setMeasureValue(0, dK[nOfCluster]);
		
		this.minRelative.storeToFile(parameters.getOutputPath()
				+ File.separator + "nOfCluster_Value.txt");
		//Ends to compute measure
		return this.measureValue;
	}

	/**
	 * Creates an array of a pseudorandom permutation of n numbers
	 * 
	 * @param n
	 * @return an array with a pseudorandom permutation of n.
	 */
	private int[] permutation(int n) {
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
	 * Creates a file containing the resample data matrix
	 * 
	 * @param nItemselect number of items for the new dataset
	 * @param dataMatrix a reference to a Data_Matrix object
	 * @see DataMatrix
	 * @param permutation a vector of integer containing permutation for the resample
	 */
	public void resample(int nItemselect, DataMatrix dataMatrix, int  permutation[], String outFileName) throws Exception
	{
		DataMatrix pMatrix = new DataMatrix(nItemselect, dataMatrix.getNOfFeature());

		DataMatrix pattMatrix = dataMatrix.copyDMatrix();

		// Swaps of rows
		pMatrix.setFeatureName(pattMatrix.getFeatureName());
		for (int i = 0; i < nItemselect; i++)
		{
			pMatrix.setItemName(i, pattMatrix.getItemName(permutation[i]));
			pMatrix.setItemDescription(i, pattMatrix.getItemDescription(permutation[i]));
			pMatrix.setItemValueRow(i,pattMatrix.getItemValueRow(permutation[i]));
		}

		// Writes the resample dataset
		pMatrix.storeToFile(outFileName);

	}

	/**
	 * Calculates the Ak values for the Consensus Matrix
	 * 
	 * @param sortVectorelement a vector of double containing sorted elements of Consensus Matrix
	 * @param cdfVector a vector of double for the CDF values
	 * @return the Ak values
	 */
	public double calulateAk(double sortVectorelement[], double cdfVector[]) {

		double ak = 0;
		for (int i = 1; i < sortVectorelement.length; i++) {
			ak += (sortVectorelement[i] - sortVectorelement[i - 1])
			* cdfVector[i];
		}

		return ak;
	}

	/**
	 * Calculates the dk values.
	 * 
	 * @param ak a vector of double for ak values.
	 * @return a vector of double containing the dk values.
	 */
	public double[] calculateDk(double ak[]) {

		double dk[] = new double[this.parameters.getKMax()
		                         - this.parameters.getKMin() + 1];

		int missing = 0;
		for (int s = 0; s < ak.length; s++)
			if (ak[s] != Double.NaN) {
				missing++;
			}

		// Missing management

		double aknoMissing[] = new double[missing];
		int j = 0;

		for (int s = 0; s < ak.length; s++)
			if (ak[s] != Double.NaN) {
				aknoMissing[j] = ak[s];
				j++;
			}

		j = 1;

		for (int i = 0; i < (this.parameters.getKMax() - this.parameters.getKMin() + 1); i++) {
			if ((ak[i] != Double.NaN) & (j < missing )) {
				if (j > 1) {
					dk[i] = (aknoMissing[j] - aknoMissing[j - 1])
					/ aknoMissing[j - 1];
				} else {
					dk[i] = aknoMissing[j-1];
				}
				j++;
			} else
				dk[i] = Double.NaN;
		}

		return dk;
	}

	/**
	 * Checks if the outuput file produced by the external algorithm is a Missing
	 * 
	 * @param path the output path
	 * @return a boolean value equal to true if is not missing output, false otherwise.
	 */
	private boolean checkOut(String path) throws FileNotFoundException, IOException, NumberFormatException,	Exception {

		File fac = new File(path);
		FileInputStream fis = new FileInputStream(fac);
		InputStreamReader isr = new InputStreamReader(fis);
		BufferedReader br = new BufferedReader(isr);
		String linea = br.readLine();

		if (linea != null) {
			return true;
		}


		return false;

	}
	
	/**
	 * Collects information about the experiment.
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
		+ " Percentage of Dataset:" + this.parameters.getPercentage()
		+ "%";
		header.setMeasParameters(parametri);
		return header;
	}

	/**
	 * Finds the relation between the original data matrix and the resample data
	 * matrix
	 * 
	 * @param clusterMatrix a reference to a Cluster_Matrix object containing 
	 * the resample data matrix
	 * @see ClusterMatrix
	 * @param permutation a vector of integer containing a permutation
	 * @param k the number of clusters
	 * @return the correct index of cluster matrix for the resample data matrix
	 * @see ClusterMatrix
	 */
	public ClusterMatrix unResample(ClusterMatrix clusterMatrix,
			int[] permutation, int k) {
		ClusterMatrix realClusterMatrix = new ClusterMatrix(k);

		for (int i = 0; i < k; i++) {
			int cl[] = clusterMatrix.getCMatrixRow(i);
			int rl[] = new int[cl.length];

			realClusterMatrix.setClusterSize(i, cl.length);
			realClusterMatrix.instanceCMatrixRow(i, cl.length);

			for (int j = 0; j < cl.length; j++) {
				rl[j] = permutation[cl[j]];
			}

			realClusterMatrix.setCMatrixRow(i, rl);
		}

		return realClusterMatrix;
	}

	/**
	 * Stores the Ak value in a file.
	 * 
	 * @param ak a vector of double containing the Ak values.
	 */
	private void storeToFileAk(double ak[]) throws FileNotFoundException, IOException, NumberFormatException,	Exception 
	{

		// Opens file in append
		FileOutputStream file = new FileOutputStream(this.pathAk);
		PrintStream Output = new PrintStream(file);

		// Writes the file
		for (int i = 0; i < ak.length; i++) {
			Output.println("--- Ak " + (i + this.parameters.getKMin())
					+ " ---");
			if (ak[i] != Double.NaN) {
				Output.println("| " + ak[i]);
			} else {
				Output.println("* Missing");
			}
			Output.println("--- End Ak " + (i + this.parameters.getKMin())
					+ " ---");
		}

		// Closes the file
		Output.close();
	}

	/**
	 * Return  the k-th Consensus Cluster Matrix in Consensus_Matrix.txt in auxiliary directory
	 * @param k the number of clusters
	 * @return the Consensus Matrix
	 * @see ConsensusMatrix
	 */
	public ConsensusMatrix getConsensusMatrix(int k) throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{
		ConsensusMatrix matrix=new ConsensusMatrix(this.dataMatrix.getNOfItem());
		matrix.loadFromFileConsensusMatrix(this.pathOutputConsMatrix, k);

		return matrix;
	}


	/**
	 * Return  the elements of k-th Consensus Cluster Matrix in Elements.txt in auxiliary directory
	 * @param k number of clusters
	 * @return the consensus matrix
	 */
	public List<Double> getElements(int k) throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{
		ConsensusMatrix matrix=new ConsensusMatrix(this.dataMatrix.getNOfItem());
		return matrix.getElemnts(this.pathOutputElements, k, this.parameters.getKMin());

	}
	
	/**
	 * Gets a Measure_Vector containing the Ak values previously computed
	 * @see MeasureVector
	 * @return a reference to a Measure_Vector object
	 * 
	 */
	public MeasureVector getAkVector() {
		return AkVector;
	}
	
	/*
	 * Added
	 */
	
	public void executeSingleIteration(int clusterIndex) throws FileNotFoundException, IOException, NumberFormatException, Exception
	{
		String commandLine = null;
		int exitValue;
		boolean flagOut;
		
		// Algorithm Input Path temporanea solo per il thread
		String tmpAlgInputPath = parameters.getAlgInputPath() + String.format( "%3d", clusterIndex);
		LOGGER.log( Level.FINE, "Temp algorithm input path: {0}", tmpAlgInputPath);

		// reads number of items
		int N = this.dataMatrix.getNOfItem();
		int numberOfIterations = this.parameters.getNumberOfIteration();
		int Nmissing = 0;

		int nGeneselect = (int) ((N * this.parameters.getPercentage()) / 100);
		
		IndicatorMatrix indMatrix = new IndicatorMatrix(N);
		ConnettivityMatrix connMatrix = new ConnettivityMatrix(N);
		
		IndicatorMatrix tmpindMatrix = new IndicatorMatrix(N);
		ConnettivityMatrix tmpconnMatrix = new ConnettivityMatrix(N);
		
		ConsensusMatrix consensusMatrix = new ConsensusMatrix(N);
		
		LOGGER.log( Level.INFO, "Executing clustering algorithm {0} for {1} clusters",
				new Object[]{ parameters.getAlgorithmName(), Integer.toString(clusterIndex + this.parameters.getKMin())});
	
		int missingOut = 0;
	
		//Initializes Consensus Matrix et all
		consensusMatrix.setConsensusMatrixtoZero();
		indMatrix.setIMatrixtoZero();
		connMatrix.setConnMatrixtoZero();
	
		//Iteration cicle for Consensus
		for (int b = 0; b < numberOfIterations; b++) {
	
			LOGGER.log( Level.FINE, "Compute on the resampled data at the {0}%",
					Math.floor((double) (b + 1) / numberOfIterations * 100));
	
			tmpindMatrix.setIMatrixtoZero();
			tmpconnMatrix.setConnMatrixtoZero();
			//Resample data
	
			//Creates item permutation and selects items for the computation
			int permutation[] = permutation(this.dataMatrix.getNOfItem());
	
			int geneData[] = new int[nGeneselect];
			for (int s = 0; s < nGeneselect; s++) {
				geneData[s] = permutation[s];
			}
	
			resample(nGeneselect, this.dataMatrix, permutation, tmpAlgInputPath);
	
			//Executes Algorithm for resampled data
	
			//Defines output path
			String tempPath = parameters.getAlgOutputPath()
			+ Integer.toString(clusterIndex + this.parameters.getKMin())
			+ ".txt";
	
			if (parameters.isInitExtFlag()) {
	
				// Defines command Line for the external algorithm
				commandLine = composeCmndLine(clusterIndex + parameters.getKMin(),
						"\""+parameters.getInitExtAlgPath()+"\"", "\""+parameters
						.getInitExtInpPath()+"\"", "\""+parameters
						.getInitExtOutPath()+"\"", null, 0, parameters
						.getInitExtCommandLine(), false);
	
				// Executes algorithm for external initialization
				exitValue = this.executeAlgorithm(commandLine, parameters.getInitExtOutPath());
	
				// checks correct execution
				if (exitValue == 1) {
					// if incorrect execution, sets random initialization
					parameters.setInitExtFlag(false);
				}
			}
	
			// Composes command line for algorithm execution
			commandLine = composeCmndLine(clusterIndex + this.parameters.getKMin(),
					"\""+parameters.getAlgorithmPath()+"\"", "\""+tmpAlgInputPath+"\"",
					"\""+tempPath+"\"", "\""+parameters.getInitExtOutPath()+"\"", 0,
					parameters.getAlgCommandLine(), parameters.isInitExtFlag());
	
			// Executes the algorithm

			LOGGER.log( Level.FINE, "Thread index {0} : iteration: {1}",
					new Object[]{ clusterIndex, b});

			exitValue = this.executeAlgorithm(commandLine, tempPath);
	
			// Checks correct execution
			flagOut = false;
			if (exitValue == 1) {
				LOGGER.log( Level.WARNING, "Missing execution in iteration {0}", b);
			}
			else if (exitValue == 0) {
				// Checks correct output (i.e. Missing out)
				flagOut = checkOut(tempPath);
			}
	
			if (flagOut) {
				// Loads cluster matrix
				ClusterMatrix clusterMatrix = new ClusterMatrix();
				clusterMatrix.loadFromFile(tempPath);
	
				ClusterMatrix clusterMatrixReal = unResample( clusterMatrix, permutation,
						clusterIndex + this.parameters.getKMin());
	
				// Calculates Connectivity and indicator Matrix
				tmpindMatrix.setIMatrix(geneData);
				tmpconnMatrix.setConnMatrix(clusterMatrixReal);
	
				// Sums Indicator and Connectivity Matrix
				indMatrix.addIMatrix(tmpindMatrix);
				connMatrix.addConnMatrix(tmpconnMatrix);
	
				missingOut++;
			}
		}
		
		double cdfVector[];
	
		// Checks number of missing
		if (((double) (missingOut / numberOfIterations)) > 0.1) {
	
			// Calculates Consensus Matrix
			consensusMatrix.setConsensusMatrix(connMatrix, indMatrix);
	
			// Calculates CDF values
			double sortVectorelement[] = consensusMatrix.vectorSortElement();
			cdfVector = new double[sortVectorelement.length];
			
			for (int s = 0; s < sortVectorelement.length; s++) {
				
				cdfVector[s] = consensusMatrix.calculateCDF(sortVectorelement[s]);
			}
	
			// Calculates Ak values
			ak[clusterIndex] = calulateAk(sortVectorelement, cdfVector);
			
			if(clusterIndex==0)
			{
				MaxAk=ak[clusterIndex];
			}
			if ((!this.parameters.isHierarchical()) && (this.parameters.isReplaceAk()))
			{
				if (ak[clusterIndex]>MaxAk)
				{
					MaxAk=ak[clusterIndex];
				}
				else {
					ak[clusterIndex] = MaxAk;
				}
			}
			missingOut = 1;
			Nmissing++;
		} else {
			ak[clusterIndex] = Double.NaN;
			missingOut = 0;
			cdfVector = new double[1];
	
		}
	
		// Stores CDF, Elements and Consensus Matrix values in a file
		consensusMatrix.storeToFileCDF(this.pathOutputCDF, clusterIndex
				+ this.parameters.getKMin(), cdfVector, missingOut);
		
		consensusMatrix.storeToFileElement(this.pathOutputElements,	missingOut);
		
		consensusMatrix.storeToFileConsensusMatrix(	this.pathOutputConsMatrix,
				clusterIndex + this.parameters.getKMin(), missingOut);
	
		this.measureValue.setNOfCluster(clusterIndex, clusterIndex + this.parameters.getKMin());
		
		// rimuove il file input per l'algoritmo di clusterizzazione temporaneo
		File tmp = new File(tmpAlgInputPath);
		tmp.delete();
	}

	
	/**
	 * Computes measure values
	 * 
	 * @see InputMeasure
	 * @return a Measure_Vector object containing measure values
	 * @see MeasureVector
	 */
	public MeasureVector parallelComputeMeasure() throws FileNotFoundException, IOException, NumberFormatException, Exception
	{
		// reads number of items
		int N = this.dataMatrix.getNOfItem();

		int nClusterSizes = this.parameters.getKMax() - this.parameters.getKMin() + 1;
				
		ak = new double[nClusterSizes];		// condiviso tra tutti i thread ognuno per il k di competenza

		double dK[] = new double[nClusterSizes];	

		MaxAk = 0.;
		
		this.measureValue = new MeasureVector(nClusterSizes);	

		this.AkVector = new MeasureVector(nClusterSizes);
		
		this.minRelative = new MeasureVector(1);
		
		int maxThreads = 8;
		LOGGER.log( Level.INFO, "Starting thread pool (max: {0})", maxThreads);

		ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        
		for (int k = 0; k < nClusterSizes; k++) {
			// lancia in un thread il calcolo
			Runnable worker = new ConsensusWorker( this, k);

			executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }

		LOGGER.log( Level.INFO, "All threads finished");
		
		if( ak[nClusterSizes - 1] < ak[nClusterSizes - 2])
			ak[nClusterSizes - 1] = ak[nClusterSizes - 2];
		// Calculates dk values
		dK = calculateDk(ak);

		// Writes Output and Ak values
		this.measureValue.setMeasureValue(dK);
		storeToFileAk(ak);

		this.headerData = this.writeHeader();
		this.headerData.storeToFile(parameters.getOutputPath()
				+ File.separator + "Measure_Header.vhf");
		this.measureValue.storeToFile(parameters.getOutputPath()
				+ File.separator + "Measure_Values.txt");
		
		//Extract Suggested Number of Cluster
		int nOfCluster = 0;
		while ((nOfCluster < nClusterSizes) && (dK[nOfCluster] > dK[nOfCluster+1]))
			nOfCluster++;

		this.minRelative.setNOfCluster(0, nOfCluster+this.parameters.getKMin());
		
		this.minRelative.setMeasureValue(0, dK[nOfCluster]);
		
		this.minRelative.storeToFile(parameters.getOutputPath()
				+ File.separator + "nOfCluster_Value.txt");
		//Ends to compute measure
		return this.measureValue;
	}
}
