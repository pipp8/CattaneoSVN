package ejbs;

import javax.ejb.Stateless;

/**
 * Session Bean implementation class GreetingsBean
 */

import java.rmi.Remote;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

@WebService
@SOAPBinding(style = Style.RPC)
	public interface Echo extends Remote {
        String echo(String e);
}

