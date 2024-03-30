package valworkbench.exceptions;
/**
 * Signals that the directory 
 * denoted by a specified pathname is empty
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0 
 */
public class EmptyDirException extends Exception {
	
	private static final long serialVersionUID = 7280796603004640058L;
	/**
	 * Default constructor method 
	 *
	 */
	public EmptyDirException()
	{
		super("Warning Empty Directory ");
	}
	/**
	 * Constructor specifying the empty directory path name
	 * 
	 * @param dirName the empty directory path name
	 */
	public EmptyDirException(String dirName)
	{
		super("Warning Directory "+dirName+" is empty!");
	}
	
	public String toString()
	{
		return getMessage()+" no file founded Error!";
	}
}
