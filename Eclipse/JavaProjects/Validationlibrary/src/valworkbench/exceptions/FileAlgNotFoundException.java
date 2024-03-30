package valworkbench.exceptions;
import java.io.FileNotFoundException;
/**
 * Signals that an attempt to open the 
 * executable file denoted by a specified path name has failed
 * 
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0 
 */
public class FileAlgNotFoundException extends FileNotFoundException {
	private static final long serialVersionUID = -2377225896773148100L;
	/**
	 * Default constructor method 
	 *
	 */
	public FileAlgNotFoundException()
	{
		super("Clustering algorithm not Found!");
	}
	/**
	 * Constructor specifying algorithm path name 
	 * 
	 * @param algName the algorithm path name 
	 */
	public FileAlgNotFoundException(String algName)
	{
		super("Clustering algorithm "+algName+" not Found!");
	}
	
	public String toString()
	{
		return getMessage()+" Error! "; 
	}
}
