package valworkbench.measures.internal.FOM;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.StringTokenizer;

import valworkbench.datatypes.DataMatrix;
import valworkbench.datatypes.HeaderData;
import valworkbench.datatypes.InputMeasure;
import valworkbench.datatypes.MeasureVector;
/**
 * The FOMGeneric class provides an Object that encapsulates methods and all the 
 * informations for computing Figure Of Merit. This measure takes in input 
 * a data matrix and a set of parameters collected through an InputMeasure object. 
 * Values of the measure are returned in a MeasureVector object.
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0
 * @see FigureOfMerit 
 *
 */
public class FOMGeneric extends FigureOfMerit {
	private final String MEASURE_NAME = "FOM Generic";
	/**
	 * Default Constructor
	 *
	 */	
	public FOMGeneric()
	{
		//Default constructor method
	}
	/**
	 * Class constructor specifying input data matrix and input parameters
	 * 
	 * @param dataMatrix input data matrix
	 * @see DataMatrix
	 * @param mParameters measure parameters
	 * @see InputMeasure
	 */
	public FOMGeneric(DataMatrix dataMatrix, InputMeasure mParameters)
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
	public MeasureVector computeMeasure() throws FileNotFoundException, IOException, NumberFormatException, Exception
	{
		this.aggregateFOMValue = new MeasureVector((parameters.getKMax()- parameters.getKMin())+1);
		DataMatrix patMatFom = new DataMatrix();
		String commandLine = null;
		int exitValue;
				
		this.fOMekMatrix = new double[(parameters.getKMax()- parameters.getKMin())+1][dataMatrix.getNOfFeature()];
		
		for (int e = 0; e < dataMatrix.getNOfFeature(); e++)
		{
			patMatFom = dataMatrix.removeFeature(e);
			System.out.println("Execute FOM Generic feature "+e);
			
			patMatFom.storeToFile(parameters.getAlgInputPath());
			
			for (int k = parameters.getKMax(); k >= parameters.getKMin(); k--)
			{
				if(e == 0)
				{
					this.aggregateFOMValue.setNOfCluster((parameters.getKMax()- parameters.getKMin())-(parameters.getKMax()- k), k);
				}
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
				
				if (exitValue == 1)
				{
					this.fOMekMatrix[(parameters.getKMax()- parameters.getKMin())-(parameters.getKMax()- k)][e] = Double.NaN;
				}
				else if (exitValue == 0)
				{
					this.clusterMatrix.loadFromFile(tempPath);
					
					if (parameters.isAdjustmentFactor() & this.adjFactor(this.dataMatrix.getNOfItem(), k)!=Double.NaN)
						this.fOMekMatrix[(parameters.getKMax()- parameters.getKMin())-(parameters.getKMax()- k)][e] = this.fOMek(e, k)/this.adjFactor(this.dataMatrix.getNOfItem(), k);
					else this.fOMekMatrix[(parameters.getKMax()- parameters.getKMin())-(parameters.getKMax()- k)][e] = this.fOMek(e, k);
				}
			}
			patMatFom = null;
		}
		
		this.aggregateFOMValue.setMeasureValue(this.aggregateFOM());
				
		this.headerData = this.writeHeader();
		
		if(this.parameters.isPredictNOfCluster())
		{
			this.gFOM = this.computeGFOM(this.aggregateFOMValue);
			this.bestNumberOfCluster = this.estimatedNumberOfCluster(this.gFOM);
			this.gFOM.storeToFile(parameters.getOutputPath()+File.separator+"GeometricFOMDiff.txt");
			this.bestNumberOfCluster.storeToFile(parameters.getOutputPath()+File.separator+"BestNumberOfCluster.txt");
		}
		
		this.headerData.storeToFile(parameters.getOutputPath()+File.separator+"Measure_Header.vhf");
		this.aggregateFOMValue.storeToFile(parameters.getOutputPath()+File.separator+"Measure_Values.txt");
						
		return this.aggregateFOMValue;   
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
		if (this.parameters.isAdjustmentFactor())header.setMeasParameters("Adjustment Factor yes");
		else header.setMeasParameters("Adjustment Factor no");
		return header;
	}
		
}	