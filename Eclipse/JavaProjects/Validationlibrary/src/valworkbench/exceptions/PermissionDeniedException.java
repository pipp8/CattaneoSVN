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
public class PermissionDeniedException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Default constructor method 
	 *
	 */
	public PermissionDeniedException(String path)
	{
		super("Read/Write/Execute Permission denied on file "+path);
	}
	/**
	 * Constructor Specifying type of permission denied and file to which permission is denied
	 * 
	 * @param permissionType type of permission denied
	 * @param path path to the file for which permission is denied
	 */
	public PermissionDeniedException(String permissionType, String path)
	{
		super(permissionType+" Permission Denied on file "+path);
	}
}
