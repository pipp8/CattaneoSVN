package valworkbench.datatypes;
/** 
 * The ListNode class provides an Object representing
 * a single node in a linked list.
 *
 * @author Raffaele Giancarlo
 * @author Davide Scaturro
 * @author Filippo Utro
 * @version 1.0
 */
public class ListNode {
    public Object element;
    public ListNode next;
    /**
     * Class constructor specifying a single node
     * 
     * @param element a reference to an Object to insert into the list
     */ 
    public ListNode( Object element) {
        this( element, null );
    }
    /**
     * Class constructor for a linked list
     * 
     * @param element a reference to an Object to insert into the list
     * @param next reference (address) to the next element in the list
     */
    public ListNode( Object element, ListNode next ) {
	if ( element == null )
	    throw new IllegalArgumentException( );
        this.element  = element;
        this.next     = next;
    }
}
