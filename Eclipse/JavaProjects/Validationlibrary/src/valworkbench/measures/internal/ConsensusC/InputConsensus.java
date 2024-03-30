package valworkbench.measures.internal.ConsensusC;

import valworkbench.datatypes.InputMeasure;
import valworkbench.exceptions.DirNotFoundException;
import valworkbench.exceptions.FileAlgNotFoundException;
import valworkbench.exceptions.InvalidCmndLineException;
import valworkbench.exceptions.NotValidOptionException;

public class InputConsensus extends InputMeasure {
	/**
	 * Default constructor method
	 *
	 */
	public InputConsensus() {
		
		// Default Constructor
	}
	/**
	 * Constructor method that provide input parameters for Consensus Measure execution
	 * 
	 * @param kMin an integer representing minimum number of clusters required
	 * @param kMax an integer representing maximum number of clusters required
	 * @param algorithmName the algorithm name
	 * @param datasetName the dataset name
	 * @param datasetType the dataset type
	 * @param algorithmPath algorithm path
	 * @param algCommandLine algorithm command line 
	 * @param algInputPath input path for algorithm
	 * @param algOutputPath output path for algorithm
	 * @param outputPath measure output path
	 * @param percentage
	 * @param numberOfIteration
	 * @param hierarchical
	 * @param replaceAk
	 * @throws NotValidOptionException
	 * @throws FileAlgNotFoundException
	 * @throws InvalidCmndLineException
	 * @throws DirNotFoundException
	 */
	public InputConsensus(int kMin, int kMax, String algorithmPath, String algCommandLine, String outputPath, 
			String algInputPath, String algOutputPath, int percentage, int numberOfIteration, boolean hierarchical, 
			boolean replaceAk) 
	 throws NotValidOptionException, FileAlgNotFoundException, InvalidCmndLineException, DirNotFoundException
	{
		this.setMeasureName("Consensus Clustering");
		this.setKMin(kMin);
		this.setKMax(kMax);
		this.setAlgorithmPath(algorithmPath);
		this.setAlgCommandLine(algCommandLine);
		this.setOutputPath(outputPath);
		this.setAlgInputPath(algInputPath);
		this.setAlgOutputPath(algOutputPath);
				
		this.setAlgorithmName(this.extractName(algorithmPath));
		this.setDatasetName(this.extractName(algInputPath));
		
		this.setPercentage(percentage);
		this.setNumberOfIteration(numberOfIteration);
		this.setHierarchical(hierarchical);
		this.setReplaceAk(replaceAk);
		
	}
	/**
	 * Constructor method that provide input parameters for Consensus Measure execution
	 * 
	 * @param kMin an integer representing minimum number of clusters required
	 * @param kMax an integer representing maximum number of clusters required
	 * @param algorithmName the algorithm name
	 * @param datasetName the dataset name
	 * @param datasetType the dataset type
	 * @param algorithmPath algorithm path
	 * @param algCommandLine algorithm command line 
	 * @param algInputPath input path for algorithm
	 * @param algOutputPath output path for algorithm
	 * @param outputPath measure output path
	 * @param percentage
	 * @param numberOfIteration
	 * @param hierarchical
	 * @param replaceAk
	 * @param initExtFlag
	 * @param initExtAlgPath
	 * @param initExtCommandLine
	 * @param initExtInpPath
	 * @param initExtOutPath
	 * @throws NotValidOptionException
	 * @throws FileAlgNotFoundException
	 * @throws InvalidCmndLineException
	 * @throws DirNotFoundException
	 */
	public InputConsensus(int kMin, int kMax, String algorithmPath, String algCommandLine, String outputPath, 
			String algInputPath, String algOutputPath, int percentage, int numberOfIteration, boolean hierarchical, 
			boolean replaceAk, boolean initExtFlag, String initExtAlgPath, String initExtCommandLine, 
			String initExtInpPath, String initExtOutPath) 
	 throws NotValidOptionException, FileAlgNotFoundException, InvalidCmndLineException, DirNotFoundException
	{
		this.setMeasureName("Consensus Clustering");
		this.setKMin(kMin);
		this.setKMax(kMax);
		this.setAlgorithmPath(algorithmPath);
		this.setAlgCommandLine(algCommandLine);
		this.setOutputPath(outputPath);
		this.setAlgInputPath(algInputPath);
		this.setAlgOutputPath(algOutputPath);
				
		this.setAlgorithmName(this.extractName(algorithmPath));
		this.setDatasetName(this.extractName(algInputPath));
		this.setInitAlgName(this.extractName(initExtAlgPath));
		
		this.setPercentage(percentage);
		this.setNumberOfIteration(numberOfIteration);
		this.setHierarchical(hierarchical);
		this.setReplaceAk(replaceAk);
		
		this.setInitExtFlag(initExtFlag);
		this.setInitExtAlgPath(initExtAlgPath);
		this.setInitExtCommandLine(initExtCommandLine);
		this.setInitExtInpPath(initExtInpPath);
		this.setInitExtOutPath(initExtOutPath);
		
	}
	

}
