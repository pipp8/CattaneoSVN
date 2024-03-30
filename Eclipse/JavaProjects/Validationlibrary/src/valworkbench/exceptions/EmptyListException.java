package valworkbench.exceptions;
/**
 * Signals that the list 
 * is empty
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0 
 */
public class EmptyListException extends Exception {
	
	private static final long serialVersionUID = -5649564206871331060L;
	/**
	 * Default constructor method 
	 *
	 */
	public EmptyListException()
	{
		super("Error List is Empty");
	}
	/**
	 * Constructor specifying the empty list number
	 * 
	 * @param listNumber an integer representing the empty list number
	 */
	public EmptyListException(int listNumber)
	{
		super("Error List "+listNumber+" is empty");
	}
}
