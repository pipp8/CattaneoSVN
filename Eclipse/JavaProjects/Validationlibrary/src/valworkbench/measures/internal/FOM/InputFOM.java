package valworkbench.measures.internal.FOM;

import valworkbench.datatypes.InputMeasure;
import valworkbench.exceptions.DirNotFoundException;
import valworkbench.exceptions.FileAlgNotFoundException;
import valworkbench.exceptions.InvalidCmndLineException;
import valworkbench.exceptions.NotValidOptionException;

public class InputFOM extends InputMeasure {
	/**
	 * Default constructor method
	 *
	 */
	public InputFOM()
	{
		//Default constructor method
	}
	/**
	 * Constructor method that provide input parameters for FOM Generic Measure execution
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
	 * @param adjustmentFactor
	 * @param predictNOfCluster
	 * @throws NotValidOptionException
	 * @throws FileAlgNotFoundException
	 * @throws InvalidCmndLineException
	 * @throws DirNotFoundException
	 */
	public InputFOM(int kMin, int kMax, String algorithmPath, String algCommandLine, String outputPath, 
			String algInputPath, String algOutputPath, boolean adjustmentFactor, boolean predictNOfCluster) 
	 throws NotValidOptionException, FileAlgNotFoundException, InvalidCmndLineException, DirNotFoundException
	{
		this.setMeasureName("FOM Generic");
				
		this.setKMin(kMin);
		this.setKMax(kMax);
		this.setAlgorithmPath(algorithmPath);
		this.setAlgCommandLine(algCommandLine);
		this.setOutputPath(outputPath);
		this.setAlgInputPath(algInputPath);
		this.setAlgOutputPath(algOutputPath);
		
		this.setAlgorithmName(this.extractName(algorithmPath));
		this.setDatasetName(this.extractName(algInputPath));
		this.setAdjustmentFactor(adjustmentFactor);
		this.setPredictNOfCluster(predictNOfCluster);
		
	}
	/**
	 * Constructor method that provide input parameters for FOM Measure index 
	 * execution with extenal init
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
	 * @param initExtFlag a boolean flag indicating external initialization
	 * @param initExtAlgPath path of external initialization algorithm
	 * @param initExtCommandLine initialization algorithm command line
	 * @param initExtInpPath initialization external algorithm input path 
	 * @param initExtOutPath initialization external algorithm output path
     * @param adjustmentFactor
	 * @param predictNOfCluster
	 * @throws NotValidOptionException
	 * @throws FileAlgNotFoundException
	 * @throws InvalidCmndLineException
	 * @throws DirNotFoundException
	 */
	public InputFOM(int kMin, int kMax, String algorithmPath, String algCommandLine, String outputPath, 
			String algInputPath, String algOutputPath, boolean initExtFlag, String initExtAlgPath,
			String initExtCommandLine, String initExtInpPath, String initExtOutPath, boolean adjustmentFactor, 
			boolean predictNOfCluster) 
	throws NotValidOptionException, FileAlgNotFoundException, InvalidCmndLineException, DirNotFoundException
	{
		this.setMeasureName("FOM Generic");
				
		this.setKMin(kMin);
		this.setKMax(kMax);
		this.setAlgorithmPath(algorithmPath);
		this.setAlgCommandLine(algCommandLine);
		this.setOutputPath(outputPath);
		this.setAlgInputPath(algInputPath);
		this.setAlgOutputPath(algOutputPath);
		
		this.setAlgorithmName(this.extractName(algorithmPath));
		this.setDatasetName(this.extractName(algInputPath));
					
		this.setInitExtFlag(initExtFlag);
		this.setInitExtAlgPath(initExtAlgPath);
		
		this.setInitAlgName(this.extractName(initExtAlgPath));
				
		this.setInitExtCommandLine(initExtCommandLine);
		this.setInitExtInpPath(initExtInpPath);
		this.setInitExtOutPath(initExtOutPath);
		
		this.setAdjustmentFactor(adjustmentFactor);
		this.setPredictNOfCluster(predictNOfCluster);
	}
	/**
	 * Constructor method that provide input parameters for FOM Fast Measure execution
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
	 * @param predictNOfCluster
     * @param numberOfSteps
	 * @param numberOfIteration
	 * @param percentage
	 * @param adjustmentFactor
	 * @throws NotValidOptionException
	 * @throws FileAlgNotFoundException
	 * @throws InvalidCmndLineException
	 * @throws DirNotFoundException
	 */
	public InputFOM(int kMin, int kMax, String algorithmPath, String algCommandLine, String outputPath, 
			String algInputPath, String algOutputPath, boolean predictNOfCluster, int numberOfSteps, 
			int numberOfIteration, int percentage, boolean adjustmentFactor)
	 throws NotValidOptionException, FileAlgNotFoundException, InvalidCmndLineException, DirNotFoundException
	{
		this.setMeasureName("FOM Fast");
				
		this.setKMin(kMin);
		this.setKMax(kMax);
		this.setAlgorithmPath(algorithmPath);
		this.setAlgCommandLine(algCommandLine);
		this.setOutputPath(outputPath);
		this.setAlgInputPath(algInputPath);
		this.setAlgOutputPath(algOutputPath);
		
		this.setAlgorithmName(this.extractName(algorithmPath));
		this.setDatasetName(this.extractName(algInputPath));
		
		this.setPredictNOfCluster(predictNOfCluster);
		this.setNumberOfSteps(numberOfSteps);
		this.setNumberOfIteration(numberOfIteration);
		this.setPercentage(percentage);
		
		this.setAdjustmentFactor(adjustmentFactor);
		
	}
	/**
	 * Constructor method that provide input parameters for FOM Hierarchical Measure execution
	 * 
	 * @param kMin an integer representing minimum number of clusters required
	 * @param kMax an integer representing maximum number of clusters required
	 * @param algorithmName the algorithm name
	 * @param datasetName the dataset name
	 * @param datasetType the dataset type
	 * @param algInputPath input path for algorithm
	 * @param algOutputPath output path for algorithm
	 * @param outputPath measure output path
	 * @param predictNOfCluster
	 * @param hAlgPath
	 * @param type
	 * @param outputHierarchical
	 * @param adjustmentFactor
	 * @throws NotValidOptionException
	 * @throws FileAlgNotFoundException
	 * @throws InvalidCmndLineException
	 * @throws DirNotFoundException
	 */
	public InputFOM(int kMin, int kMax, String outputPath, 
			String algInputPath, String algOutputPath, boolean predictNOfCluster, String hAlgPath, 
			String type, String outputHierarchical, boolean adjustmentFactor) 
	 throws NotValidOptionException, FileAlgNotFoundException, InvalidCmndLineException, DirNotFoundException
	{
		this.setMeasureName("FOM Hierarchical");
				
		this.setKMin(kMin);
		this.setKMax(kMax);
		
		this.setOutputPath(outputPath);
		this.setAlgInputPath(algInputPath);
		this.setAlgOutputPath(algOutputPath);
		
		this.setAlgorithmName("Inner Hierarchical");
		this.setDatasetName(this.extractName(algInputPath));
		
		this.setPredictNOfCluster(predictNOfCluster);
		this.setHAlgPath(hAlgPath);
		this.setType(type);
		this.setOutputHierarchical(outputHierarchical);
		
		this.setAdjustmentFactor(adjustmentFactor);
	}
	/**
	 * Constructor method that provide input parameters for FOM Hierarchical Initialization  Measure execution
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
	 * @param predictNOfCluster
	 * @param hAlgPath
	 * @param type
	 * @param outputHierarchical
	 * @param adjustmentFactor
	 * @throws NotValidOptionException
	 * @throws FileAlgNotFoundException
	 * @throws InvalidCmndLineException
	 * @throws DirNotFoundException
	 */
	public InputFOM(int kMin, int kMax, String algorithmPath, String algCommandLine, String outputPath, 
			String algInputPath, String algOutputPath, boolean predictNOfCluster, String hAlgPath, 
			String type, String outputHierarchical, boolean adjustmentFactor) 
	 throws NotValidOptionException, FileAlgNotFoundException, InvalidCmndLineException, DirNotFoundException
	{
		this.setMeasureName("FOM H. Init.");
				
		this.setKMin(kMin);
		this.setKMax(kMax);
		this.setAlgorithmPath(algorithmPath);
		this.setAlgCommandLine(algCommandLine);
		this.setOutputPath(outputPath);
		this.setAlgInputPath(algInputPath);
		this.setAlgOutputPath(algOutputPath);
		
		this.setAlgorithmName(this.extractName(algorithmPath));
		this.setDatasetName(this.extractName(algInputPath));
		
		this.setPredictNOfCluster(predictNOfCluster);
		this.setHAlgPath(hAlgPath);
		this.setType(type);
		this.setOutputHierarchical(outputHierarchical);
		
		this.setAdjustmentFactor(adjustmentFactor);
	}
	
}
