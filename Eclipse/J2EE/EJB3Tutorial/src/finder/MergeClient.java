package finder;

import java.util.List;

import javax.naming.InitialContext;

public class MergeClient {

	public static void main(String[] args) throws Exception
	{
		InitialContext ctx = new InitialContext();
	    CustomerDAO dao = (CustomerDAO) ctx.lookup("CustomerDAOBean/remote");

	    System.out.println("Create Bill Burke and Monica Smith");
	    dao.create("Bill", "Burke", "1 Boston Road", "Boston", "MA", "02115");
	    int moId = dao.create("Monica", "Smith", "1 Boston Road", "Boston", "MA", "02115");

	    System.out.println("Bill and Monica get married");
	    Customer monica = dao.find(moId);
	    monica.setLast("Burke");
	    dao.merge(monica);

	    System.out.println("Get all the Burkes");
	    List<Customer> burkes = dao.findByLastName("Burke");
	    System.out.println("There are now " + burkes.size() + " Burkes");
	    for(Customer x : burkes)
	    {
	    	System.out.println(x.getFirst() + ", " + x.getLast() + " " + x.getStreet() + ", "+
	    			x.getCity() + ", " + x.getState() + " " + x.getZip());
	    }
	}
}
