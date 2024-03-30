package valworkbench.exceptions;
/**
 * Signals that an attempt to open the directory 
 * denoted by a specified path name, has failed
 * because the directory contains old experiment data
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0 
 */
public class InvalidDirectoryException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Default constructor method 
	 *
	 */
	public InvalidDirectoryException()
	{
		super("Directory contains old experiment data!");
	}
	/**
	 * Constructor specifying the directory path name for which opening operation has failed
	 * 
	 * @param nameDir the directory path name 
	 */
	public InvalidDirectoryException(String nameDir)
	{
		super("Directory "+nameDir+" contains old experiment data!");
	}
}
