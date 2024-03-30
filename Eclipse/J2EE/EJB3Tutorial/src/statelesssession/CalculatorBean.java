package statelesssession;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import statelesssession.CalculatorRemote;

@Stateless(name="calculator")
@Remote(CalculatorRemote.class)
@Local(CalculatorLocal.class)
public class CalculatorBean implements CalculatorRemote, CalculatorLocal {

	public int add(int x, int y)
	   {
	      return x + y;
	   }

	   public int subtract(int x, int y)
	   {
	      return x - y;
	   }

}
