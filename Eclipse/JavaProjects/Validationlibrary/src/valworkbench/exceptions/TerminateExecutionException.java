package valworkbench.exceptions;
/**
 * Signals that an invalid option has been selected
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0 
 */
public class TerminateExecutionException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5225443755010575305L;
	public TerminateExecutionException()
	{
		super("Execution Terminated!");
	}
	/**
	 * Constructor method specifying invalid option and relative field
	 * 
	 */
	public String toString()
	{
		return getMessage(); 
	}
}
