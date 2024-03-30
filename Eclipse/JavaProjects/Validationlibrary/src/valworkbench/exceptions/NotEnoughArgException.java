package valworkbench.exceptions;
/**
 * Signals that not enough arguments has been entered
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0 
 */
public class NotEnoughArgException extends Exception { 
	
	private static final long serialVersionUID = 4351310810284740819L;
	/**
	 * Default constructor method
	 *
	 */
	public NotEnoughArgException() 
	{
		super("Not enough arguments");
	}
	/**
	 * 
	 */
	public String toString()
	{
		return getMessage()+" Error! "; 
	}
}
