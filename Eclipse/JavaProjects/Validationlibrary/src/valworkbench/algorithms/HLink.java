package valworkbench.algorithms;

import java.io.FileNotFoundException;
import java.io.IOException;

import valworkbench.datatypes.ClusterList;
import valworkbench.datatypes.ClusterMatrix;
import valworkbench.datatypes.DataMatrix;
import valworkbench.datatypes.SimilarityMatrix;
/**
 * The HLink class provides an object that encapsulate 
 * methods and state information for computing hierarchical 
 * agglomerative partition.
 *  
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0
 */
public class HLink {
	private int nOfCluster; //Specify maximum number of clusters 
	private DataMatrix dataMatrix;
	private SimilarityMatrix similarityMatrix;
	private ClusterList clusterList;
	private boolean writeOutput;
	private String outputPath;
	private String type;
	/**
	 * Default constructor method
	 * 
	 *
	 */	
	public HLink()
	{
		//Default constructor method
	}
	/**
	 * Class constructor specifying: number of clusters required, input data matrix
	 * and hierarchical cluster method selected
	 * 
	 * @param nOfCluster a integer representing the number of clusters required
	 * @param dataMatrix input data matrix of Data_Matrix type
	 * @param type a String representing the Hierarchical cluster method selected (A, C, S)
	 */
	public HLink(int nOfCluster, DataMatrix dataMatrix, String type)
	{
		this.setNOfCluster(nOfCluster);
		this.setDataMatrix(dataMatrix);
		this.clusterList = new ClusterList(dataMatrix);
		this.similarityMatrix = new SimilarityMatrix(dataMatrix.getNOfItem());
		this.similarityMatrix.fillSMatrix(dataMatrix);
		this.setType(type);
	}
	/**
	 * Class constructor specifying number of clusters required, input data matrix,
	 * Hierarchical cluster method selected and output path for clustering solution  
	 *
	 * @param nOfCluster a integer representing the number of clusters required
	 * @param dataMatrix a input data matrix of Data_Matrix type 
	 * @param outputPath a String representing the path name for clustering output file
	 * @param type a String representing the type of hierarchical cluster method selected (A, C, S)
	 * @param writeOutput a boolean flag used to control if writing or not output solution on disk 
	 */
	public HLink(int nOfCluster, DataMatrix dataMatrix, String outputPath, String type, boolean writeOutput)
	{
		this.setNOfCluster(nOfCluster);
		this.setDataMatrix(dataMatrix);
		this.setOutputPath(outputPath);
		this.clusterList = new ClusterList(dataMatrix);
		this.similarityMatrix = new SimilarityMatrix(dataMatrix.getNOfItem());
		this.similarityMatrix.fillSMatrix(dataMatrix);
		this.setWriteOutput(writeOutput);
		this.setType(type);
	}
	/**
	 * Class constructor specifying number of clusters required, input data matrix, 
	 * hierarhical cluster method selected, input clustering matrix of Cluster_Matrix type, and output path for 
	 * clustering solution
	 * 
	 * @param nOfCluster a integer representing the number of clusters required
	 * @param dataMatrix an input data matrix of Data_Matrix type
	 * @param clusterMatrix an input clustering matrix of Cluster_Matrix type
	 * @param outputPath a String representing the path name for clustering output file
	 * @param type a String representing the hierarchical clustering method selected (A, C, S)
	 * @param writeOutput a boolean flag used to control if writing or not output solution on disk
	 * @see DataMatrix
	 * @see ClusterMatrix
	 */
	public HLink(int nOfCluster, DataMatrix dataMatrix, ClusterMatrix clusterMatrix, String outputPath, String type, boolean writeOutput)
	{
		this.setNOfCluster(nOfCluster);
		this.setDataMatrix(dataMatrix);
		this.initCList(clusterMatrix);
		this.getSimilarityMatrix().fillSMatrix(clusterMatrix, dataMatrix);
		this.setOutputPath(outputPath);
		this.setType(type);
		this.setWriteOutput(writeOutput);
	}
	/**
	 * method used to calculate Hierarchical linkage computing
	 * for the specified number of clusters in nOfCluster field
	 */
	public void hlinkComputing() throws FileNotFoundException, IOException, NumberFormatException, Exception
	{
		SimilarityMatrix simMatrix = new SimilarityMatrix();
				
		//Main loop: compute Hierarchical Partition
		for(int i=0; i<(this.dataMatrix.getNOfItem()-this.getNOfCluster()); i++)
		{
			simMatrix = this.hlinkageStep(this.similarityMatrix, this.type);
			this.similarityMatrix = null;
			this.similarityMatrix = simMatrix;
		}
		if(this.isWriteOutput())
		{
			this.clusterList.storeToFile(outputPath);
		}
	}
	/**
	 * method used to compute hierarchical linkage step
	 *     
	 * @param similarityMatrix a similarity matrix of Similarity_Matrix type
	 * @param type a String representing hierarchical clustering method selected
	 * @return a similarity matrix obtained from computing
	 * @see SimilarityMatrix
	 */
	public SimilarityMatrix hlinkageStep(SimilarityMatrix similarityMatrix, String type)
	{	
		int fusionIndex[] = new int[2];
		SimilarityMatrix simMatrix = new SimilarityMatrix();
		fusionIndex = similarityMatrix.maxSimilarityIndex();
		simMatrix = reduceSimilarityMatrix(similarityMatrix, fusionIndex, this.clusterList, type);
		this.clusterList = this.mergeList(this.clusterList, fusionIndex);
		return simMatrix; 
	}
	/**
	 * Performs hierarhical agglomerative step on similarity matrix
	 * 
	 * @param similarityMatrix a similarity matrix of Similarity_Matrix type
	 * @param fusionIndex a two elements vector containing row and column indices of Similarity Matrix  
	 * @param clsList a clustering matrix of Cluster_Matrix data type 
	 * @param type hierarchical cluster method selected
	 * @return a similarity matrix
	 * @see SimilarityMatrix 
	 */
	public SimilarityMatrix reduceSimilarityMatrix(SimilarityMatrix similarityMatrix, int fusionIndex[], ClusterList clsList, String type)
	{
		SimilarityMatrix simMatrix = new SimilarityMatrix((similarityMatrix.getNumberOfRow()-1));
		
		int indmin, indmax;
		
		if(fusionIndex[0] < fusionIndex[1]){//
			indmin=fusionIndex[0];       //
		    indmax=fusionIndex[1];	   //
		}						   //Index of new row and column 
		else{					   //in new dissimilarity matrix
			indmin=fusionIndex[1];    //
			indmax=fusionIndex[0];       //
		}

		//Rewriting first quadrant 
		for (int riga=0; riga<indmax; riga++){
			if(riga!=indmin){
				for(int colonna=0; colonna<indmax; colonna++){
					if(riga==colonna)simMatrix.setValueSMatrix(riga, colonna, 0);   //DisMreduc[riga].dist[colonna]=0;
					if(colonna!=indmin)
						simMatrix.setValueSMatrix(riga, colonna, similarityMatrix.getValueSMatrix(riga, colonna));
				}
			}
		}
		//End rewriting first quadrant


		//Rewriting second quadrant
		if(indmax < similarityMatrix.getNumberOfRow())  //(Dim+1)
		{
			for(int riga=0; riga<indmax; riga++)
			{
				if(riga!=indmin)
				{
					for(int colonna=(indmax+1); colonna<similarityMatrix.getNumberOfRow(); colonna++)  //
					{
						simMatrix.setValueSMatrix(riga, colonna-1, similarityMatrix.getValueSMatrix(riga, colonna));
					}
				}
			}
		}
		//End rewriting second quadrant

		//Rewriting third quadrant
		for(int riga=(indmax+1); riga<similarityMatrix.getNumberOfRow(); riga++)  //(Dim+1) 
		{
			for(int colonna=0; colonna<indmax; colonna++)
			{
				if(colonna!=indmin)
				{
					simMatrix.setValueSMatrix((riga-1), colonna, similarityMatrix.getValueSMatrix(riga, colonna));
				}
			}
		}
		//End rewriting third quadrant

		//Rewriting fourth quadrant
		for(int riga=(indmax+1); riga<similarityMatrix.getNumberOfRow(); riga++)  //(Dim+1)
		{
			for(int colonna=(indmax+1); colonna<similarityMatrix.getNumberOfRow(); colonna++)   //(Dim+1)
			{
				simMatrix.setValueSMatrix((riga-1), (colonna-1), similarityMatrix.getValueSMatrix(riga, colonna));
			}
		}
		//End rewriting fourth quadrant
		
		double alfaIFactor=0, alfaJFactor=0;
		int nI = clsList.getCount(indmin);
		int nJ = clsList.getCount(indmax);
		double lambda = 0;
		
		String method=type.toLowerCase();
		
		if(method.contentEquals("a"))
		{
			alfaIFactor = (double) nI/(nI+nJ);
			alfaJFactor = (double) nJ/(nI+nJ);
			     lambda = (double) 0; 
		}
		else if(method.contentEquals("c"))
		{
			alfaIFactor = (double) 1/2;
			alfaJFactor = (double) 1/2;
				 lambda = (double) 1/2;
		}
		else if(method.contentEquals("s"))
		{
			alfaIFactor = (double) 1/2;
			alfaJFactor = (double) 1/2;
				 lambda = (double) -1/2;
		}
		else
		{ 
			System.out.println("Unknown Method! Error! Program will terminate!");
			System.exit(1);
		}

		for(int riga=0; riga<indmax;  riga++)
		{
			double dki = similarityMatrix.getValueSMatrix(riga, indmin);  //Verificare
			double dkj = similarityMatrix.getValueSMatrix(riga, indmax);  //Verificare
			double dijk =(alfaIFactor * dki) + (alfaJFactor * dkj) + (lambda * Math.abs(dki - dkj));
			simMatrix.setValueSMatrix(riga, indmin, dijk);
			simMatrix.setValueSMatrix(indmin, riga, dijk);
			if(riga==indmin)simMatrix.setValueSMatrix(riga, indmin, 0);//DisMreduc[riga].dist[indmin]=0;
		}
		
		for(int riga=(indmax+1); riga <= simMatrix.getNumberOfRow(); riga++)
		{
			double dki = similarityMatrix.getValueSMatrix(riga, indmin);  //Verificare
			double dkj = similarityMatrix.getValueSMatrix(riga, indmax);  //Verificare
			double dijk =(alfaIFactor * dki) + (alfaJFactor * dkj) + (lambda * Math.abs(dki - dkj));
			simMatrix.setValueSMatrix((riga-1), indmin, dijk);
			simMatrix.setValueSMatrix(indmin, (riga-1), dijk);
		}
				
		return simMatrix;
	}	
	/**
	 * Initializes clusters list with a Cluster_Matrix
	 * 
	 * @param clusterMatrix the input clustering matrix of Cluster_Matrix data type
	 * @return a clusters list equivalent to input clustering matrix
	 */
	@SuppressWarnings("null")
	public ClusterList initCList(ClusterMatrix clusterMatrix)
	{
		ClusterList clsList = null;
		clsList.clusterListInit(clusterMatrix);
		return clsList; 
	}
	/**
	 * Initializes clusters list with a Data_Matrix
	 * 
	 * @param dataMatrix the input data matrix of Data_Matrix type
	 * @return a clusters list
	 */
	@SuppressWarnings("null")
	public ClusterList initCList(DataMatrix dataMatrix)
	{
		ClusterList clsList = null;
		clsList.clusterListInit(dataMatrix);
		return clsList;
	}
	/**
	 * Merges two lists in a cluster list Object and returns a new cluster list object
	 * 
	 * @param clusterList a cluster List object of Cluster_List data type
	 * @param fusionIndex a two elements vector containing row and column indices of Similarity Matrix
	 * @return a cluster list object
	 * @see ClusterList
	 */
	public ClusterList mergeList(ClusterList clusterList, int fusionIndex[])
	{
		int minIndex, maxIndex;
		ClusterList clsList = new ClusterList((clusterList.getNOfCluster()-1));
			
		//Select minimum row
		if(fusionIndex[0] < fusionIndex[1])
		{
			minIndex = fusionIndex[0];
			maxIndex = fusionIndex[1];
		}
		else
		{
			minIndex = fusionIndex[1];
			maxIndex = fusionIndex[0];
		}
		
		//List Fusion
		clusterList.listUnion(minIndex, maxIndex);
		
		//Copying ClusterList
		for(int i = 0; i < maxIndex; i++)
		{
			clsList.setClusterList(i, clusterList.getClusterList(i));
			clsList.setTail(i, clusterList.getTail(i));
			clsList.setCount(i, clusterList.getCount(i));
		}
	
		for(int i = maxIndex+1; i < clusterList.getNOfCluster(); i++)
		{
			clsList.setClusterList(i-1, clusterList.getClusterList(i));
			clsList.setTail(i-1, clusterList.getTail(i));
			clsList.setCount(i-1, clusterList.getCount(i));
		}
				
		return clsList;
	}
	/**
	 * Number of clusters accessor method
	 * 
	 * @return an integer representing number of clusters required
	 */	
	public int getNOfCluster() {
		return nOfCluster;
	}
	/**
	 * Number of clusters mutator method 
	 * 
	 * @param nofCluster an integer representing number of clusters
	 */
	public void setNOfCluster(int nofCluster) {
		this.nOfCluster = nofCluster;
	}
	/**
	 * Output path accessor method
	 * 
	 * @return a String representing the output path for clustering solution
	 */
	public String getOutputPath() {
		return outputPath;
	}
	/**
	 * Output path mutator method
	 * 
	 * @param outputPath a String representing the output path name
	 */
	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}
	/**
	 * Data matrix accessor method 
	 * 
	 * @return a reference to an object of Data_Matrix type
	 */
	public DataMatrix getDataMatrix() {
		return dataMatrix;
	}
	/**
	 * Data_Matrix mutator method
	 * 
	 * @param dataMatrix a reference to an object of Data_Matrix type 
	 */
	public void setDataMatrix(DataMatrix dataMatrix) {
		this.dataMatrix = dataMatrix;
	}
	/**
	 * Similarity matrix accessor method
	 * 
	 * @return a reference to a Similairity_Matrix object 
	 * @see SimilarityMatrix object
	 */
	public SimilarityMatrix getSimilarityMatrix() {
		return similarityMatrix;
	}
	/**
	 * Similarity matrix mutator method
	 * 
	 * @param similarityMatrix a reference to a similarity matrix object 
	 */
	public void setSimilarityMatrix(SimilarityMatrix similarityMatrix) {
		this.similarityMatrix = similarityMatrix;
	}
	/**
	 * Hierarchical type Accessor method
	 * 
	 * @return a String representing the hierarchical cluster method selected
	 */
	public String getType() {
		return type;
	}
	/**
	 * Hierarchical type mutator method
	 * 
	 * @param type a String representing the hierarchical cluster method selected
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * Writes flag accessor method
	 * 
	 * @return a boolean flag used to control if writing or not output solution on disk
	 */
	public boolean isWriteOutput() {
		return writeOutput;
	}
	/**
	 * Writes flag Mutator method
	 * 
	 * @param writeOutput a boolean flag used to control if writing or not output solution on disk
	 */
	public void setWriteOutput(boolean writeOutput) {
		this.writeOutput = writeOutput;
	}
	/**
	 * Clusters list object accessor method
	 * 
	 * @return an object of Cluster_List type 
	 */
	public ClusterList getClusterList() {
		return clusterList;
	}
	/**
	 * Clusters list object mutator method
	 * 
	 * @param clusterList an object of Cluster_List type
	 */
	public void setClusterList(ClusterList clusterList) {
		this.clusterList = clusterList;
	}
}
