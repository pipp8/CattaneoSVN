package valworkbench.exceptions;
/**
 * Signals that the execution 
 * of the specified algorithm has failed
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0 
 */
public class ErrorAlgorithmException extends Exception {
	
	private static final long serialVersionUID = -5017754244466334952L;
	/**
	 * Default constructor method
	 *
	 */
	public ErrorAlgorithmException()
	{
		super("An error has occurred! \n Not valid execution of clustering Algorithm! \n Program will Terminate");
	}
	
	public String toString()
	{
		return getMessage();
	}

}
