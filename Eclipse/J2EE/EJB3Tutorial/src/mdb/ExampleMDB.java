package mdb;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;

@MessageDriven(activationConfig =
{
@ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Queue"),
@ActivationConfigProperty(propertyName="destination", propertyValue="queue/tutorial/example")
})
public class ExampleMDB implements MessageListener
{

	public void onMessage(Message recvMsg)
		{
		  System.out.println("----------------");
		  System.out.println("Received message");
		  System.out.println("----------------");
		}
}
