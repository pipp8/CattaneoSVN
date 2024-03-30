package transaction;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.annotation.security.SecurityDomain;


import javax.ejb.Stateless;
import transaction.CalculatorTrans;

@Stateless
@SecurityDomain("other")
@Remote(CalculatorTrans.class)
public class CalculatorTransBean implements CalculatorTrans {

	   @PermitAll
	   @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	   public int add(int x, int y)
	   {
	      return x + y;
	   }

	   @RolesAllowed({"student"})
	   public int subtract(int x, int y)
	   {
	      return x - y;
	   }

	   @RolesAllowed({"teacher"})
	   public int divide(int x, int y)
	   {
	      return x / y;
	   }
}
