package valworkbench.measures.internal.Gap;

import valworkbench.datatypes.InputMeasure;
import valworkbench.exceptions.DirNotFoundException;
import valworkbench.exceptions.FileAlgNotFoundException;
import valworkbench.exceptions.InvalidCmndLineException;
import valworkbench.exceptions.NotValidOptionException;

public class InputGap extends InputMeasure {
	/**
	 * Default constructor method
	 *
	 */
	public InputGap()
	{
		//Default constructor method
	}
	/**
	 * Constructor method that provide input parameters for Gap Measure execution
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
	 * @param algInputPath
	 * @param algOutputPath
	 * @param numberOfIteration
	 * @param resample
	 * @param numberOfRun
	 * @throws NotValidOptionException
	 * @throws FileAlgNotFoundException
	 * @throws InvalidCmndLineException
	 * @throws DirNotFoundException
	 */
	public InputGap(int kMin, int kMax, String algorithmPath, String algCommandLine, String outputPath, 
			String algInputPath, String algOutputPath, int numberOfIteration, String resample, int numberOfRun) 
	 throws NotValidOptionException, FileAlgNotFoundException, InvalidCmndLineException, DirNotFoundException
	{
		this.setMeasureName("Gap Statistics Generic");
				
		this.setKMin(kMin);
		this.setKMax(kMax);
		this.setAlgorithmPath(algorithmPath);
		this.setAlgCommandLine(algCommandLine);
		this.setOutputPath(outputPath);
		this.setAlgInputPath(algInputPath);
		this.setAlgOutputPath(algOutputPath);
		
		this.setAlgorithmName(this.extractName(algorithmPath));
		this.setDatasetName(this.extractName(algInputPath));
		
		this.setNumberOfIteration(numberOfIteration);
		this.setResample(resample);
		this.setNumberOfRun(numberOfRun);
		
	}
	/**
	 * Constructor method that provide input parameters for Gap Measure index 
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
	 * @param initAlgName the initialization algorithm name
	 * @param initExtAlgPath path of external initialization algorithm
	 * @param initExtCommandLine initialization algorithm command line
	 * @param initExtInpPath initialization external algorithm input path 
	 * @param initExtOutPath initialization external algorithm output path
	 * @param numberOfIteration
	 * @param resample
	 * @param numberOfRun
	 * @throws NotValidOptionException
	 * @throws FileAlgNotFoundException
	 * @throws InvalidCmndLineException
	 * @throws DirNotFoundException
	*/
	public InputGap(int kMin, int kMax, String algorithmPath, String algCommandLine, String outputPath, 
			String algInputPath, String algOutputPath, boolean initExtFlag, String initExtAlgPath,
			String initExtCommandLine, String initExtInpPath, String initExtOutPath, int numberOfIteration,
			String resample, int numberOfRun) 
	throws NotValidOptionException, FileAlgNotFoundException, InvalidCmndLineException, DirNotFoundException
	{
		this.setMeasureName("Gap Statistics Generic");
				
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
		
		this.setNumberOfIteration(numberOfIteration);
		this.setResample(resample);
		this.setNumberOfRun(numberOfRun);
	}
	/**
	 * Constructor method that provide input parameters for Gap Fast Measure execution
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
	 * @param numberOfSteps
	 * @param numberOfIteration
	 * @param percentage
	 * @param numberOfResample
	 * @throws NotValidOptionException
	 * @throws FileAlgNotFoundException
	 * @throws InvalidCmndLineException
	 * @throws DirNotFoundException
	 */
	public InputGap(int kMin, int kMax, String algorithmPath, String algCommandLine, String outputPath, 
			String algInputPath, String algOutputPath, int numberOfSteps, 
			int numberOfIteration, int percentage, int numberOfResample,
			String resample, int numberOfRun)
	 throws NotValidOptionException, FileAlgNotFoundException, InvalidCmndLineException, DirNotFoundException
	{
		this.setMeasureName("Gap Statistics Fast");
				
		this.setKMin(kMin);
		this.setKMax(kMax);
		this.setAlgorithmPath(algorithmPath);
		this.setAlgCommandLine(algCommandLine);
		this.setOutputPath(outputPath);
		this.setAlgInputPath(algInputPath);
		this.setAlgOutputPath(algOutputPath);
		
		this.setAlgorithmName(this.extractName(algorithmPath));
		this.setDatasetName(this.extractName(algInputPath));
		
		this.setNumberOfSteps(numberOfSteps);
		this.setNumberOfIteration(numberOfIteration);
		this.setPercentage(percentage);
		this.setNumberOfResample(numberOfResample);
				
	}
	/**
	 * Constructor method that provide input parameters for Gap Hierarchical Measure execution
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
	 * @param numberOfIteration
	 * @param resample
	 * @param numberOfRun
	 * @throws NotValidOptionException
	 * @throws FileAlgNotFoundException
	 * @throws InvalidCmndLineException
	 * @throws DirNotFoundException
	 */
	public InputGap(int kMin, int kMax, String outputPath, 
			String algInputPath, String algOutputPath, String hAlgPath, 
			String type, String outputHierarchical, int numberOfIteration,
			String resample, int numberOfRun) 
	 throws NotValidOptionException, FileAlgNotFoundException, InvalidCmndLineException, DirNotFoundException
	{
		this.setMeasureName("Gap Statistics Hierarchical");
				
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
		
		this.setNumberOfIteration(numberOfIteration);
		this.setResample(resample);
		this.setNumberOfRun(numberOfRun);
		
	}
	/**
	 * Constructor method that provide input parameters for Gap Hierarchical Initialization  Measure execution
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
	 * @param numberOfIteration
	 * @param resample
	 * @param numberOfRun
	 * @throws NotValidOptionException
	 * @throws FileAlgNotFoundException
	 * @throws InvalidCmndLineException
	 * @throws DirNotFoundException
	 */
	public InputGap(int kMin, int kMax, String algorithmPath, String algCommandLine, String outputPath, 
			String algInputPath, String algOutputPath, String hAlgPath, 
			String type, String outputHierarchical, int numberOfIteration,
			String resample, int numberOfRun) 
	 throws NotValidOptionException, FileAlgNotFoundException, InvalidCmndLineException, DirNotFoundException
	{
		this.setMeasureName("Gap Statistics H. Init.");
				
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
		
		this.setNumberOfIteration(numberOfIteration);
		this.setResample(resample);
		this.setNumberOfRun(numberOfRun);
		
	}
}
