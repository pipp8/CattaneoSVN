package valworkbench.graphics;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.jfree.ui.RefineryUtilities;

import valworkbench.datatypes.ConsensusMatrix;
import valworkbench.datatypes.InputMeasure;
import valworkbench.datatypes.MeasureVector;
/**
 * Creates ..........
 * 
 * @author Raffaele Giancarlo
 * @author Filippo Utro
 * @author Davide Scaturro
 * @version 1.0
 */
public class VisualConsensus
{
	/**
	 * 
	 *
	 */
	public VisualConsensus() 
	{
		
	}
	
	/**
	 * create all figure for the consensus cluster data
	 * 
	 * @param nameCdf path of cdf file
	 * @param nameElements path of elements file
	 * @param k number of cluster
	 * @param connMatrix path of consensus matrix
	 * @see ConsensusMatrix
	 * @param dk array containing the dk value
	 * @see MeasureVector 
	 * @param parameters input parameters of the method
	 * @see InputMeasure
	 */
	public VisualConsensus(String nameCdf, String nameElements, int k, ConsensusMatrix connMatrix, MeasureVector dk,InputMeasure parameters )throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{
		visualClusterMatrix(connMatrix);
		visualCDF( nameCdf, nameElements, parameters, connMatrix.getnOfItems());
		visualDk(dk);
		visualElementsHistogram(parameters.getCurrentAuxDir()+nameElements, connMatrix.getnOfItems(), k);
	}
	
	/**
	 * create all figure for the consensus cluster data
	 * 
	 * @param pathconnMatrix path of file containing the consensus matrix
	 * @param pathElements path of elements file
	 * @param pathDk path of file containing the dk value
	 * @param pathCdf path of cdf file 
	 * @param kmax maximun number of cluster
	 * @param kmin minimun number of cluster
	 * @param k number of cluster 
	 * @param N number of gene
	 */
	public VisualConsensus(String pathconnMatrix,String pathElements, String pathDk, String pathCdf, int kmax, int kmin, int k, int N)throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{
		visualClusterMatrix(pathconnMatrix, k, N);
		visualElementsHistogram(pathElements, N, k);
		visualDk(pathDk, kmax, kmin);
		visualCDF(pathCdf, pathElements, N, kmax, kmin);
	}
	
	/**
	 * crate a figure a consensus matrix
	 * 
	 * @param path file path the consensus matrix
	 * @param k number of cluster
	 * @param N number of gene
	 */
	public void visualClusterMatrix(String path, int k, int N) throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{
		VisualConsensusClusterMatrix  demo = new VisualConsensusClusterMatrix ("Consensus Matrix", path, k, N);
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);
		
	}
	
	/**
	 * crate a figure a consensus matrix 
	 * 
	 * @param connMatrix a consensus matrix
	 * @see ConsensusMatrix
	 */
	public void visualClusterMatrix(ConsensusMatrix connMatrix)throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{
		VisualConsensusClusterMatrix  demo = new VisualConsensusClusterMatrix ("Consensus Matrix", connMatrix);
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);			
	}

	/**
	 * crate a figure a elements histogram
	 * 
	 * @param path file path of elements
	 * @param N number of gene
	 * @param k number of cluster
	 */
	public void visualElementsHistogram(String path, int N, int k)throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{
		VisualConsensusClusterHistogram demo = new VisualConsensusClusterHistogram("Consensus Matrix Histogram", path, N, k);
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);			
	}

	/**
	 * create a figure the dk value
	 * 
	 * @param path file path the dk value
	 * @param kmax maximun number of cluster
	 * @param kmin minimun number of cluster
	 */
	public void visualDk(String path, int kmax, int kmin)throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{
		VisualConsensusClusterDk demo = new VisualConsensusClusterDk("Consensus Area", path,kmax, kmin);
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);
	}

	/**
	 * create a figure the dk value
	 * 
	 * @param dk array of dk value
	 * @see MeasureVector
	 */
	public void visualDk(MeasureVector dk)throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{
		VisualConsensusClusterDk demo = new VisualConsensusClusterDk("Consensus Area", dk);
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);
	}
	
	/**
	 * create a figure the cdf value
	 * 
	 * @param pathcdf file path containing cdf value
	 * @param pathelements file path containing elements value 
	 * @param N number of gene
	 * @param kmax maximn number of cluster
	 * @param kmin minimun number of cluster
	 */
	public void visualCDF(String pathcdf, String pathelements, int N, int kmax, int kmin)throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{
		VisualConsensusClusterCdf demo = new VisualConsensusClusterCdf("Consensus Matrix CDF", pathcdf, pathelements,N,kmax,kmin);
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);			
	}
	
	/**
	 * create a figure the cdf value
	 *  
	 * @param nameCdf file path containing cdf value
	 * @param nameElements file path containing elements value
	 * @param parameters input parameters of compuation consensus cluster
	 * @see InputMeasure
	 * @param N number of gene
	 */
	public void visualCDF(String nameCdf, String nameElements, InputMeasure parameters, int N)throws FileNotFoundException, IOException, NumberFormatException,	Exception
	{
		VisualConsensusClusterCdf demo = new VisualConsensusClusterCdf("Consensus Matrix CDF", nameCdf, nameElements,parameters, N);
		demo.pack();
		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);			
	}
	
}
