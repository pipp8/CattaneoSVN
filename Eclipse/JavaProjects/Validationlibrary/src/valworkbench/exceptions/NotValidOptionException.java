package valworkbench.exceptions;
/**
 * Signals that an invalid option has been selected
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0 
 */
public class NotValidOptionException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3903834984963737874L;
	/**
	 * Default constructor method
	 *
	 */
	public NotValidOptionException()
	{
		super("Not valid option!");
	}
	/**
	 * Constructor method specifying invalid option and relative field
	 * 
	 * @param option a String representing the invalid option
	 * @param field a String representing the invalid option field name
	 */
	public NotValidOptionException(String option, String field)
	{
		super("Option "+option+" is not a valid Option for "+field);
	}
	
	public String toString()
	{
		return getMessage()+" Error! "; 
	}
}
