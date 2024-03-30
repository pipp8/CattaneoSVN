package entity;

import java.io.Serializable;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import entity.CMPShoppingCart;
@Stateful
public  class CMPShoppingCartBean implements CMPShoppingCart, Serializable {
	
	@PersistenceContext
	private EntityManager manager;
	private Order order;

	public void buy(String product, int quantity, double price)
	{
		if (order == null) order = new Order();
	    order.addPurchase(product, quantity, price);
	} 

	public Order getOrder()
	{
		return order;
	}	   

	@Remove
	public void checkout()
	{
		manager.persist(order);
	}
}
