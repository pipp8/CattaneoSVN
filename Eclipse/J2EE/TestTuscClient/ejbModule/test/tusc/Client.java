package test.tusc;

import java.rmi.RemoteException;
import java.util.Hashtable;

import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Client {

	private test.tusc.MySessionHome getHome() throws  NamingException {
		return (test.tusc.MySessionHome) getContext().lookup( test.tusc.MySessionHome.JNDI_NAME);
	}	
	
	private InitialContext getContext() throws NamingException {
		
		Hashtable props = new Hashtable (); 
        props . put ( InitialContext . INITIAL_CONTEXT_FACTORY , "org.jnp.interfaces.NamingContextFactory" ); 
        props . put ( InitialContext . PROVIDER_URL , "jnp://127.0.0.1:1099" ); 
        return new InitialContext ( props ); 
	}
	
	public void testBean() {
		try {
			test.tusc.MySession myBean = getHome().create();
			String request = "I'm tired of 'Hello, world' example";
			System.out.println("request from client: " + request);
			System.out.println("Message from session bean: " + myBean.learnJ2EE(request));
		}
		catch (RemoteException ex){
			System.err.println("Remote Exception: " + ex.getMessage());
			ex.printStackTrace();
		}
		catch (CreateException ex) {
			System.err.println("Create Exception: " + ex.getMessage());
			ex.printStackTrace();
		}
		catch (NamingException ex) {
			System.err.println("Naming Exception: " + ex.getMessage());
			ex.printStackTrace();
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Client tt = new Client();
		
		tt.testBean();
	}

}
	