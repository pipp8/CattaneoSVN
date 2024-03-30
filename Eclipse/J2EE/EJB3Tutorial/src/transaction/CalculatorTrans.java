package transaction;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;

@Remote
public interface CalculatorTrans {
	   public int add(int x, int y);
	   public int subtract(int x, int y);
	   public int divide(int x, int y);
}
