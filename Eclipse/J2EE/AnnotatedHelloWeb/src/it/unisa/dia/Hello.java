package it.unisa.dia;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;


/**
* This is a webservice class exposing a method called greet which takes a
* input parameter and greets the parameter with hello.
*
* @author ankit
*/

/*
* @WebService indicates that this is webservice interface and the name
* indicates the webservice name.
*/
@WebService(name = "Hello")
/*
* @SOAPBinding indicates binding information of soap messages. Here we have
* document-literal style of webservice and the parameter style is wrapped.
*/
@SOAPBinding
(
      style = SOAPBinding.Style.DOCUMENT,
      use = SOAPBinding.Use.LITERAL,
      parameterStyle = SOAPBinding.ParameterStyle.WRAPPED
)

public class Hello
{

	/**
	 * This method takes a input parameter and appends "Hello" to it and
	 * returns the same.
	 *
	 * @param name
	 * @return
	 */
	@WebMethod
	public String greet( @WebParam(name = "name")
	String name )
	{
	   return "Hello " + name + "!!!";
	}
}
