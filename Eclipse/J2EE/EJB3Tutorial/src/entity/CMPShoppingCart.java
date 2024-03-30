package entity;

import javax.ejb.Remote;
import javax.ejb.Remove;

@Remote
public interface CMPShoppingCart {
	public void buy(String product, int quantity, double price);
	public Order getOrder();
	@Remove public void checkout();
}
