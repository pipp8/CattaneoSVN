/**
 * 
 */
package beans;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;


/**
 *
 * <!-- begin-user-doc -->
 * A generated session bean
 * <!-- end-user-doc -->
 * *
 * <!-- begin-xdoclet-definition --> 
 * @ejb.bean name="ScheduleSession"	
 *           description="Application Facade  Stateless Session Bean"
 *           display-name="ScheduleSession"
 *           jndi-name="ScheduleSession"
 *           type="Stateless" 
 *           transaction-type="Container"
 * 
 * @ejb.ejb-ref ejb-name="ScheduleItem" view-type="local"
 * 
 * @jboss.ejb-local-ref ref-name="ScheduleItemLocal"
 *                      jndi-name="ScheduleItemLocal"
 * 
 * <!-- end-xdoclet-definition --> 
 * @generated
 */

public abstract class ScheduleSessionBean implements javax.ejb.SessionBean {

	/** 
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.create-method view-type="both"
	 * <!-- end-xdoclet-definition --> 
	 * @generated
	 *
	 */
	public void ejbCreate() {
	}

	/** 
	 *
	 * <!-- begin-xdoclet-definition --> 
	 * @ejb.interface-method view-type="both"
	 * <!-- end-xdoclet-definition --> 
	 *  @generated
	 */
	public ScheduleItemData addScheduleItem(String name, int startTime,
			int endTime, int day, String scheduleID) {
		ScheduleItemLocalHome home = getScheduleItemLocalHome();
		try {
			ScheduleItemLocal scheduleItem = home.create(name);
			scheduleItem.setStartTime(Integer.valueOf(startTime));
			scheduleItem.setEndTime(Integer.valueOf(endTime));
			scheduleItem.setDay(Integer.valueOf(day));
			scheduleItem.setScheduleID(scheduleID);
			return new ScheduleItemData(Integer.valueOf(0), name, Integer.valueOf(startTime),
					Integer.valueOf(endTime), Integer.valueOf(day), scheduleID);
		} catch (CreateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @ejb.interface-method view-type="both"
	 */
	public List getScheduleItem(String name) {
		ScheduleItemLocalHome home = getScheduleItemLocalHome();
		try {
			Collection items = home.findByName(name);
			return wrapScheduleItemsInList(items);
		} catch (FinderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @ejb.interface-method view-type="both"
	 */
	public List getAllScheduleItems() {
		ScheduleItemLocalHome home = getScheduleItemLocalHome();
		try {
			Collection items = home.findAll();
			return wrapScheduleItemsInList(items);
		} catch (FinderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @ejb.interface-method view-type="both"
	 */
	public List getScheduleItemesForScheduleID(String scheduleID) {
		ScheduleItemLocalHome home = getScheduleItemLocalHome();
		try {
			Collection items = home.findByScheduleID(scheduleID);
			return wrapScheduleItemsInList(items);
		} catch (FinderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private List wrapScheduleItemsInList(Collection items) {
		Iterator iterator = items.iterator();
		List list = new ArrayList(items.size());
		while (iterator.hasNext()) {
			ScheduleItemLocal scheduleItem = (ScheduleItemLocal) iterator
					.next();
			ScheduleItemData wrapper = new ScheduleItemData( Integer.valueOf(0),
					scheduleItem.getName(), scheduleItem.getStartTime(),
					scheduleItem.getEndTime(), scheduleItem.getDay(), scheduleItem
					.getScheduleID());
			list.add(wrapper);
		}
		return list;
	}

	private ScheduleItemLocalHome getScheduleItemLocalHome() {
		try {
			Context context = new InitialContext();
			Object obj = context.lookup("java:comp/env/ejb/ScheduleItemLocal");
			ScheduleItemLocalHome home = (ScheduleItemLocalHome) PortableRemoteObject
					.narrow(obj, ScheduleItemLocalHome.class);
			return home;
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


	/* (non-Javadoc)
	 * @see javax.ejb.SessionBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException, RemoteException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.ejb.SessionBean#ejbPassivate()
	 */
	public void ejbPassivate() throws EJBException, RemoteException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.ejb.SessionBean#ejbRemove()
	 */
	public void ejbRemove() throws EJBException, RemoteException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext arg0) throws EJBException,
			RemoteException {
		// TODO Auto-generated method stub

	}

	/**
	 * 
	 */
	public ScheduleSessionBean() {
		// TODO Auto-generated constructor stub
	}
}
