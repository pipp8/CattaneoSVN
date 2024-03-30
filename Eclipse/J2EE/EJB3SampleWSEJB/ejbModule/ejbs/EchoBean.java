package ejbs;

/**
 * Session Bean implementation class EchoBean
 */
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebService;

import ejbs.Echo;

/**
 * Session Bean implementation class EchoBean
 */
@Stateless
@WebService(endpointInterface = "ejbs.Echo")
@Remote(Echo.class)
public class EchoBean {

	public String echo(String e) {
		return "Web Service Echo + " + e;
	}
}
