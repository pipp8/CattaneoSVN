package valworkbench.measures.internal.KL;

import valworkbench.datatypes.InputMeasure;
import valworkbench.exceptions.DirNotFoundException;
import valworkbench.exceptions.FileAlgNotFoundException;
import valworkbench.exceptions.InvalidCmndLineException;
import valworkbench.exceptions.NotValidOptionException;

public class InputKL extends InputMeasure {
	/**
	 * Default constructor method
	 *
	 */
	public InputKL()
	{
		//Default constructor method
	}
	/**
	 * Constructor method that provide input parameters for KL Measure execution
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
	 * @throws NotValidOptionException
	 * @throws FileAlgNotFoundException
	 * @throws InvalidCmndLineException
	 * @throws DirNotFoundException
	 */
	public InputKL(int kMin, int kMax, String algorithmPath, String algCommandLine, String outputPath, 
			String algInputPath, String algOutputPath) 
	 throws NotValidOptionException, FileAlgNotFoundException, InvalidCmndLineException, DirNotFoundException
	{
		this.setMeasureName("KL Generic");
				
		this.setKMin(kMin);
		this.setKMax(kMax);
		this.setAlgorithmPath(algorithmPath);
		this.setAlgCommandLine(algCommandLine);
		this.setOutputPath(outputPath);
		this.setAlgInputPath(algInputPath);
		this.setAlgOutputPath(algOutputPath);
		
		this.setAlgorithmName(this.extractName(algorithmPath));
		this.setDatasetName(this.extractName(algInputPath));
			
	}
	/**
	 * Constructor method that provide input parameters for KL with external initialization Measure execution
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
	public InputKL(int kMin, int kMax, String algorithmPath, String algCommandLine, String outputPath, 
			String algInputPath, String algOutputPath, boolean initExtFlag,
			String initExtAlgPath, String initExtCommandLine, String initExtInpPath, String initExtOutPath) 
	 throws NotValidOptionException, FileAlgNotFoundException, InvalidCmndLineException, DirNotFoundException
	{
		this.setMeasureName("KL Generic");
				
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
		
		this.setInitExtFlag(initExtFlag);
		this.setInitExtAlgPath(initExtAlgPath);
		this.setInitExtCommandLine(initExtCommandLine);
		this.setInitExtInpPath(initExtInpPath);
		this.setInitExtOutPath(initExtOutPath);
	}
	/**
	 * Constructor method that provide input parameters for WCSS Hierarchical Measure execution
	 * 
	 * @param kMin an integer representing minimum number of clusters required
	 * @param kMax an integer representing maximum number of clusters required
	 * @param algorithmName the algorithm name
	 * @param datasetName the dataset name
	 * @param datasetType the dataset type
	 * @param algInputPath input path for algorithm
	 * @param algOutputPath output path for algorithm
	 * @param outputPath measure output path
	 * @param hAlgPath
	 * @param type
	 * @param outputHierarchical
	 * @throws NotValidOptionException
	 * @throws FileAlgNotFoundException
	 * @throws InvalidCmndLineException
	 * @throws DirNotFoundException
	 */
	public InputKL(int kMin, int kMax, String outputPath, 
			String algInputPath, String algOutputPath, String hAlgPath, String type, String outputHierarchical) 
	 throws NotValidOptionException, FileAlgNotFoundException, InvalidCmndLineException, DirNotFoundException
	{
		this.setMeasureName("KL Hierarchical");
				
		this.setKMin(kMin);
		this.setKMax(kMax);
		
		this.setOutputPath(outputPath);
		this.setAlgInputPath(algInputPath);
		this.setAlgOutputPath(algOutputPath);
		
		this.setAlgorithmName("Inner Hierarchical");
		this.setDatasetName(this.extractName(algInputPath));
		
		this.setHAlgPath(hAlgPath);
		this.setType(type);
		this.setOutputHierarchical(outputHierarchical);
	}
	/**
	 * Constructor method that provide input parameters for WCSS Hierarchical Initialization  Measure execution
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
	 * @param hAlgPath
	 * @param type
	 * @param outputHierarchical
	 * @throws NotValidOptionException
	 * @throws FileAlgNotFoundException
	 * @throws InvalidCmndLineException
	 * @throws DirNotFoundException
	 */
	public InputKL(int kMin, int kMax, String algorithmPath, String algCommandLine, String outputPath, 
			String algInputPath, String algOutputPath, String hAlgPath, String type, String outputHierarchical) 
	 throws NotValidOptionException, FileAlgNotFoundException, InvalidCmndLineException, DirNotFoundException
	{
		this.setMeasureName("KL H. Init.");
				
		this.setKMin(kMin);
		this.setKMax(kMax);
		this.setAlgorithmPath(algorithmPath);
		this.setAlgCommandLine(algCommandLine);
		this.setOutputPath(outputPath);
		this.setAlgInputPath(algInputPath);
		this.setAlgOutputPath(algOutputPath);
		
		this.setAlgorithmName(this.extractName(algorithmPath));
		this.setDatasetName(this.extractName(algInputPath));
		
		this.setHAlgPath(hAlgPath);
		this.setType(type);
		this.setOutputHierarchical(outputHierarchical);
	}
}
