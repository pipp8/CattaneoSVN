package valworkbench.measures.internal.WCSS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.StringTokenizer;

import valworkbench.datatypes.DataMatrix;
import valworkbench.datatypes.HeaderData;
import valworkbench.datatypes.InputMeasure;
import valworkbench.datatypes.MeasureVector;
/**
 * the WCSSGeneric class provides an object that encapsulates methods and all the 
 * informations for computing Within Clusters Sum of Squares. This class 
 * takes in input a Data Matrix and a InputMeasure object. 
 * Values of the measure are returned in a MeasureVector object.
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0
 * @see WithinClustersSumSquares
 */
public class WCSSGeneric extends WithinClustersSumSquares {
	private final String MEASURE_NAME = "WCSS Generic";
		
	/**
	 * Default constructor
	 *
	 */
	public WCSSGeneric()
	{
		//Default Constructor
	}
	
	/**
	 * Class constructor specifying input data matrix and input parameters
	 * 
	 * @param dataMatrix input data matrix
	 * @see DataMatrix
	 * @param mParameters measure parameters
	 * @see InputMeasure
	 */
	public WCSSGeneric(DataMatrix dataMatrix, InputMeasure mParameters)
	{
		this.parameters = mParameters;
		this.dataMatrix = dataMatrix;
	}
	
	/**
	 * Computes measure values
	 *
	 * @return a Measure_Vector object containing measure values
	 * @see MeasureVector
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	public MeasureVector computeMeasure() throws FileNotFoundException,
			IOException, NumberFormatException, Exception {
		
		this.wCSS = new MeasureVector((parameters.getKMax()- parameters.getKMin())+1);
		String commandLine = null; 
		int exitValue;
		
		this.dataMatrix.storeToFile(parameters.getAlgInputPath()); //OK
		
		for (int k = parameters.getKMin(); k <= parameters.getKMax(); k++)
		{
			String tempPath = parameters.getAlgOutputPath()+"OutCLS_"+Integer.toString(k)+".txt";
			System.out.println("Execute WCSS Generic for "+Integer.toString(k)+" Clusters");
			if (parameters.isInitExtFlag())
			{
				commandLine = composeCmndLine(k, "\""+parameters.getInitExtAlgPath()+"\"", "\""+parameters.getInitExtInpPath()+"\"", 
						"\""+parameters.getInitExtOutPath()+"\"", null, 0, parameters.getInitExtCommandLine(), false);
				
				exitValue = this.executeAlgorithm(commandLine, parameters.getInitExtOutPath());
				if (exitValue == 1)
				{
					parameters.setInitExtFlag(false);
				}
			}
			commandLine = composeCmndLine(k, "\""+parameters.getAlgorithmPath()+"\"", "\""+parameters.getAlgInputPath()+"\"", "\""+tempPath+"\"", 
					"\""+parameters.getInitExtOutPath()+"\"", 0, parameters.getAlgCommandLine(), parameters.isInitExtFlag());
			
			exitValue = this.executeAlgorithm(commandLine, tempPath); 
			
			if (exitValue == 1)
			{
				this.wCSS.setMeasureValue((k-parameters.getKMin()), Double.NaN);
				this.wCSS.setNOfCluster((k-parameters.getKMin()), k);
			}
			else
			{
				this.clusterMatrix.loadFromFile(tempPath);
				this.wCSS.setMeasureValue((k-parameters.getKMin()), this.computeW(clusterMatrix));
				this.wCSS.setNOfCluster((k-parameters.getKMin()), k);
			}
					
		}
		
		this.headerData = this.writeHeader();
		
		if(this.parameters.isPredictNOfCluster())
		{
			this.gWCSS = this.computeGWCSS(this.wCSS);
			this.bestNumberOfCluster = this.estimatedNumberOfCluster(this.gWCSS);
			this.gWCSS.storeToFile(parameters.getOutputPath()+File.separator+"GeometricWCSSDiff.txt");
			this.bestNumberOfCluster.storeToFile(parameters.getOutputPath()+File.separator+"BestNumberOfCluster.txt");
		}
		

		this.headerData.storeToFile(parameters.getOutputPath()+File.separator+"Measure_Header.vhf");
		this.wCSS.storeToFile(parameters.getOutputPath()+File.separator+"Measure_Values.txt");
				
		return this.wCSS;
	}
	
	/**
	 * Collects informations about the experiment.
	 * 
	 * @return the header data for the experiment
	 * @see HeaderData
	 */
	protected HeaderData writeHeader() {
		HeaderData header = new HeaderData();
		String stringParam = null;
		
		if (this.parameters.isInitExtFlag())
		{
			header.setAlgorithmName(this.parameters.getAlgorithmName()+" init. "+this.parameters.getInitAlgName()); 
		}
		else
		{
			header.setAlgorithmName(this.parameters.getAlgorithmName());
		}
		stringParam = Integer.toString(this.parameters.getKMin())+"_"+Integer.toString(this.parameters.getKMax()); 
		String algParam = "";
		StringTokenizer st = new StringTokenizer(this.parameters.getAlgCommandLine()); 
		for (int i = 0; i <= st.countTokens(); i++)
		{
			String temp = st.nextToken();
			if (temp.equals("<inputfile>"));
			else if (temp.equals("<outputfile>"));
			else if (temp.equals("<nofcluster>"));
			else if (temp.equals("<extinit>"))
			{
				StringTokenizer  stInit = new StringTokenizer(this.parameters.getInitExtCommandLine());
				for (int j = 0; stInit.hasMoreTokens(); j++)
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
		header.setAlgParameters(stringParam+" "+algParam); 
		header.setDatasetName(this.parameters.getDatasetName());
		header.setDatasetType(this.parameters.getDatasetType());	
		header.setDateTime();
		header.setMeasureName(this.MEASURE_NAME);
		header.setMeasParameters(" - ");
		return header;
	}
		
}
