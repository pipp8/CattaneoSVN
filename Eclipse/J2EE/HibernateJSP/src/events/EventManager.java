package events;

import org.hibernate.Session;

import java.util.Date;
import java.util.List;

import util.HibernateUtil;

public class EventManager {

	public static void main(String[] args) {
	    EventManager mgr = new EventManager();
	
	    if (args[0].equals("store")) {
	        mgr.createAndStoreEvent("My Event", new Date());
	    }
	    else if (args[0].equals("list")) {
	    	List events = mgr.listEvents();
	    	for(int i = 0; i < events.size(); i++) {
	    		Event theEvent = (Event) events.get(i);
	    		System.out.println("Event: " + theEvent.getTitle() +
	    				" Time: " + theEvent.getDate());
	    	}
	    }
	    else if (args[0].equals("addpersontoevent")) {
	        Long eventId = mgr.createAndStoreEvent("My Event", new Date());
	        Long personId = mgr.createAndStorePerson("Pippo", "Cattaneo", 48);
	        mgr.addPersonToEvent(personId, eventId);
	        System.out.println("Added person " + personId + " to event " + eventId);
	    }
	    HibernateUtil.getSessionFactory().close();
	}

    private Long  createAndStoreEvent(String title, Date theDate) {

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        session.beginTransaction();

        Event theEvent = new Event();
        theEvent.setTitle(title);
        theEvent.setDate(theDate);

        session.save(theEvent);
        Long result = theEvent.getId();
        
        System.out.println("Event saved id:" + result);

        session.getTransaction().commit();
        return result;
    }
    
    private List listEvents() {
    	Session session = HibernateUtil.getSessionFactory().getCurrentSession();
    	
    	session.beginTransaction();
    	List result = session.createQuery("FROM Event").list();
    	session.getTransaction().commit();
    	
    	return result;
    }
    
    private Long  createAndStorePerson(String firstname, String lastname, int age) {

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();

        session.beginTransaction();

        Person thePerson = new Person();
        thePerson.setFirstname(firstname);
        thePerson.setLastname(lastname);
        thePerson.setAge(age);

        session.save(thePerson);
        Long result = thePerson.getId();
        
        System.out.println("Person saved id:" + result);

        session.getTransaction().commit();
        return result;
    }
 
    private void addPersonToEvent(Long personId, Long eventId) {

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        Person aPerson = (Person) session.load(Person.class, personId);
        Event anEvent = (Event) session.load(Event.class, eventId);

        aPerson.getEvents().add(anEvent);

        session.getTransaction().commit();
    }

    private void addEmailToPerson(Long personId, String emailAddress) {

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        Person aPerson = (Person) session.load(Person.class, personId);

        // The getEmailAddresses() might trigger a lazy load of the collection
        aPerson.getEmailAddresses().add(emailAddress);

        session.getTransaction().commit();
    }

}
