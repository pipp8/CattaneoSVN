package beans;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class BookTestBean implements BookTestBeanLocal, BookTestBeanRemote {
	
	@PersistenceContext
	EntityManager em;
	public static final String RemoteJNDIName =  BookTestBean.class.getSimpleName() + "/remote";
	public static final String LocalJNDIName =  BookTestBean.class.getSimpleName() + "/local";

	public void test() {
		Book book = new Book(null, "My first bean book", "Sebastian");
		em.persist(book);
		Book book2 = new Book(null, "another book", "Paul");
		em.persist(book2);
		Book book3 = new Book(null, "EJB 3 developer guide, comes soon", "Sebastian");
		em.persist(book3);
		
		Random rnd = new Random();
		ArrayList idlist = new ArrayList();
		for(int i = 0; i < 10000; i++) {
			Book db= new Book(null, new BigInteger(500, rnd).toString(), new BigInteger(100, rnd).toString());
			idlist.add(db.getId());
			em.persist(db);
		}
		
		System.out.println("list some books");
		List someBooks = em.createQuery("from Book b where b.author=:name")
				.setParameter("name", "Sebastian").getResultList();
		
		for (Iterator iter = someBooks.iterator(); iter.hasNext();)	{
			Book element = (Book) iter.next();
			System.out.println(element);
		}
		System.out.println("List all books");
		List allBooks = em.createQuery("from Book").getResultList();
		for (Iterator iter = allBooks.iterator(); iter.hasNext();) {
			Book element = (Book) iter.next();
			System.out.println(element);
		}
		System.out.println("delete a book");
		em.remove(book2);

		for(int i = 0; i < 100; i++) {
			int tag = rnd.nextInt(idlist.size());
			idlist.remove(tag);
		}
		
		System.out.println("List all books");
		allBooks = em.createQuery("from Book").getResultList();	
		for (Iterator iter = allBooks.iterator(); iter.hasNext();) {
			Book element = (Book) iter.next();
			System.out.println(element);
		}
	}
}	
