package valworkbench.exceptions;
/**
 * Signals that the file opened have an invalid format
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0 
 */
public class NotValidFormatException extends Exception {
	
	private static final long serialVersionUID = -7859156453886277229L;
	/**
	 * Default constructor method
	 *
	 */
	public NotValidFormatException()
	{
		super("Cannot read file! Invalid Format ");
	}
	/**
	 * Constructor method specifying text file path name 
	 * 
	 * @param nameFile the text file pathname
	 */
	public NotValidFormatException(String nameFile)
	{
		super("Cannot read file "+nameFile+"! Invalid format");
	}
	
	public String toString()
	{
		return getMessage()+" Error! "; 
	}
}
