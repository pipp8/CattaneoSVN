package valworkbench.measures.internal.modelExplorer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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

/**
 * The ModelExplorer class provides an object that encapsulates methods and
 * states information for computing Model Explorer measure. This class takes
 * in input a DataMatrix and an InputMeasure object. Values of
 * mesaure are returned in a text file. The effect of
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
public class ModelExplorer extends Measure{
	
	private final String MEASURE_NAME = "ModelExplorer";
	private InputModelExplorer parameters;
	private DataMatrix dataMatrix;
	private HeaderData header;
	private int nIntesect;

	/**
	 * Default class Constructor
	 * 
	 */
	public ModelExplorer() {
	}

	/**
	 * Class constructor specifying input data Matrix and input parameters
	 * 
	 * @param dataMatrix input data Matrix
	 * @see Data_Matrix 
	 * @param mParameters measure parameters
	 * @see InputMeasure
	 */
	public ModelExplorer(DataMatrix dataMatrix, InputModelExplorer parameters) {
		super();
		this.parameters = parameters;
		this.dataMatrix = dataMatrix;
		
	}


	/**
	 * Compute the External Index
	 * @param g Gold Solution 
	 * @param t Clustering solution to verify
	 * @see ClusterMatrix
	 * @return a value of similarity
	 */
	public double externalIndex(ClusterMatrix trueSolution, ClusterMatrix clusterSolution, int size)
	{
		double value = Double.NaN;
		if (this.parameters.getExternalName().equalsIgnoreCase("Adjusted Rand index"))
		{
			AdjustedRand adJRand = new AdjustedRand();
			value = adJRand.computeAdjustedRand(trueSolution.compactClusterMatrix(), clusterSolution, size);
		}
		else if (this.parameters.getExternalName().equalsIgnoreCase("F-Index"))
		{
			FIndex fIndexValue = new FIndex();
			value = fIndexValue.computeFMeasure(trueSolution.compactClusterMatrix(), clusterSolution, size, 1);
		}
		else if (this.parameters.getExternalName().equalsIgnoreCase("FM-Index"))
		{
			FMIndex fMIndexValue = new FMIndex();
			value = fMIndexValue.computefMIndex(trueSolution.compactClusterMatrix(), clusterSolution, size);
		}
		
		return value;
	}
	
	/**
	 * This methods allow to run an external algorithm
	 * @param outputPath output path of the clustering algorithm 
	 * @param k number of cluster
	 * @return an integer that denotes if the executions had successful
	 */
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
	 * Collects information about the experiment.
	 * @see HeaderData
	 * 
	 */
	protected HeaderData writeHeader() {
		
		header = new HeaderData();
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
		parametri = parametri + " Number of resample:" + this.parameters.getNumberOfResample()
		+ " Percentage of Dataset:" + this.parameters.getPercentage()
		+ "%" +" External Index:" +this.parameters.getExternalName().toString();
		header.setMeasParameters(parametri);
		
		return header;
	}

	/**
	 * Creates a file containing the resample data matrix
	 * 
	 * @param nItemselect number of items for the new dataset
	 * @param dataMatrix a reference to a DataMatrix object
	 * @see DataMatrix
	 * @param permutation a vector of integer containing permutation for the resample
	 */
	public DataMatrix Resample(int nItemselect, DataMatrix dataMatrix, int  permutation[]) throws Exception
	{
		DataMatrix pMatrix = new DataMatrix(nItemselect, dataMatrix
				.getNOfFeature());

		DataMatrix pattMatrix = dataMatrix.copyDMatrix();

		// Swaps of rows
		pMatrix.setFeatureName(pattMatrix.getFeatureName());
		for (int i=0;i<nItemselect;i++)
		{
			pMatrix.setItemName(i, pattMatrix.getItemName(permutation[i]));
			pMatrix.setItemDescription(i,  
					pattMatrix.getItemDescription(permutation[i]));

			pMatrix.setItemValueRow(i,pattMatrix.getItemValueRow(permutation[i]));
		}

		// Writes the resample dataset
		return pMatrix;

	}
	
	/**
	 * Finds the relation between the original data matrix and the resample data
	 * matrix
	 * 
	 * @param clusterMatrix a reference to a ClusterMatrix object containing 
	 * the resample data matrix
	 * @see ClusterMatrix
	 * @param permutation a vector of integer containing a permutation
	 * @param k the number of clusters
	 * @return the correct index of cluster matrix for the resample data matrix
	 * @see ClusterMatrix
	 */
	public ClusterMatrix UnResample(ClusterMatrix clusterMatrix, int[] permutation, int k) {
		ClusterMatrix realClusterMatrix = new ClusterMatrix(k);

		for (int i = 0; i < k; i++) 
		{
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
	 * This methods find the intersection between two different resampling from the same input data.
	 * @param sub1 a resampling of the input data
	 * @param sub2 a resampling of the input data
	 * @return the intersection between sub1 and sub2
	 */
	public Map<Integer, Double> findIntesect (int [] sub1, int [] sub2)
	{
		Map<Integer, Double> K = new HashMap<Integer, Double>(); 
		Map<Integer, Double> Intersection = new HashMap<Integer, Double>();
		int nItemselect = (int) ((this.dataMatrix.getNOfItem() * this.parameters.getPercentage()) / 100);
		
		for(int i=0; i<nItemselect; i++)
			K.put(sub1[i], 0.0);
		
		for(int i=0; i<nItemselect; i++)
			if(K.containsKey(sub2[i]))
			{
				Intersection.put(sub2[i],0.0);
			}

		return Intersection;
	}
	
	/**
	 * This method provides to find the cluster intersection 
	 * @param cluster is a cluster matrix
	 * @see ClusterMatrix
	 * @param k is the number of cluster 
	 * @param permutation is a vector that contains the permutation of the data
	 * @param intersect is a Map that contains the intersection between  two different clusterizations
	 * @return a cluster Matrix
	 * @see ClusterMatrix
	 */
	public ClusterMatrix clustIntesect(ClusterMatrix cluster, int k, int permutation[], Map<Integer, Double> intersect)
	{
		ClusterMatrix clIntersect = new ClusterMatrix(k);
		
		
		ClusterMatrix temp = UnResample(cluster, permutation,  k);
				
		for (int i=0; i<k; i++)
		{
			int clust[] = temp.getCMatrixRow(i);
			int clsize=0;
			Map<Integer, Double> clelements = new HashMap<Integer, Double>();
			for(int j=0; j<temp.getClusterSize(i);j++)
			{
				if (intersect.containsKey(clust[j]))
				{
					clsize++;
					clelements.put(clust[j], 0.0);
				}
			}
			
			clIntersect.instanceCMatrixRow(i, clsize);
			clIntersect.setClusterSize(i, clsize);
			int index=0;
			
			for(Integer j: clelements.keySet())
			{
				clIntersect.setCMatrixValue(i, index++, j);	
			}
		}
		
		return clIntersect;
	}
	
	/**
	 * Computes measure values
	 * 
	 * @see InputMeasure
	 * 
	 */
	public MeasureVector computeMeasure() throws FileNotFoundException,IOException, NumberFormatException, Exception 
	{
		
		int nItemselect = (int) ((this.dataMatrix.getNOfItem() * this.parameters.getPercentage()) / 100);
		ClusterMatrix clust1 = new ClusterMatrix();
		ClusterMatrix clust2 = new ClusterMatrix();
		
		double [][] s = new double[this.parameters.getKMax()-this.parameters.getKMin()+1][this.parameters.getNumberOfResample()];
		/*double [][] sF = new double[this.parameters.getKMax()-this.parameters.getKMin()+1][this.parameters.getNumberOfResample()];
		double [][] sFM = new double[this.parameters.getKMax()-this.parameters.getKMin()+1][this.parameters.getNumberOfResample()];
		double [][] sAdj=new double[this.parameters.getKMax()-this.parameters.getKMin()+1][this.parameters.getNumberOfResample()];
		*/
		//AdjustedRand adJRand = new AdjustedRand();
		//FIndex fIndexValue = new FIndex();
		FMIndex fMIndexValue = new FMIndex();
		
		boolean miss1=false, miss2=false;
		
			
		for(int k=0; k<this.parameters.getKMax()-this.parameters.getKMin()+1;k++)
		{
			System.out.println("Execute clustering algorithm "
					+ this.parameters.getAlgorithmName() + " for "
					+ Integer.toString(k + this.parameters.getKMin())
					+ " clusters");
			
			String tmpOut=this.parameters.getAlgOutputPath()
			+ Integer.toString(k+this.parameters.getKMin())
			+ ".txt";
			
		
			//Resample Cicle
			for (int i = 0; i < this.parameters.getNumberOfResample(); i++) 
			{
				// create first subsample
				int[] permu1 = Permutation(this.dataMatrix.getNOfItem());
				DataMatrix sub1=Resample(nItemselect, this.dataMatrix, permu1);
				
				//create second subsample
				int[] permu2 = Permutation(this.dataMatrix.getNOfItem());
				DataMatrix sub2=Resample(nItemselect, this.dataMatrix, permu2);
				
				sub1.storeToFile(this.parameters.getAlgInputPath());
				
				if (clusterAlgorithmRun(tmpOut,k+this.parameters.getKMin())==0)
				{
					clust1.loadFromFile(tmpOut);
					miss1=false;
				}
				else
					miss1=true;
				
				if (!miss1)
				{
					sub2.storeToFile(this.parameters.getAlgInputPath());
					if (clusterAlgorithmRun(tmpOut,k+this.parameters.getKMin())==0)
					{
						clust2.loadFromFile(tmpOut);
						miss2=false;
					}
					else
						miss2=true;
				}
					
				if (miss1 || miss2)
				{
					System.out.println("Missing Value for "+(k+this.parameters.getKMin()));
				}
				else
				{
					Map<Integer, Double> inter = findIntesect (permu1, permu2);
					ClusterMatrix gold = clustIntesect(clust1, k+this.parameters.getKMin(), permu1, inter);
					
					
					ClusterMatrix solution = clustIntesect(clust2, k+this.parameters.getKMin(), permu2, inter);
									
					s[k][i]=externalIndex(gold.compactClusterMatrix(), solution.compactClusterMatrix(),  inter.size());
						
					
					if (Double.valueOf(s[k][i]).isNaN())
					{
						s[k][i]=1.0;
					}
					if (s[k][i]<0)
					{
						s[k][i]=0.0;
					}
										
				}
			
			}
		}
		
		this.header = this.writeHeader();
		this.header.storeToFile(parameters.getOutputPath()
				+ File.separator + "Measure_Header.vhf");
		
		
		
		StoreToFile(this.parameters.getOutputPath()+ File.separator + "Index.txt",s);
		
		
		return null;
	}
	
	/**
	 * Store in to text file the value of the measuer
	 * @param path the pathname of the file to be stored
	 * @param indexValue matrix that contains the values of the index
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	public void StoreToFile(String path, double indexValue[][]) throws FileNotFoundException, IOException, NumberFormatException,	Exception 
	{
		// Open file in append
		FileOutputStream file = new FileOutputStream(path, true);
		PrintStream Output = new PrintStream(file);

		// write first row
		for(int k=0; k<this.parameters.getKMax()-this.parameters.getKMin()+1;k++)
		{
			for (int i = 0; i < indexValue[k].length; i++) 
			{
				Output.print(indexValue[k][i] + "\t");
			}
			Output.println("");
		// write last row
		Output.println("-----------");
		// close file
		}
		Output.close();
	}
	
}
