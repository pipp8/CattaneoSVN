package valworkbench.measures.external.nullmeasures;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.StringTokenizer;

import valworkbench.datatypes.DataMatrix;
import valworkbench.datatypes.HeaderData;
import valworkbench.datatypes.InputMeasure;
import valworkbench.datatypes.MeasureVector;
import valworkbench.measures.Measure;
/**
 * The NullMeasureGeneric class provides an object that encapsulates methods and all the
 * informations for computing Null Measure. This class takes in input a Data 
 * Matrix and an InputMeasure object. Values of the measure are equal to <code>NaN</code> 
 * and are returned in a MeasureVector object 
 * The effect of computing such a measure is basically to produce clustering 
 * solutions with the given algorithm A and Benchmark dataset D  
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0
 * @see Measure
 */
public class NullMeasureGeneric extends NullMeasure{
	private final String MEASURE_NAME = "Null Measure Generic";
	
	/**
	 * Default constructor
	 *
	 */
	public NullMeasureGeneric()
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
	public NullMeasureGeneric(DataMatrix dataMatrix, InputMeasure mParameters)
	{
		this.parameters = mParameters;
		this.dataMatrix = dataMatrix;
	}
	
	/**
	 * Computes measure values
	 *
	 * @return a Measure_Vector object containing measure values
	 * equal to <code>NaN</code>
	 * @see MeasureVector
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	public MeasureVector computeMeasure() throws FileNotFoundException, IOException, NumberFormatException, Exception
	{
		this.nullValue = new MeasureVector((parameters.getKMax()- parameters.getKMin())+1); 
		String commandLine = null; 
		int exitValue;
		
		this.dataMatrix.storeToFile(parameters.getAlgInputPath());
				
		for (int k = parameters.getKMin(); k <= parameters.getKMax(); k++)
		{
			System.out.println("Compute Null Measure Generic for "+Integer.toString(k)+" Clusters");
			String tempPath = parameters.getAlgOutputPath()+"OutCLS_"+Integer.toString(k)+".txt";
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

			this.nullValue.setNOfCluster((k-parameters.getKMin()), k);
			this.nullValue.setMeasureValue((k-parameters.getKMin()), Double.NaN);
			
		}
		
		this.headerData = this.writeHeader();

		this.headerData.storeToFile(parameters.getOutputPath()+File.separator+"Measure_Header.vhf");
		this.nullValue.storeToFile(parameters.getOutputPath()+File.separator+"Measure_Values.txt");
		
		return this.nullValue;
	}
	/**
	 * Collects informations about the experiment.
	 * 
	 * @return the header data for the experiment
	 * @see HeaderData
	 */
	protected HeaderData writeHeader()
	{
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
		for (int i = 0; st.hasMoreTokens(); i++)
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
