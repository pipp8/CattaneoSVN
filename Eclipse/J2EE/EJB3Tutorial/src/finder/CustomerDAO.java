package finder;

import java.util.List;

import javax.ejb.Remote;

@Remote
public interface CustomerDAO {

	int create(String first, String last, String street, String city, String state, String zip);

	Customer find(int id);

	List findByLastName(String name);

	void merge(Customer c);
}
