/*
 * $Id$
 * $LastChangedDate: 2017-04-09 00:31:23 +0200(Dom, 09 Apr 2017) $
 */

package valworkbench.measures;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.StringTokenizer;

import valworkbench.datatypes.HeaderData;
import valworkbench.datatypes.MeasureVector;
/**
 * The Measure class is the base class for all measures. A Measure 
 * Object encapsulates all the informations needed for a measure. 
 * These informations include:
 * <UL>
 * <LI>A field MEASURE_NAME containing the name of the measure
 * <LI>An input Data Matrix
 * <LI>A measure vector containing measure values
 * <LI>A data header containing data collected in experiment
 * </UL>
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0
 * 
 */
public abstract class Measure {
	private static Process process;
	/**
	 * Computes Measure values
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	public abstract MeasureVector computeMeasure() throws FileNotFoundException, IOException, NumberFormatException, Exception;

	
	protected String[] parseArgouments(String cmnd, String outputPath) 
	{   
	    //Process process;     
	    this.process = null;     
	    
	    //String Tokenizer on double quote
	    StringTokenizer stCommand1 = new StringTokenizer(cmnd, "\"");
	    String cmndVector[] = new String[stCommand1.countTokens()];
	    int i = 0;
	    while(stCommand1.hasMoreTokens())
	    {
	    	cmndVector[i++] = stCommand1.nextToken();
	    }
	    
	    //String Tokenizer on command vector
	    int j = 0;
	    String cmndMatrix[][] = new String[cmndVector.length][];
	    while(j < cmndVector.length)
	    {
	    	if((cmndVector[j].startsWith(" ")) || (cmndVector[j].endsWith(" ")))
	    	{
	    		if(cmndVector[j].length()>1)
	    		{
	    			StringTokenizer stCommand2 = new StringTokenizer(cmndVector[j]);
	    			cmndMatrix[j] = new String[stCommand2.countTokens()];
	    			int s = 0;
	    			while(stCommand2.hasMoreTokens())
	    			{
	    				String temp = stCommand2.nextToken();
	    				cmndMatrix[j][s++] = temp;
	    			}
	    		}
	    		else
	    		{
	    			//Do Nothing
	    		}
	    	}
	    	else
	    	{
	    		cmndMatrix[j] = new String[1];
	    		cmndMatrix[j][0] = cmndVector[j]; 
	    	}
	    	j++;
	    }
	    
	    //Convert command matrix in a vector
	    //Count Total number of parameters
	    int numberParam = 0;
	    for(int t = 0; t < cmndVector.length; t++)
	    {
	    	if(cmndMatrix[t]!=null)
	    		numberParam += cmndMatrix[t].length;
	    		
	    }
	    
	    //Linearize cmndMatrix in a vector
	    int index = 0;
	    String cmdVectorFinal[] = new String[numberParam];
	    for(int t = 0; t < cmndVector.length; t++)
	    {
	    	for(int r = 0; (cmndMatrix[t]!=null)&&(r < cmndMatrix[t].length); r++)
	  
	    		cmdVectorFinal[index++] = cmndMatrix[t][r];
	    }
	    return cmdVectorFinal;
	}     
	    

	/**
	 * Executes external clustering Algorithm
	 * 
	 * @param cmnd the command line for algorithm execution
	 * @param outputPath the output path used from the external algorithm
	 * @return an integer representing exit code, equals to <code>0</code>
	 * if algorithm terminates without error, equals to <code>1</code> if
	 * algorithm terminates with error or an Exception occurs
	 */
	@SuppressWarnings("static-access")
	public int executeAlgorithm(String cmnd, String outputPath) throws IllegalThreadStateException 
	{   
	    int exitCode = 0;  
	    // can we use a local variable ???
	    Process localProcRef = null;
	    
	    try
		{
//	    	String[] cmdLine = parseArgouments(cmnd, outputPath);
//	    	for(String x : cmdLine)
//	    		System.out.printf("%s ", x);
//	    	System.out.println();
	    	
	    	localProcRef = Runtime.getRuntime().exec(parseArgouments(cmnd, outputPath));
	    	exitCode = localProcRef.waitFor(); 
		}
	    catch(Exception x)
	    {
	    	System.err.printf("Error while executing ext Algorithm: %s -> %s\n", cmnd, x.getMessage());
	    	exitCode = 1;
	    }
	    
	    try {	    
	    	exitCode = localProcRef.exitValue();
	    }
	    catch(IllegalThreadStateException itse) {
	    	System.err.println("Illegal Thread state exception: " + itse.getMessage());
	    	exitCode = 1;
	    }
	    
	    if (exitCode != 0)
	    {
	    	File output = new File(outputPath);
	    	if (output.exists())
	    	{
	    		if (output.length() == 0)
	    		{
	    		    exitCode = 1;
	    		}
	    		else
	    		{
	    			exitCode = 0;
	    		}
	    	}
	    }
	    return exitCode;
	}	
	/**
	 * Interprets the command line tags and composes the correct command line for 
	 * extern algorithm execution. Tags include:
	 * <UL>
	 * <LI><code>"<"inputfile">"</code> represents input file path name
	 * <LI><code>"<"outputfile">"</code> represents output file path name
	 * <LI><code>"<"nofcluster">"</code> represents number of clusters
	 * <LI><code>"<"extinit">"</code> represents path name of external init file
	 * <LI><code>"<"nsteps">"</code> represents a parameter for convergence
	 * <UL>
	 * 
	 * @param k number of clusters required in command line 
	 * @param algPath path of the external algorithm 
	 * @param inputPath path of data matrix input file
	 * @param outputPath path of clustering solution
	 * @param initPath path of external initializzation file 
	 * @param nsteps number of steps for algorithm convergence
	 * @param commandLine the command line to interpret
	 * @param initFlag flag for external initialization
	 * @return the command line interpreted
	 */
	public String composeCmndLine(int k, String algPath, String inputPath, String outputPath, String initPath, int nsteps,
			String commandLine, boolean initFlag) 
	{	
		String command = "";
		String token;
		command = command.concat(algPath);
		StringTokenizer sCmnd = new StringTokenizer(commandLine);

		while (sCmnd.hasMoreTokens())
		{
			token = sCmnd.nextToken();

			if (token.equals("<inputfile>"))
			{
				command = command.concat(" "+inputPath);
			}
			else if (token.equals("<outputfile>"))
			{
				command = command.concat(" "+outputPath);
			}
			else if (token.equals("<nofcluster>"))
			{
				command = command.concat(" "+Integer.toString(k));
			}
			else if ((token.equals("<extinit>")) & (initFlag))
			{
				command = command.concat(" "+initPath);
			}
			else if (token.equals("<nsteps>"))
			{
				command = command.concat(" "+nsteps);
			}
			else  
			{
				if (token.equals("<extinit>"))
				{
					//NoP
				}
				else
				{
					command = command.concat(" "+token); 
				}
			}
		}
		return command;
	}
	
	/**
	 * Object process accessor method
	 * 
	 * @return a reference to the process object
	 */
//	public static Process getProcess() {
//		return process;
//	}
	/**
	 * Collects information about the experiment.
	 * 
	 * @return the header data for the experiment
	 * @see HeaderData
	 */
	protected abstract HeaderData writeHeader();
		
}
