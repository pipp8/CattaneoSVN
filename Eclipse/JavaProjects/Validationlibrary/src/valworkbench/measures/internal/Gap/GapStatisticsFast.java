package valworkbench.measures.internal.Gap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.StringTokenizer;

import valworkbench.datatypes.DataMatrix;
import valworkbench.datatypes.HeaderData;
import valworkbench.datatypes.InputMeasure;
import valworkbench.datatypes.MeasureVector;
import valworkbench.measures.internal.WCSS.WCSSFast;
import valworkbench.nullmodels.NullModels;

/**
 * The GapStatisticsFast class provides an Object that encapsulates methods and all the 
 * informations for computing Gap Statistics Fast. This measure takes in input 
 * a data matrix and a set of parameters collected through an 
 * InputMeasure object. Values of the measure are returned in a Measure_Vector object.
 * In this class clustering solutions submitted to validation are obtained through the 
 * following scheme: first solution is produced from the external clustering algorithm, 
 * the next ones are obtained by merging  two clusters belonging to the last
 * clustering solution obtained from the algorithm. The external clustering algorithm is 
 * again executed when the required number of clusters is a multiple of the integer value 
 * contained in the numberOfIteration field, or it is always executed if the percentage of 
 * the number of steps used to reach the convergence is higher than zero. 
 * 
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0
 * @see GapStatistics
 *
 */
public class GapStatisticsFast extends GapStatistics {
	private final String MEASURE_NAME = "Gap Statistics Fast";
	private WCSSFast wKFastIndex;
	
	/**
	 * Default constructor method
	 *
	 */
	public GapStatisticsFast()
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
	public GapStatisticsFast(DataMatrix dataMatrix, InputMeasure mParameters)
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
		
		//Istantiates Variables
		int iteration = ((this.parameters.getKMax() - this.parameters.getKMin()) + 1);
		this.wCSSbK = new double[this.parameters.getNumberOfIteration()][iteration];
		this.gapRun = new MeasureVector(iteration);
		this.gapValue = new MeasureVector(iteration);
		this.wCSSK = new double[iteration];
		this.wKFastIndex = new WCSSFast(dataMatrix, parameters);
		
		NullModels nModel = new NullModels(dataMatrix, this.decomposition, this.parameters.getResample(), this.parameters.getAlgorithmPath());
		
		//Computes Wk Index
		System.out.println("Compute Wk on original Data\n");
		this.wK = wKFastIndex.computeMeasure();
				
		//Computes log of Wk index
		for(int i = 0; i < this.wK.getSizeMVector(); i++){
			this.wCSSK[i] = Math.log(this.wK.getMeasureValue(i));
		}
				
		for(int i = 0; i < iteration; i++)
		{
			this.gapRun.setNOfCluster(i, (i + parameters.getKMin()));
		}
		
		for(int n = 0; n < this.parameters.getNumberOfRun(); n++)
		{
			System.out.println("Compute Gap Statistics Run Number "+Integer.toString(n)+"\n");
							
			//Resampling
			for(int b = 0; b < this.parameters.getNumberOfResample(); b++)
			{
				DataMatrix resampleData = new DataMatrix();
				System.out.println("\n");
				System.out.println("Compute Wk on Resample number "+Integer.toString(b)+"\n");
				//Resamples data
				resampleData = nModel.resample();
				//Computes Wk on resample data
				WCSSFast wbKIndex = new WCSSFast(resampleData, this.parameters);
				this.wbK = wbKIndex.computeMeasure();
			
				for(int i = 0; i < this.wbK.getSizeMVector(); i++){
					this.wCSSbK[b][i] = Math.log(this.wbK.getMeasureValue(i)); 
				}		
			}
			//End Resampling
		
			//Computes Gap Statistics 
			System.out.println("\n");
			System.out.println("Compute Gap Statistics\n"); 
			this.computeGapStatistics(this.wCSSK);
												
			File fileMeasure = new File(this.parameters.getOutputPath()+File.separator+"Measure_Values.txt");
			File fileHeader = new File(this.parameters.getOutputPath()+File.separator+"Measure_Header.vhf");
			fileMeasure.delete();
			fileHeader.delete();
			
			//writes Measure Vector
			this.gapValue.storeToFile(this.parameters.getOutputPath()+File.separator+"GapValues_Run_"+Integer.toString(n)+".txt");
		
			//writes Best Gap
			this.storeBestGap(this.parameters.getCurrentAuxDir()+File.separator+"BestGap_Run_"+Integer.toString(n)+".txt", this.selectBestK());
			this.gapRun.setMeasureValue(this.selectBestK(), (this.gapRun.getMeasureValue(this.selectBestK())+1));
						
		} //for (On number of Iteration)
			
		//writes Header File
		this.headerData = this.writeHeader();
		this.headerData.storeToFile(parameters.getOutputPath()+File.separator+"Measure_Header.vhf");
		this.gapRun.storeToFile(parameters.getOutputPath()+File.separator+"Measure_Values.txt");
				
		return this.gapRun;
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
		stringParam = Integer.toString(this.parameters.getKMin())+"-"+Integer.toString(this.parameters.getKMax()); 
		String algParam = "";
		StringTokenizer st = new StringTokenizer(this.parameters.getAlgCommandLine()); 
		for (int i = 0; i <= st.countTokens(); i++)
		{
			String temp = st.nextToken();
			if (temp.equals("<inputfile>"));
			else if (temp.equals("<outputfile>"));
			else if (temp.equals("<nofcluster>"));
			else if (temp.equals("<extinit>"));
			else if (temp.equals("<nsteps>"));
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
		
		String resample;
		if(parameters.getResample().equals("m")){
			resample = "Poisson Sampling";
		}
		else if (parameters.getResample().equals("c"))
		{
			resample = "Column Sampling";
		}
		else
		{
			resample = "Principal Component Sampling";
		}
		header.setMeasParameters(" Number of Run: "+this.parameters.getNumberOfRun()+" Iteration: "+this.parameters.getNumberOfIteration()+" Resample type: "+resample);
		
		return header;
	}
	
}
