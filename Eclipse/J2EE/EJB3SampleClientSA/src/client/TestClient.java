package client;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceFactory;


public class TestClient {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting Test Client");
	    URL url = new URL("http://localhost:8080/EJB3SampleWSEJB/EchoBean?wsdl");
	    QName qname = new QName(	"http://ejbs/",
	    							"EchoBeanService");
	
	    System.out.println("Creating a service Using: \n\t" 
	                               + url + " \n\tand " + qname);
	    ServiceFactory factory = ServiceFactory.newInstance();
	    Service remote = factory.createService(url, qname);
	
	    System.out.println("Obtaining reference to a proxy object");
	    Echo proxy = (Echo) remote.getPort(Echo.class);
	    System.out.println("Accessed local proxy: " + proxy);
	   
	    String string = "John";
	    System.out.println("Sending: " + string);
	    
	    System.out.println("Receiving: " + proxy.echo("John"));
	}
}