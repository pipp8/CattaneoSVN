package valworkbench.measures.internal.diffFOM;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.StringTokenizer;

import valworkbench.datatypes.DataMatrix;
import valworkbench.datatypes.HeaderData;
import valworkbench.datatypes.InputMeasure;
import valworkbench.datatypes.MeasureVector;
import valworkbench.measures.internal.FOM.FOMGeneric;
import valworkbench.measures.internal.KL.KrzanowskiLai;

public class DiffFOMGeneric extends KrzanowskiLai {
	private final String MEASURE_NAME = "DiffFOM Generic";
	/**
	 * Default constructor
	 *
	 */
	public DiffFOMGeneric()
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
	public DiffFOMGeneric(DataMatrix dataMatrix, InputMeasure mParameters)
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
		int upperLimit = parameters.getKMax();
		@SuppressWarnings("unused")
		int lowerLimit = parameters.getKMin();
		parameters.setKMax(upperLimit+1);
		
		this.intIndex = new MeasureVector((parameters.getKMax()- parameters.getKMin())+1);
		this.estimateNCluster = new MeasureVector(1);
		
		FOMGeneric fomGen = new FOMGeneric(dataMatrix, parameters);
		this.intIndex = fomGen.computeMeasure();
		parameters.setKMax(upperLimit);
		
		this.kLk = new MeasureVector((parameters.getKMax()- parameters.getKMin())+1);
				
		File deleter = new File(parameters.getOutputPath()+File.separator+"Measure_Header.vhf");
		deleter.delete();
		deleter = new File(parameters.getOutputPath()+File.separator+"Measure_Values.txt");
		deleter.delete();
		
		System.out.println("Compute Diff(k) for FOM index");
		
		this.kLk.setMeasureValue(this.computeDiffK(this.intIndex, this.dataMatrix.getNOfFeature()));
		for(int i = 0; i < this.kLk.getNOfEntries(); i++)
		{
			kLk.setNOfCluster(i, this.intIndex.getNOfCluster(i+1));
		}
				
		System.out.println("Estimate number of clusters");
		this.estimateNCluster = this.estimatedNumberOfCluster(this.kLk);
		this.headerData = this.writeHeader();
		this.headerData.storeToFile(parameters.getOutputPath()+File.separator+"Measure_Header.vhf");
		this.kLk.storeToFile(parameters.getOutputPath()+File.separator+"Measure_Values.txt");
		this.estimateNCluster.storeToFile(parameters.getOutputPath()+File.separator+"nOfCluster_Value.txt");	
				
		return kLk;
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
		header.setMeasParameters(" - ");
		return header;
	}

}
