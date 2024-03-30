package valworkbench.exceptions;
/**
 * Signals that an attempt to open the directory 
 * denoted by a specified path name, has failed
 * because the directory not exist.
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0 
 */
public class DirNotFoundException extends Exception {

	private static final long serialVersionUID = -4204899257610855912L;
	/**
	 * Default constructor method 
	 *
	 */
	public DirNotFoundException()
	{
		super("Cannot find Directory");
	}
	/**
	 * Constructor specifying the directory path name for which opening operation has failed
	 * 
	 * @param nameDir the directory path name 
	 */
	public DirNotFoundException(String nameDir)
	{
		super("Cannot find Directory "+nameDir);
	}
}
