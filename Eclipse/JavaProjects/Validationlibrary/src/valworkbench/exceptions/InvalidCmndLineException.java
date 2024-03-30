package valworkbench.exceptions;
/**
 * Signals that an invalid command line has been
 * entered 
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0 
 */
public class InvalidCmndLineException extends Exception {
	private static final long serialVersionUID = -1899534055827926946L;
	/**
	 * Default constructor method
	 *
	 */
	public InvalidCmndLineException()
	{
		super("Unknow Argument ");
	}
	/**
	 * 
	 */
	public String toString()
	{
		return getMessage()+" Error!";
	}
}
